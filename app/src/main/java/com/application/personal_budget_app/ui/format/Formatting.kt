package com.application.personal_budget_app.ui.format

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayMonth = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH)

/** 25 sept., 24 oct. */
fun LocalDate.shortFr(): String = format(dayMonth)

fun BudgetCycle.label(): String = "${start.shortFr()} → ${end.shortFr()}"

fun daysLabel(count: Int): String = if (count <= 1) "$count jour" else "$count jours"