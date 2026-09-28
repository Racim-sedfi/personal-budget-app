package com.application.personal_budget_app.domain.budget

enum class SetupStep {
    INCOMES, CHARGES, CAPS;

    val number: Int get() = ordinal + 1
    val isFirst: Boolean get() = ordinal == 0
    val isLast: Boolean get() = ordinal == entries.lastIndex

    fun next(): SetupStep = entries[minOf(ordinal + 1, entries.lastIndex)]
    fun previous(): SetupStep = entries[maxOf(ordinal - 1, 0)]

    /** Sans revenu, impossible de calculer un budget : on bloque les revenus et l'activation. */
    fun canContinue(overview: BudgetOverview): Boolean = when (this) {
        CHARGES -> true
        INCOMES, CAPS -> overview.canActivateBudget
    }

    companion object {
        val count: Int get() = entries.size
    }
}