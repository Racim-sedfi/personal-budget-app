package com.application.personal_budget_app.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.analysis.ANALYSIS_CYCLES
import com.application.personal_budget_app.domain.analysis.Analysis
import com.application.personal_budget_app.domain.analysis.buildAnalysis
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    settings: SettingsRepository,
    transactions: TransactionRepository,
    dayStatus: DayStatusRepository,
    categories: CategoryRepository,
    budget: BudgetRepository,
    clock: Clock,
) : ViewModel() {

    val state: StateFlow<Analysis?> = settings.settings
        .flatMapLatest { appSettings ->
            val today = LocalDate.now(clock)
            val current = BudgetCycle.containing(today, appSettings.cycleStartDay)
            val first = current.shifted(-(ANALYSIS_CYCLES - 1))
            combine(
                transactions.observeBetween(first.start, current.end),
                dayStatus.observeNoExpenseDays(first.start, current.end),
                categories.observeAll(),
                budget.observeIncomes(),
                budget.observeFixedCharges(),
            ) { tx, noExpense, cats, incomes, charges ->
                buildAnalysis(today, appSettings.cycleStartDay, tx, noExpense, cats, incomes, charges)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}