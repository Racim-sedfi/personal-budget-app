package com.application.personal_budget_app.domain.home

import com.application.personal_budget_app.domain.budget.EnvelopeStatus
import com.application.personal_budget_app.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class HomeSummaryTest {
    private val today = LocalDate.of(2026, 10, 12)
    private val categories = listOf(
        Category(1, "Courses", "cart", Money.euros(250)),
        Category(2, "Restaurants", "restaurant", Money.euros(90)),
        Category(3, "Transports", "bus", Money.euros(75)),
        Category(4, "Loisirs", "ticket", Money.euros(60)),
        Category(5, "Shopping", "bag", Money.euros(80)),
        Category(6, "Santé", "health", Money.euros(30)),
        Category(7, "Imprévus", "umbrella", Money.euros(60), isFuse = true),
    )
    private fun expense(category: Long, euros: Long, cents: Long = 0) = Transaction(
        amount = Money.euros(euros, cents), type = TransactionType.EXPENSE,
        categoryId = category, date = LocalDate.of(2026, 10, 10),
    )
    private val transactions = listOf(
        expense(1, 142, 30), expense(2, 64, 50), expense(3, 38, 20),
        expense(4, 95), expense(5, 47, 90), expense(6, 12),
    )
    private val incomes = listOf(
        Income(name = "Salaire", amount = Money.euros(1650), dayOfMonth = 25),
        Income(name = "Aide au logement", amount = Money.euros(200), dayOfMonth = 5),
    )
    private val savings = listOf(PlannedSaving(label = "Livret", amount = Money.euros(150), dayOfMonth = 26))
    private val charges = listOf(
        FixedCharge(name = "Loyer", amount = Money.euros(650), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 10, 25)),
        FixedCharge(name = "Électricité", amount = Money.euros(48), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 11, 10)),
        FixedCharge(name = "Internet", amount = Money.euros(30), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 11, 1)),
        FixedCharge(name = "Téléphone", amount = Money.euros(15), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 11, 5)),
    )

    private fun build(mode: BudgetMode, tx: List<Transaction> = transactions) = buildHomeSummary(
        today, AppSettings(cycleStartDay = 25, mode = mode, onboardingDone = true),
        tx, emptySet(), categories, incomes, savings, charges,
    )

    @Test fun `budget mode gives 557,10 remaining and a comfortable status`() {
        val summary = build(BudgetMode.BUDGET)
        assertEquals(Money.euros(399, 90), summary.spent)
        assertEquals(Money.euros(957), summary.snapshot!!.spendable)
        assertEquals(Money.euros(557, 10), summary.snapshot!!.remaining)
        assertEquals(EnvelopeStatus.NORMAL, summary.overallStatus)
        assertEquals(18, summary.dayNumber)
        assertEquals(12, summary.daysRemaining)
    }

    @Test fun `envelope statuses match the design`() {
        val byName = build(BudgetMode.BUDGET).envelopes.associateBy { it.category.name }
        assertEquals(EnvelopeStatus.OVER, byName.getValue("Loisirs").status)
        assertEquals(Money.euros(-35), byName.getValue("Loisirs").remaining)
        assertEquals(EnvelopeStatus.NEAR_LIMIT, byName.getValue("Restaurants").status)
        assertEquals(EnvelopeStatus.NORMAL, byName.getValue("Courses").status)
    }

    @Test fun `next fixed charge is the soonest upcoming one`() =
        assertEquals("Loyer", build(BudgetMode.BUDGET).nextFixedCharge?.name)

    @Test fun `observation mode has no budget and sorts by spending`() {
        val summary = build(BudgetMode.OBSERVATION)
        assertNull(summary.snapshot)
        assertNull(summary.overallStatus)
        assertEquals("Courses", summary.envelopes.first().category.name)
        assertEquals("Imprévus", summary.envelopes.last().category.name)
    }

    @Test fun `refund reduces its envelope`() {
        val refund = Transaction(amount = Money.euros(12), type = TransactionType.REFUND, categoryId = 2, date = LocalDate.of(2026, 10, 8))
        val restaurants = build(BudgetMode.BUDGET, transactions + refund).envelopes.first { it.category.id == 2L }
        assertEquals(Money.euros(52, 50), restaurants.spent)
    }

    @Test fun `transactions outside the cycle are ignored`() {
        val old = expense(1, 999).copy(date = LocalDate.of(2026, 9, 24))
        assertEquals(Money.euros(399, 90), build(BudgetMode.BUDGET, transactions + old).spent)
    }
}