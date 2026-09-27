package com.application.personal_budget_app.ui.format

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH)
private val weekdayDayMonth = DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH)

/** 25 sept., 24 oct. */
fun LocalDate.shortFr(): String = format(dayMonth)

fun BudgetCycle.label(): String = "${start.shortFr()} → ${end.shortFr()}"

fun daysLabel(count: Int): String = if (count <= 1) "$count jour" else "$count jours"

/** lun. 12 oct. */
fun LocalDate.withWeekdayFr(): String = format(weekdayDayMonth)

/** « Aujourd'hui · lun. 12 oct. », « Hier · dim. 11 oct. » ou « sam. 10 oct. » */
fun LocalDate.dayTitle(today: LocalDate): String = when (this) {
    today -> "Aujourd'hui · ${withWeekdayFr()}"
    today.minusDays(1) -> "Hier · ${withWeekdayFr()}"
    else -> withWeekdayFr()
}