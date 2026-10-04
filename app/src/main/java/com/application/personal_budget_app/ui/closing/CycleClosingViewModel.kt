package com.application.personal_budget_app.ui.closing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.closing.*
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

enum class LeftoverChoice { SAVE, CARRY }

data class ClosingResult(
    val outcome: CycleOutcome,
    val leftover: Money,
    val nextCycle: BudgetCycle,
    val nextSpendable: Money?,
    val budgetActivated: Boolean = false,
    val needsSetup: Boolean = false,
)

data class CycleClosingUiState(
    val review: CycleReview,
    val availableNext: Money,                    // à dépenser sur le nouveau cycle, sans report
    val choice: LeftoverChoice = LeftoverChoice.SAVE,
    val applied: Set<Long> = emptySet(),         // ajustements acceptés (id de catégorie)
    val caps: Map<Long, Money?> = emptyMap(),    // plafonds proposés (observation)
    val saving: Boolean = false,
    val result: ClosingResult? = null,
)

@HiltViewModel
class CycleClosingViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val transactions: TransactionRepository,
    private val dayStatus: DayStatusRepository,
    private val categories: CategoryRepository,
    private val budget: BudgetRepository,
    private val closedCycles: ClosedCycleRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow<CycleClosingUiState?>(null)
    val state: StateFlow<CycleClosingUiState?> = _state.asStateFlow()

    private lateinit var current: BudgetCycle

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val appSettings = settings.settings.first()
        current = BudgetCycle.containing(LocalDate.now(clock), appSettings.cycleStartDay)
        val ended = current.shifted(-1)
        val incomes = budget.observeIncomes().first()
        val savings = budget.observeSavings().first()
        val charges = budget.observeFixedCharges().first()

        val review = buildCycleReview(
            cycle = ended,
            mode = appSettings.mode,
            transactions = transactions.observeBetween(ended.start, ended.end).first(),
            noExpenseDays = dayStatus.observeNoExpenseDays(ended.start, ended.end).first(),
            categories = categories.observeAll().first(),
            incomes = incomes,
            savings = savings,
            charges = charges,
            carryIn = closedCycles.observe(ended.shifted(-1).start).first()?.carryOver ?: Money.ZERO,
        )
        val availableNext = incomes.map { it.amount }.sum() - savings.map { it.amount }.sum() - charges.totalFor(current)
        _state.value = CycleClosingUiState(review, availableNext, caps = review.proposedCaps())
    }

    fun choose(choice: LeftoverChoice) = _state.update { it?.copy(choice = choice) }

    fun toggleAdjustment(categoryId: Long) = _state.update { s ->
        s?.copy(applied = if (categoryId in s.applied) s.applied - categoryId else s.applied + categoryId)
    }

    /** −5 / +5 € sur un plafond proposé. En dessous de 5 €, l'enveloppe n'a plus de plafond. */
    fun stepCap(categoryId: Long, deltaEuros: Long) = _state.update { s ->
        s ?: return@update null
        val next = (s.caps[categoryId] ?: Money.ZERO) + Money.euros(deltaEuros)
        s.copy(caps = s.caps + (categoryId to next.takeIf { it.cents > 0 }))
    }

    fun validateBudget() = finish { s ->
        val review = s.review
        review.adjustments.filter { it.category.id in s.applied }
            .forEach { categories.update(it.category.copy(cap = it.suggested)) }
        val outcome = when {
            review.leftover.cents <= 0 -> CycleOutcome.NOTHING_LEFT
            s.choice == LeftoverChoice.CARRY -> CycleOutcome.CARRIED
            else -> CycleOutcome.SAVED
        }
        val closed = ClosedCycle(review.cycle.start, review.cycle.end, outcome, review.leftover)
        closedCycles.close(closed)
        ClosingResult(outcome, review.leftover, current, s.availableNext + closed.carryOver)
    }

    fun applyProposedCaps() = finish { s ->
        val review = s.review
        review.envelopes.filterNot { it.category.archived }.forEach { line -> categories.update(line.category.copy(cap = s.caps[line.category.id])) }
        if (review.hasIncome) settings.setMode(BudgetMode.BUDGET)
        closedCycles.close(ClosedCycle(review.cycle.start, review.cycle.end, CycleOutcome.OBSERVED, Money.ZERO))
        ClosingResult(
            CycleOutcome.OBSERVED, Money.ZERO, current,
            nextSpendable = if (review.hasIncome) s.availableNext else null,
            budgetActivated = review.hasIncome,
            needsSetup = !review.hasIncome,
        )
    }

    fun keepObserving() = finish { s ->
        closedCycles.close(ClosedCycle(s.review.cycle.start, s.review.cycle.end, CycleOutcome.OBSERVED, Money.ZERO))
        ClosingResult(CycleOutcome.OBSERVED, Money.ZERO, current, nextSpendable = null)
    }

    private fun finish(action: suspend (CycleClosingUiState) -> ClosingResult) {
        val s = _state.value ?: return
        if (s.saving) return // double appui
        _state.value = s.copy(saving = true)
        viewModelScope.launch {
            val result = action(s)
            _state.update { it?.copy(saving = false, result = result) }
        }
    }
}