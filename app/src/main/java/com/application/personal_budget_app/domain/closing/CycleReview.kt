package com.application.personal_budget_app.domain.closing

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.cycle.Completion
import com.application.personal_budget_app.domain.cycle.completion
import com.application.personal_budget_app.domain.home.EnvelopeLine
import com.application.personal_budget_app.domain.model.*
import java.time.LocalDate

/** Sous ce pourcentage d'utilisation, on propose de baisser un plafond. */
const val UNDERUSED_PERCENT = 50

data class CapAdjustment(val category: Category, val spent: Money, val current: Money, val suggested: Money) {
    val isOver: Boolean get() = spent > current
}

/** Le bilan d'un cycle terminé. */
data class CycleReview(
    val cycle: BudgetCycle,
    val mode: BudgetMode,
    val spent: Money,
    val spendable: Money?,          // null en observation
    val envelopes: List<EnvelopeLine>,
    val completion: Completion,
    val hasIncome: Boolean,
) {
    val leftover: Money get() = spendable?.let { it - spent }?.takeIf { it.cents > 0 } ?: Money.ZERO
    val overspent: Money get() = spendable?.let { spent - it }?.takeIf { it.cents > 0 } ?: Money.ZERO

    /** Plafonds à revoir : dépassés ou utilisés à moins de moitié. Seulement si le cycle est fiable. */
    val adjustments: List<CapAdjustment>
        get() = if (!completion.isReliable) emptyList() else envelopes.mapNotNull { line ->
            val cap = line.category.cap
            if (cap == null || line.category.isFuse) return@mapNotNull null
            val over = line.spent > cap
            val underused = line.spent.cents * 100 < cap.cents * UNDERUSED_PERCENT
            if (!over && !underused) return@mapNotNull null
            val suggested = maxOf(line.spent.roundUpTo(10), Money.euros(10))
            if (suggested == cap) null else CapAdjustment(line.category, line.spent, cap, suggested)
        }

    /**
     * Plafonds proposés après observation, arrondis aux 5 € supérieurs.
     * Rien dépensé → pas de plafond. Imprévus : son plafond actuel, sinon 10 % des dépenses (20 € minimum).
     */
    fun proposedCaps(): Map<Long, Money?> = envelopes.filterNot { it.category.archived }.associate { line ->
        line.category.id to when {
            line.category.isFuse -> line.category.cap ?: maxOf(Money(spent.cents / 10).roundUpTo(10), Money.euros(20))
            line.spent.cents > 0 -> line.spent.roundUpTo(5)
            else -> null
        }
    }
}

fun buildCycleReview(
    cycle: BudgetCycle,
    mode: BudgetMode,
    transactions: List<Transaction>,
    noExpenseDays: Set<LocalDate>,
    categories: List<Category>,
    incomes: List<Income>,
    savings: List<PlannedSaving>,
    charges: List<FixedCharge>,
    carryIn: Money,
): CycleReview {
    val inCycle = transactions.filter { it.date in cycle }
    val byCategory = inCycle.groupBy { it.categoryId }.mapValues { (_, list) -> list.netSpent() }
    val income = incomes.map { it.amount }.sum()
    return CycleReview(
        cycle = cycle,
        mode = mode,
        spent = inCycle.netSpent(),
        spendable = if (mode == BudgetMode.BUDGET) {
            income - savings.map { it.amount }.sum() - charges.totalFor(cycle) + carryIn + inCycle.extraIncome()
        } else null,
        envelopes = categories
            .map { EnvelopeLine(it, byCategory[it.id] ?: Money.ZERO) }
            .filter { !it.category.archived || it.spent.cents != 0L },
        // Le cycle est fini : tous ses jours comptent.
        completion = completion(cycle, cycle.end.plusDays(1), inCycle.map { it.date }.toSet(), noExpenseDays),
        hasIncome = income.cents > 0,
    )
}