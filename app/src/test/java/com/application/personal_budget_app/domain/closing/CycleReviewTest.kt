package com.application.personal_budget_app.domain.closing

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CycleReviewTest {
    private val cycle = BudgetCycle.containing(LocalDate.of(2026, 10, 12), 25) // 25 sept. → 24 oct.
    private val allDays = cycle.days().toSet()

    private val categories = listOf(
        Category(1, "Courses", "cart", Money.euros(250)),
        Category(2, "Restaurants", "restaurant", Money.euros(90)),
        Category(3, "Transports", "bus", Money.euros(75)),
        Category(4, "Loisirs", "ticket", Money.euros(60)),
        Category(5, "Shopping", "bag", Money.euros(80)),
        Category(6, "Santé", "health", Money.euros(30)),
        Category(7, "Imprévus", "umbrella", Money.euros(60), isFuse = true),
    )

    private fun tx(category: Long, euros: Long, cents: Long = 0) = Transaction(
        amount = Money.euros(euros, cents), type = TransactionType.EXPENSE,
        categoryId = category, date = LocalDate.of(2026, 10, 1),
    )

    private val transactions = listOf(
        tx(1, 231, 80), tx(2, 98, 50), tx(3, 61, 20), tx(4, 118), tx(5, 79, 90), tx(6, 12),
    )

    private fun review(
        mode: BudgetMode = BudgetMode.BUDGET,
        cats: List<Category> = categories,
        noExpense: Set<LocalDate> = allDays,
        income: Long = 1850,
        carryIn: Money = Money.ZERO,
    ) = buildCycleReview(
        cycle = cycle, mode = mode, transactions = transactions, noExpenseDays = noExpense,
        categories = cats,
        incomes = listOf(Income(name = "Salaire", amount = Money.euros(income), dayOfMonth = 25)),
        savings = listOf(PlannedSaving(label = "Livret", amount = Money.euros(150), dayOfMonth = 26)),
        charges = listOf(FixedCharge(name = "Charges", amount = Money.euros(743), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 10, 1))),
        carryIn = carryIn,
    )

    @Test fun `leftover of the reference cycle`() {
        val r = review()
        assertEquals(Money.euros(601, 40), r.spent)
        assertEquals(Money.euros(957), r.spendable)
        assertEquals(Money.euros(355, 60), r.leftover)
        assertEquals(Money.ZERO, r.overspent)
    }

    @Test fun `carry over from the previous cycle adds to spendable`() =
        assertEquals(Money.euros(1007), review(carryIn = Money.euros(50)).spendable)

    @Test fun `overspending has no leftover`() {
        val r = review(income = 1000) // 1000 − 150 − 743 = 107 à dépenser
        assertEquals(Money.ZERO, r.leftover)
        assertEquals(Money.euros(494, 40), r.overspent)
    }

    @Test fun `suggests caps for over and underused envelopes`() {
        val adjustments = review().adjustments
        assertEquals(listOf(2L, 4L, 6L), adjustments.map { it.category.id })       // Restos, Loisirs, Santé
        assertEquals(listOf(100L, 120L, 20L).map { Money.euros(it) }, adjustments.map { it.suggested })
    }

    @Test fun `no suggestions when the cycle is not reliable`() =
        assertTrue(review(noExpense = emptySet()).adjustments.isEmpty())

    @Test fun `observation proposes caps from real spending`() {
        val r = review(mode = BudgetMode.OBSERVATION, cats = categories.map { it.copy(cap = null) })
        assertNull(r.spendable)
        assertEquals(
            mapOf(1L to 235L, 2L to 100L, 3L to 65L, 4L to 120L, 5L to 80L, 6L to 15L, 7L to 70L)
                .mapValues { Money.euros(it.value) },
            r.proposedCaps(),
        )
    }

    @Test fun `rounds up to the next step`() {
        assertEquals(Money.euros(235), Money.euros(231, 80).roundUpTo(5))
        assertEquals(Money.euros(120), Money.euros(120).roundUpTo(10))
        assertEquals(Money.ZERO, Money.ZERO.roundUpTo(5))
    }

    @Test fun `an empty or already closed previous cycle is not proposed`() {
        val current = cycle.shifted(1)
        assertEquals(cycle, cycleToClose(current, previousClosed = false, previousHasActivity = true))
        assertNull(cycleToClose(current, previousClosed = true, previousHasActivity = true))
        assertNull(cycleToClose(current, previousClosed = false, previousHasActivity = false))
    }

    @Test fun `extra income adds to the leftover, not to spending`() {
        val gift = Transaction(amount = Money.euros(50), type = TransactionType.INCOME, categoryId = null, date = LocalDate.of(2026, 10, 3))
        val r = buildCycleReview(
            cycle = cycle, mode = BudgetMode.BUDGET, transactions = transactions + gift, noExpenseDays = allDays,
            categories = categories,
            incomes = listOf(Income(name = "Salaire", amount = Money.euros(1850), dayOfMonth = 25)),
            savings = listOf(PlannedSaving(label = "Livret", amount = Money.euros(150), dayOfMonth = 26)),
            charges = listOf(FixedCharge(name = "Charges", amount = Money.euros(743), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 10, 1))),
            carryIn = Money.ZERO,
        )
        assertEquals(Money.euros(601, 40), r.spent)
        assertEquals(Money.euros(405, 60), r.leftover)
    }
}