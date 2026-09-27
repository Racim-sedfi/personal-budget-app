package com.application.personal_budget_app.domain.cycle

import java.time.LocalDate
import kotlin.math.roundToInt

/** Complétion d'un cycle, calculée sur les jours passés (aujourd'hui exclu). */
data class Completion(val filledDays: Int, val elapsedDays: Int) {
    val missingDays: Int get() = elapsedDays - filledDays
    val percent: Int get() = if (elapsedDays == 0) 100 else (filledDays * 100.0 / elapsedDays).roundToInt()
    /** Sous 80 %, le cycle est atténué dans les graphes et exclu des moyennes. */
    val isReliable: Boolean get() = elapsedDays == 0 || filledDays * 100 >= elapsedDays * 80
}

fun completion(
    cycle: BudgetCycle,
    today: LocalDate,
    daysWithTransactions: Set<LocalDate>,
    noExpenseDays: Set<LocalDate>,
): Completion {
    val pastDays = cycle.days().filter { it.isBefore(today) }
    val filled = pastDays.count { it in daysWithTransactions || it in noExpenseDays }
    return Completion(filledDays = filled, elapsedDays = pastDays.size)
}