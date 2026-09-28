package com.application.personal_budget_app.domain.closing

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.Money
import java.time.LocalDate

enum class CycleOutcome { SAVED, CARRIED, OBSERVED, NOTHING_LEFT }

/** Un cycle clôturé, et ce qu'on a fait de son reste. */
data class ClosedCycle(
    val start: LocalDate,
    val end: LocalDate,
    val outcome: CycleOutcome,
    val leftover: Money,
) {
    /** Ce qui s'ajoute au cycle suivant. */
    val carryOver: Money get() = if (outcome == CycleOutcome.CARRIED) leftover else Money.ZERO
}

/**
 * Le cycle précédent, s'il reste à clôturer.
 * Un cycle sans aucune saisie (avant l'installation, par exemple) n'est pas proposé.
 */
fun cycleToClose(current: BudgetCycle, previousClosed: Boolean, previousHasActivity: Boolean): BudgetCycle? =
    if (!previousClosed && previousHasActivity) current.shifted(-1) else null