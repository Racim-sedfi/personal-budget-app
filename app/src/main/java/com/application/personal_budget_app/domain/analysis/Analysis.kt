package com.application.personal_budget_app.domain.analysis

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.cycle.Completion
import com.application.personal_budget_app.domain.cycle.completion
import com.application.personal_budget_app.domain.model.*
import java.time.LocalDate
import kotlin.math.roundToInt

/** Nombre de cycles affichés dans l'évolution (cycle en cours compris). */
const val ANALYSIS_CYCLES = 6

/** Il faut au moins ce nombre de cycles fiables pour calculer des moyennes. */
const val MIN_RELIABLE_CYCLES = 2

data class CategoryShare(val category: Category, val spent: Money, val percent: Int)

data class CycleStat(
    val cycle: BudgetCycle,
    val spent: Money,
    val completion: Completion,
    val isCurrent: Boolean,
) {
    /** Utilisable dans les moyennes : terminé et renseigné à 80 % ou plus. */
    val isReliable: Boolean get() = !isCurrent && completion.isReliable
}

data class EnvelopeAverage(val category: Category, val average: Money) {
    /** De combien la moyenne dépasse le plafond, ou null. */
    val overBy: Money? get() = category.cap?.let { cap -> (average - cap).takeIf { it.cents > 0 } }
}

data class Analysis(
    val cycle: BudgetCycle,
    val today: LocalDate,
    val spent: Money,                 // dépenses nettes du cycle en cours
    val shares: List<CategoryShare>,  // cycle en cours, de la plus grosse à la plus petite
    val cycles: List<CycleStat>,      // du plus ancien au cycle en cours
    val averages: List<EnvelopeAverage>,
    val capsTotal: Money,
    val fixedChargesPercent: Int?,    // null sans revenu
    val missingDays: Int,             // journées non renseignées du cycle en cours
) {
    val reliableCycles: List<CycleStat> get() = cycles.filter { it.isReliable }
    val hasAverages: Boolean get() = reliableCycles.size >= MIN_RELIABLE_CYCLES

    val averageSpent: Money?
        get() = reliableCycles.takeIf { it.isNotEmpty() }
            ?.let { list -> Money(list.sumOf { it.spent.cents } / list.size) }

    val biggestOverrun: EnvelopeAverage?
        get() = averages.filter { it.overBy != null }.maxByOrNull { it.overBy!!.cents }
}

fun buildAnalysis(
    today: LocalDate,
    cycleStartDay: Int,
    transactions: List<Transaction>,
    noExpenseDays: Set<LocalDate>,
    categories: List<Category>,
    incomes: List<Income>,
    charges: List<FixedCharge>,
): Analysis {
    val current = BudgetCycle.containing(today, cycleStartDay)
    val candidates = (ANALYSIS_CYCLES - 1 downTo 0).map { current.shifted(-it) }

    // On ne montre pas les cycles d'avant la première saisie.
    val activeDays = transactions.map { it.date }.toSet() + noExpenseDays
    val firstActive = candidates.indexOfFirst { c -> activeDays.any { it in c } }
    val shown = if (firstActive == -1) listOf(current) else candidates.drop(firstActive)

    val stats = shown.map { c ->
        val inCycle = transactions.filter { it.date in c }
        val isCurrent = c == current
        CycleStat(
            cycle = c,
            spent = inCycle.netSpent(),
            completion = completion(c, if (isCurrent) today else c.end.plusDays(1), inCycle.map { it.date }.toSet(), noExpenseDays),
            isCurrent = isCurrent,
        )
    }

    // Répartition du cycle en cours (seulement les catégories avec des dépenses).
    val currentTx = transactions.filter { it.date in current }
    val byCategory = currentTx.groupBy { it.categoryId }.mapValues { (_, list) -> list.netSpent() }
    val positive = categories.mapNotNull { cat -> byCategory[cat.id]?.takeIf { it.cents > 0 }?.let { cat to it } }
    val positiveTotal = positive.sumOf { it.second.cents }
    val shares = positive
        .sortedByDescending { it.second.cents }
        .map { (cat, spent) -> CategoryShare(cat, spent, (spent.cents * 100.0 / positiveTotal).roundToInt()) }

    // Moyennes par enveloppe, sur les cycles fiables uniquement.
    val reliable = stats.filter { it.isReliable }
    val averages = if (reliable.isEmpty()) emptyList() else categories.map { cat ->
        val total = reliable.sumOf { s ->
            transactions.filter { it.date in s.cycle && it.categoryId == cat.id }.netSpent().cents
        }
        EnvelopeAverage(cat, Money(total / reliable.size))
    }.filter { !it.category.archived || it.average.cents != 0L } // archivée : seulement si elle a servi

    val income = incomes.map { it.amount }.sum()
    return Analysis(
        cycle = current,
        today = today,
        spent = currentTx.netSpent(),
        shares = shares,
        cycles = stats,
        averages = averages,
        capsTotal = categories.filterNot { it.archived }.mapNotNull { it.cap }.sum(),
        fixedChargesPercent = if (income.cents > 0) {
            (charges.totalFor(current).cents * 100.0 / income.cents).roundToInt()
        } else null,
        missingDays = stats.last().completion.missingDays,
    )
}