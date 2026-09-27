package com.application.personal_budget_app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.cycle.Completion
import com.application.personal_budget_app.domain.cycle.completion
import com.application.personal_budget_app.domain.history.HistoryDay
import com.application.personal_budget_app.domain.history.buildHistory
import com.application.personal_budget_app.domain.model.Category
import com.application.personal_budget_app.domain.model.Money
import com.application.personal_budget_app.domain.model.netSpent
import com.application.personal_budget_app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class HistoryUiState(
    val cycle: BudgetCycle,
    val today: LocalDate,
    val isCurrentCycle: Boolean,
    val days: List<HistoryDay>,
    val spent: Money,
    val completion: Completion,
    val categories: Map<Long, Category>,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    settings: SettingsRepository,
    transactions: TransactionRepository,
    private val dayStatus: DayStatusRepository,
    categories: CategoryRepository,
    private val clock: Clock,
) : ViewModel() {

    /** 0 = cycle en cours, -1 = précédent, etc. */
    private val cycleOffset = MutableStateFlow(0)

    /** null tant que les données ne sont pas chargées. */
    val state: StateFlow<HistoryUiState?> = combine(settings.settings, cycleOffset) { s, offset -> s.cycleStartDay to offset }
        .flatMapLatest { (startDay, offset) ->
            val today = LocalDate.now(clock)
            val cycle = BudgetCycle.containing(today, startDay).shifted(offset)
            combine(
                transactions.observeBetween(cycle.start, cycle.end),
                dayStatus.observeNoExpenseDays(cycle.start, cycle.end),
                categories.observeAll(),
            ) { tx, noExpense, cats ->
                HistoryUiState(
                    cycle = cycle,
                    today = today,
                    isCurrentCycle = offset == 0,
                    days = buildHistory(cycle, today, tx, noExpense),
                    spent = tx.netSpent(),
                    completion = completion(cycle, today, tx.map { it.date }.toSet(), noExpense),
                    categories = cats.associateBy { it.id },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun previousCycle() = cycleOffset.update { it - 1 }
    fun nextCycle() = cycleOffset.update { minOf(it + 1, 0) } // jamais dans le futur

    fun declareNoExpense(date: LocalDate) {
        viewModelScope.launch { dayStatus.declareNoExpense(date) }
    }

    fun clearNoExpense(date: LocalDate) {
        viewModelScope.launch { dayStatus.clearNoExpense(date) }
    }
}