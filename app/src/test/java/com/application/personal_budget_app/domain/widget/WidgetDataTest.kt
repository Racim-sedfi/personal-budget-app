package com.application.personal_budget_app.domain.widget

import com.application.personal_budget_app.domain.home.buildHomeSummary
import com.application.personal_budget_app.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WidgetDataTest {
    private val today = LocalDate.of(2026, 10, 12)
    private val tx = listOf(
        Transaction(amount = Money.euros(100), type = TransactionType.EXPENSE, categoryId = 1, date = LocalDate.of(2026, 10, 1)),
    )

    private fun summary(settings: AppSettings) = buildHomeSummary(
        today, settings, tx, emptySet(), listOf(Category(1, "Courses", "cart")),
        incomes = listOf(Income(name = "Salaire", amount = Money.euros(1000), dayOfMonth = 25)),
        savings = emptyList(), charges = emptyList(),
    )

    private val base = AppSettings(cycleStartDay = 25, onboardingDone = true, currency = AppCurrency.MAD)

    @Test fun `budget mode shows what is left`() {
        val s = base.copy(mode = BudgetMode.BUDGET)
        val data = widgetData(s, summary(s)) as WidgetData.Remaining
        assertEquals(Money.euros(900), data.amount)
        assertEquals(AppCurrency.MAD, data.currency)
        assertEquals(12, data.daysLeft)                       // du 12 au 24 oct.
        assertEquals(LocalDate.of(2026, 10, 24), data.cycleEnd)
    }

    @Test fun `observation mode shows what was spent`() {
        val s = base.copy(mode = BudgetMode.OBSERVATION)
        assertEquals(Money.euros(100), (widgetData(s, summary(s)) as WidgetData.Spent).amount)
    }

    @Test fun `no amount on the home screen when the app is locked`() {
        val s = base.copy(mode = BudgetMode.BUDGET, lockEnabled = true)
        assertEquals(WidgetData.Locked, widgetData(s, summary(s)))
    }

    @Test fun `not configured before onboarding`() {
        assertEquals(WidgetData.NotConfigured, widgetData(AppSettings(), null))
    }

    @Test fun `overspending is flagged`() {
        assertTrue(WidgetData.Remaining(Money.euros(-5), AppCurrency.EUR, 3, today).isOver)
    }
}
