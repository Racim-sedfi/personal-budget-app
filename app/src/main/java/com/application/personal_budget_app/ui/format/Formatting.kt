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

/** "Du 25 au 24 du mois suivant", "Du 2 au 1er du mois suivant", "Du 1er au dernier jour du mois". */
fun cycleRule(startDay: Int): String =
    if (startDay == 1) "Du 1er au dernier jour du mois"
    else "Du $startDay au ${if (startDay - 1 == 1) "1er" else (startDay - 1).toString()} du mois suivant"


private val monthShort = DateTimeFormatter.ofPattern("MMM", Locale.FRENCH)

/** "sept.", "oct." */
fun LocalDate.monthShortFr(): String = format(monthShort)