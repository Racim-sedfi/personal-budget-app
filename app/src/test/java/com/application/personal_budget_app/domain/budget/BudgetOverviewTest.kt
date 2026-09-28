package com.application.personal_budget_app.domain.budget

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class BudgetOverviewTest {
    private val today = LocalDate.of(2026, 10, 12)

    private val overview = BudgetOverview(
        mode = BudgetMode.BUDGET,
        cycle = BudgetCycle.containing(today, 25),
        incomes = listOf(
            Income(name = "Salaire", amount = Money.euros(1650), dayOfMonth = 25),
            Income(name = "Aide au logement", amount = Money.euros(200), dayOfMonth = 5),
        ),
        savings = listOf(PlannedSaving(label = "Livret", amount = Money.euros(150), dayOfMonth = 26)),
        charges = listOf(
            FixedCharge(name = "Loyer", amount = Money.euros(650), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 10, 25)),
            FixedCharge(name = "Électricité", amount = Money.euros(48), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 11, 10)),
            FixedCharge(name = "Internet", amount = Money.euros(30), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 11, 1)),
            FixedCharge(name = "Téléphone", amount = Money.euros(15), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 11, 5)),
            FixedCharge(name = "Assurance", amount = Money.euros(96), frequency = Frequency.YEARLY, nextDueDate = LocalDate.of(2027, 3, 15)),
        ),
        categories = listOf(
            Category(1, "Courses", "cart", Money.euros(250)), Category(2, "Restaurants", "restaurant", Money.euros(90)),
            Category(3, "Transports", "bus", Money.euros(75)), Category(4, "Loisirs", "ticket", Money.euros(60)),
            Category(5, "Shopping", "bag", Money.euros(80)), Category(6, "Santé", "health", Money.euros(30)),
            Category(7, "Imprévus", "umbrella", Money.euros(60), isFuse = true),
        ),
    )

    @Test fun `reference data - 957 to spend, 645 of caps, 312 unallocated`() {
        assertEquals(Money.euros(1850), overview.incomeTotal)
        assertEquals(Money.euros(743), overview.chargesThisCycle) // l'assurance annuelle n'est pas de ce cycle
        assertEquals(Money.euros(957), overview.spendable)
        assertEquals(Money.euros(645), overview.capsTotal)
        assertEquals(Money.euros(312), overview.unallocated)
    }

    @Test fun `budget cannot be activated without income`() =
        assertFalse(overview.copy(incomes = emptyList()).canActivateBudget)

    @Test fun `categories without cap are ignored in caps total`() {
        val noCaps = overview.categories.map { it.copy(cap = null) }
        assertEquals(Money.ZERO, overview.copy(categories = noCaps).capsTotal)
    }

    @Test fun `parses common ways of typing an amount`() {
        assertEquals(Money.euros(650), parseAmount("650"))
        assertEquals(Money.euros(48, 90), parseAmount("48,9"))
        assertEquals(Money.euros(12, 50), parseAmount("12.50"))
        assertEquals(Money.euros(1650), parseAmount("1 650,00 €"))
    }

    @Test fun `rejects invalid amounts`() {
        assertNull(parseAmount(""))
        assertNull(parseAmount("abc"))
        assertNull(parseAmount("12,345"))
        assertNull(parseAmount("-5"))
    }

    @Test fun `next monthly date is this month if not passed yet`() {
        assertEquals(LocalDate.of(2026, 10, 25), nextMonthlyDate(25, today))
        assertEquals(LocalDate.of(2026, 10, 12), nextMonthlyDate(12, today))
    }

    @Test fun `next monthly date moves to next month once passed`() =
        assertEquals(LocalDate.of(2026, 11, 10), nextMonthlyDate(10, today))

    @Test fun `day 31 falls on the last day of a short month`() =
        assertEquals(LocalDate.of(2027, 2, 28), nextMonthlyDate(31, LocalDate.of(2027, 2, 5)))
}