package com.application.personal_budget_app.domain.widget

import com.application.personal_budget_app.domain.budget.EnvelopeStatus
import com.application.personal_budget_app.domain.home.buildHomeSummary
import com.application.personal_budget_app.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class WidgetDataTest {
    private val today = LocalDate.of(2026, 10, 12)
    private fun expense(cat: Long, euros: Long, day: Int) =
        Transaction(amount = Money.euros(euros), type = TransactionType.EXPENSE, categoryId = cat, date = LocalDate.of(2026, 10, day))
    private val tx = listOf(expense(1, 75, 1), expense(2, 25, 12))

    private fun summary(settings: AppSettings) = buildHomeSummary(
        today, settings, tx, emptySet(),
        listOf(Category(1, "Courses", "cart"), Category(2, "Loisirs", "ticket")),
        incomes = listOf(Income(name = "Salaire", amount = Money.euros(1000), dayOfMonth = 25)),
        savings = emptyList(), charges = emptyList(),
    )

    private val base = AppSettings(cycleStartDay = 25, onboardingDone = true, currency = AppCurrency.MAD)

    @Test fun `budget mode shows what is left and how it goes`() {
        val s = base.copy(mode = BudgetMode.BUDGET)
        val data = widgetData(s, summary(s), todaySpent = Money.euros(25)) as WidgetData.Remaining
        assertEquals(Money.euros(900), data.amount)
        assertEquals(AppCurrency.MAD, data.currency)
        assertEquals(12, data.daysLeft)                        // du 12 au 24 oct.
        assertEquals(LocalDate.of(2026, 10, 24), data.cycleEnd)
        assertEquals(Money.euros(100), data.spent)
        assertEquals(Money.euros(1000), data.spendable)
        assertEquals(EnvelopeStatus.NORMAL, data.status)
        assertEquals(Money.euros(25), data.todaySpent)
        assertEquals(0.1f, data.progress, 0.001f)
        assertEquals(Money(6923), data.perDay)                 // 900 € sur 13 jours (aujourd'hui compris)
    }

    @Test fun `observation mode shows spending and the main envelope`() {
        val s = base.copy(mode = BudgetMode.OBSERVATION)
        val data = widgetData(s, summary(s)) as WidgetData.Spent
        assertEquals(Money.euros(100), data.amount)
        assertEquals("Courses", data.topCategory)
        assertEquals(75, data.topPercent)
    }

    @Test fun `no amount on the home screen when the app is locked`() {
        val s = base.copy(mode = BudgetMode.BUDGET, lockEnabled = true)
        assertEquals(WidgetData.Locked, widgetData(s, summary(s)))
    }

    @Test fun `not configured before onboarding`() =
        assertEquals(WidgetData.NotConfigured, widgetData(AppSettings(), null))

    @Test fun `no daily amount once over budget`() {
        val over = WidgetData.Remaining(
            Money.euros(-5), AppCurrency.EUR, 3, today, Money.euros(105), Money.euros(100),
            EnvelopeStatus.OVER, Money.ZERO,
        )
        assertTrue(over.isOver)
        assertNull(over.perDay)
        assertEquals(1f, over.progress, 0.001f)
    }
}
