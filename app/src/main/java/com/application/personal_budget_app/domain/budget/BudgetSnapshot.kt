package com.application.personal_budget_app.domain.budget

import com.application.personal_budget_app.domain.model.Money

/** Photo du budget d'un cycle. */
data class BudgetSnapshot(
    val income: Money,
    val plannedSavings: Money,
    val fixedCharges: Money,
    val variableSpent: Money,
    val carryOver: Money = Money.ZERO,   // reporté du cycle précédent
) {
    /** Ce qu'on peut dépenser sur le cycle. */
    val spendable: Money get() = income - plannedSavings - fixedCharges + carryOver

    /** Reste disponible = Revenus − Épargne − Charges fixes − Dépenses variables. */
    val remaining: Money get() = spendable - variableSpent
}