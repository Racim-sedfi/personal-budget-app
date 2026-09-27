package com.application.personal_budget_app.domain.cycle

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** Cycle budgétaire, bornes incluses. Ex. 25 sept. → 24 oct. */
data class BudgetCycle(val start: LocalDate, val end: LocalDate) {

    val lengthInDays: Int get() = ChronoUnit.DAYS.between(start, end).toInt() + 1

    operator fun contains(date: LocalDate) = !date.isBefore(start) && !date.isAfter(end)

    /** Numéro du jour dans le cycle : le premier jour vaut 1. */
    fun dayNumber(date: LocalDate): Int = ChronoUnit.DAYS.between(start, date).toInt() + 1

    /** Jours restants après `date` (aujourd'hui exclu). */
    fun daysRemainingAfter(date: LocalDate): Int = ChronoUnit.DAYS.between(date, end).toInt()

    /** Tous les jours du cycle. */
    fun days(): List<LocalDate> = (0 until lengthInDays).map { start.plusDays(it.toLong()) }

    /** Le cycle décalé de `cycles` (négatif = passé). Ex. shifted(-1) = cycle précédent. */
    fun shifted(cycles: Int): BudgetCycle =
        containing(start.plusMonths(cycles.toLong()), start.dayOfMonth)
    companion object {
        /** Le cycle qui contient `date`, pour un jour de début entre 1 et 28. */
        fun containing(date: LocalDate, startDay: Int): BudgetCycle {
            require(startDay in 1..28) { "Le jour de début doit être entre 1 et 28" }
            val month = YearMonth.from(date).let { if (date.dayOfMonth >= startDay) it else it.minusMonths(1) }
            val start = month.atDay(startDay)
            return BudgetCycle(start, start.plusMonths(1).minusDays(1))
        }
    }
}