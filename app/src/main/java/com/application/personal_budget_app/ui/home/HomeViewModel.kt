package com.application.personal_budget_app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.home.HomeSummary
import com.application.personal_budget_app.domain.home.buildHomeSummary
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
    clock: Clock,
) : ViewModel() {

    private val budgetItems = combine(
        budget.observeIncomes(), budget.observeSavings(), budget.observeFixedCharges(),
    ) { incomes, savings, charges -> Triple(incomes, savings, charges) }

    val state: StateFlow<HomeUiState> = settings.settings
        .flatMapLatest { appSettings ->
            val today = LocalDate.now(clock)
            val cycle = BudgetCycle.containing(today, appSettings.cycleStartDay)
            combine(
                transactions.observeBetween(cycle.start, cycle.end),
                dayStatus.observeNoExpenseDays(cycle.start, cycle.end),
                categories.observeAll(),
                budgetItems,
            ) { tx, noExpense, cats, (incomes, savings, charges) ->
                HomeUiState.Ready(buildHomeSummary(today, appSettings, tx, noExpense, cats, incomes, savings, charges))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}