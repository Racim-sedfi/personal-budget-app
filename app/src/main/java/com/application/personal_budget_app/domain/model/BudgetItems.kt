package com.application.personal_budget_app.domain.model

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import java.time.LocalDate

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

/** Mensuelles : chaque cycle. Trimestrielles/annuelles : seulement le cycle de leur échéance. */
fun Iterable<FixedCharge>.totalFor(cycle: BudgetCycle): Money =
    filter { it.frequency == Frequency.MONTHLY || it.nextDueDate in cycle }
        .map { it.amount }
        .sum()