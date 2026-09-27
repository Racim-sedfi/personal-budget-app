package com.application.personal_budget_app.domain.budget

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.*

/** Tout ce que l'écran Budget affiche, calculé pour le cycle en cours. */
data class BudgetOverview(
    val mode: BudgetMode,
    val cycle: BudgetCycle,
    val incomes: List<Income>,
    val savings: List<PlannedSaving>,
    val charges: List<FixedCharge>,
    val categories: List<Category>,
) {
    val incomeTotal: Money get() = incomes.map { it.amount }.sum()
    val savingsTotal: Money get() = savings.map { it.amount }.sum()
    val chargesThisCycle: Money get() = charges.totalFor(cycle)
    val spendable: Money get() = incomeTotal - savingsTotal - chargesThisCycle
    val capsTotal: Money get() = categories.mapNotNull { it.cap }.sum()
    val unallocated: Money get() = spendable - capsTotal
    val canActivateBudget: Boolean get() = incomeTotal.cents > 0
}