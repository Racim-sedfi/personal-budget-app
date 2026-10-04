package com.application.personal_budget_app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.budget.BudgetOverview
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.domain.repository.BudgetRepository
import com.application.personal_budget_app.domain.repository.CategoryRepository
import com.application.personal_budget_app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Le formulaire ouvert. Un élément null = création. */
sealed interface BudgetEditor {
    data class IncomeEditor(val income: Income?) : BudgetEditor
    data class SavingEditor(val saving: PlannedSaving?) : BudgetEditor
    data class ChargeEditor(val charge: FixedCharge?) : BudgetEditor
    data class CategoryEditor(val category: Category?) : BudgetEditor
}

data class BudgetUiState(val overview: BudgetOverview, val editor: BudgetEditor?)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val budget: BudgetRepository,
    private val categories: CategoryRepository,
    private val clock: Clock,
) : ViewModel() {

    private val editor = MutableStateFlow<BudgetEditor?>(null)

    private val overview = settings.settings.flatMapLatest { appSettings ->
        val cycle = BudgetCycle.containing(LocalDate.now(clock), appSettings.cycleStartDay)
        combine(
            budget.observeIncomes(), budget.observeSavings(),
            budget.observeFixedCharges(), categories.observeActive(),
        ) { incomes, savings, charges, cats ->
            BudgetOverview(
                appSettings.mode, cycle, incomes, savings,
                charges.map { it.rolledTo(cycle.start) }.sortedBy { it.nextDueDate },
                cats,
            )
        }
    }

    val state: StateFlow<BudgetUiState?> = combine(overview, editor) { o, e -> BudgetUiState(o, e) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun today(): LocalDate = LocalDate.now(clock)

    fun edit(target: BudgetEditor) { editor.value = target }
    fun closeEditor() { editor.value = null }

    fun saveIncome(income: Income) = saveAndClose { budget.upsertIncome(income) }
    fun saveSaving(saving: PlannedSaving) = saveAndClose { budget.upsertSaving(saving) }
    fun saveCharge(charge: FixedCharge) = saveAndClose { budget.upsertFixedCharge(charge) }
    fun saveCategory(category: Category) = saveAndClose {
        if (category.id == 0L) categories.add(category) else categories.update(category)
    }
    fun deleteCategory(category: Category) = saveAndClose { categories.remove(category) }

    fun deleteIncome(id: Long) = saveAndClose { budget.deleteIncome(id) }
    fun deleteSaving(id: Long) = saveAndClose { budget.deleteSaving(id) }
    fun deleteCharge(id: Long) = saveAndClose { budget.deleteFixedCharge(id) }

    fun setMode(mode: BudgetMode) {
        viewModelScope.launch { settings.setMode(mode) }
    }

    /** Active le budget puis prévient l'écran, une fois l'écriture terminée. */
    fun activateBudget(onActivated: () -> Unit) {
        viewModelScope.launch {
            settings.setMode(BudgetMode.BUDGET)
            onActivated()
        }
    }
    private fun saveAndClose(action: suspend () -> Unit) {
        viewModelScope.launch {
            action()
            editor.value = null
        }
    }
}