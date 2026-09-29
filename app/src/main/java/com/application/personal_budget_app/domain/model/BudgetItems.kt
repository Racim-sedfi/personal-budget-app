package com.application.personal_budget_app.domain.model

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class Frequency { MONTHLY, QUARTERLY, YEARLY }

data class Income(val id: Long = 0, val name: String, val amount: Money, val dayOfMonth: Int)

data class PlannedSaving(val id: Long = 0, val label: String, val amount: Money, val dayOfMonth: Int)

data class FixedCharge(
    val id: Long = 0,
    val name: String,
    val amount: Money,
    val frequency: Frequency,
    val nextDueDate: LocalDate,
)

val Frequency.months: Long
    get() = when (this) {
        Frequency.MONTHLY -> 1
        Frequency.QUARTERLY -> 3
        Frequency.YEARLY -> 12
    }

/**
 * Première échéance à partir de `date` (incluse).
 * La date enregistrée sert de point de départ : on avance de 1, 3 ou 12 mois, jamais avant elle.
 * Chaque échéance est calculée depuis le point de départ, donc un 31 redevient un 31 après février.
 */
fun FixedCharge.nextDueOnOrAfter(date: LocalDate): LocalDate {
    if (!date.isAfter(nextDueDate)) return nextDueDate
    val step = frequency.months
    var k = ChronoUnit.MONTHS.between(nextDueDate, date) / step
    var candidate = nextDueDate.plusMonths(k * step)
    while (candidate.isBefore(date)) {
        k++
        candidate = nextDueDate.plusMonths(k * step)
    }
    return candidate
}

/** Une échéance tombe-t-elle dans ce cycle ? */
fun FixedCharge.isDueIn(cycle: BudgetCycle): Boolean =
    frequency == Frequency.MONTHLY || !nextDueOnOrAfter(cycle.start).isAfter(cycle.end)

/** La même charge, avec sa prochaine échéance à partir de `date` (pour l'affichage). */
fun FixedCharge.rolledTo(date: LocalDate): FixedCharge = copy(nextDueDate = nextDueOnOrAfter(date))

/** Mensuelles : chaque cycle. Trimestrielles/annuelles : les cycles où tombe une échéance. */
fun Iterable<FixedCharge>.totalFor(cycle: BudgetCycle): Money =
    filter { it.isDueIn(cycle) }.map { it.amount }.sum()