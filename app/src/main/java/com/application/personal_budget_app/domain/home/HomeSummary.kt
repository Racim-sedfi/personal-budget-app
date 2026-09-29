package com.application.personal_budget_app.domain.home

import com.application.personal_budget_app.domain.budget.BudgetSnapshot
import com.application.personal_budget_app.domain.budget.EnvelopeStatus
import com.application.personal_budget_app.domain.budget.envelopeStatus
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.cycle.Completion
import com.application.personal_budget_app.domain.cycle.completion
import com.application.personal_budget_app.domain.model.*
import java.time.LocalDate

/** Une enveloppe et ce qui a été dépensé dedans ce cycle. */
data class EnvelopeLine(val category: Category, val spent: Money) {
    val status: EnvelopeStatus? get() = category.cap?.let { envelopeStatus(spent, it) }
    val remaining: Money? get() = category.cap?.let { it - spent }
}

data class HomeSummary(
    val mode: BudgetMode,
    val cycle: BudgetCycle,
    val today: LocalDate,
    val spent: Money,
    val snapshot: BudgetSnapshot?,      // null en mode observation
    val envelopes: List<EnvelopeLine>,
    val completion: Completion,
    val hasTransactions: Boolean,
    val plannedSavings: Money,
    val fixedCharges: Money,
    val nextFixedCharge: FixedCharge?,
    val cycleToClose: BudgetCycle? = null,   // cycle précédent à clôturer
) {
    val dayNumber: Int get() = cycle.dayNumber(today)
    val daysRemaining: Int get() = cycle.daysRemainingAfter(today)
    /** État global : on réutilise la règle des enveloppes sur le budget total. */
    val overallStatus: EnvelopeStatus? get() = snapshot?.let { envelopeStatus(spent, it.spendable) }
}

fun buildHomeSummary(
    today: LocalDate,
    settings: AppSettings,
    transactions: List<Transaction>,
    noExpenseDays: Set<LocalDate>,
    categories: List<Category>,
    incomes: List<Income>,
    savings: List<PlannedSaving>,
    charges: List<FixedCharge>,
    carryOver: Money = Money.ZERO,
    cycleToClose: BudgetCycle? = null,
): HomeSummary {
    val cycle = BudgetCycle.containing(today, settings.cycleStartDay)
    val inCycle = transactions.filter { it.date in cycle }
    val spent = inCycle.netSpent()
    val spentByCategory = inCycle.groupBy { it.categoryId }.mapValues { (_, list) -> list.netSpent() }
    val lines = categories.map { EnvelopeLine(it, spentByCategory[it.id] ?: Money.ZERO) }
    val savingsTotal = savings.map { it.amount }.sum()
    val chargesTotal = charges.totalFor(cycle)
    val isBudget = settings.mode == BudgetMode.BUDGET

    return HomeSummary(
        mode = settings.mode,
        cycle = cycle,
        today = today,
        spent = spent,
        snapshot = if (isBudget) BudgetSnapshot(incomes.map { it.amount }.sum(), savingsTotal, chargesTotal, spent, carryOver) else null,
        envelopes = if (isBudget) lines else lines.sortedByDescending { it.spent },
        completion = completion(cycle, today, inCycle.map { it.date }.toSet(), noExpenseDays),
        hasTransactions = inCycle.isNotEmpty(),
        plannedSavings = savingsTotal,
        fixedCharges = chargesTotal,
        nextFixedCharge = charges.map { it.rolledTo(today.plusDays(1)) }.minByOrNull { it.nextDueDate },
        cycleToClose = cycleToClose,
    )
}