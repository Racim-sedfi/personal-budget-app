package com.application.personal_budget_app.domain.history

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.cycle.DayState
import com.application.personal_budget_app.domain.cycle.dayState
import com.application.personal_budget_app.domain.model.Money
import com.application.personal_budget_app.domain.model.Transaction
import com.application.personal_budget_app.domain.model.netSpent
import java.time.LocalDate

data class HistoryDay(
    val date: LocalDate,
    val state: DayState,
    val transactions: List<Transaction>,
) {
    /** Total net du jour : les remboursements viennent en moins. */
    val total: Money get() = transactions.netSpent()
}

/** Jours du cycle, du plus récent au plus ancien, sans les jours futurs. */
fun buildHistory(
    cycle: BudgetCycle,
    today: LocalDate,
    transactions: List<Transaction>,
    noExpenseDays: Set<LocalDate>,
): List<HistoryDay> {
    val lastDay = minOf(today, cycle.end)
    if (lastDay.isBefore(cycle.start)) return emptyList()
    val byDate = transactions.groupBy { it.date }

    return generateSequence(lastDay) { it.minusDays(1) }
        .takeWhile { !it.isBefore(cycle.start) }
        .map { date ->
            val dayTransactions = byDate[date].orEmpty().sortedByDescending { it.id } // dernière saisie en haut
            HistoryDay(
                date = date,
                state = dayState(date, today, dayTransactions.isNotEmpty(), date in noExpenseDays),
                transactions = dayTransactions,
            )
        }
        .toList()
}