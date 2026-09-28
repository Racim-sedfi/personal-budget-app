package com.application.personal_budget_app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.closing.cycleToClose
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.home.HomeSummary
import com.application.personal_budget_app.domain.home.buildHomeSummary
import com.application.personal_budget_app.domain.model.Money
import com.application.personal_budget_app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(val summary: HomeSummary) : HomeUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    settings: SettingsRepository,
    transactions: TransactionRepository,
    dayStatus: DayStatusRepository,
    categories: CategoryRepository,
    budget: BudgetRepository,
    closedCycles: ClosedCycleRepository,
    clock: Clock,
) : ViewModel() {

    private val budgetItems = combine(
        budget.observeIncomes(), budget.observeSavings(), budget.observeFixedCharges(),
    ) { incomes, savings, charges -> Triple(incomes, savings, charges) }

    val state: StateFlow<HomeUiState> = settings.settings
        .flatMapLatest { appSettings ->
            val today = LocalDate.now(clock)
            val cycle = BudgetCycle.containing(today, appSettings.cycleStartDay)
            val previous = cycle.shifted(-1)

            // Le cycle précédent : clôturé ? contient-il des saisies ?
            val previousInfo = combine(
                closedCycles.observe(previous.start),
                transactions.observeBetween(previous.start, previous.end),
                dayStatus.observeNoExpenseDays(previous.start, previous.end),
            ) { closed, tx, noExpense -> closed to (tx.isNotEmpty() || noExpense.isNotEmpty()) }

            combine(
                transactions.observeBetween(cycle.start, cycle.end),
                dayStatus.observeNoExpenseDays(cycle.start, cycle.end),
                categories.observeAll(),
                budgetItems,
                previousInfo,
            ) { tx, noExpense, cats, (incomes, savings, charges), (closed, hadActivity) ->
                HomeUiState.Ready(
                    buildHomeSummary(
                        today, appSettings, tx, noExpense, cats, incomes, savings, charges,
                        carryOver = closed?.carryOver ?: Money.ZERO,
                        cycleToClose = cycleToClose(cycle, closed != null, hadActivity),
                    )
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}