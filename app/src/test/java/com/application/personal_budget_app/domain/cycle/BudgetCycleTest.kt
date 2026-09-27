package com.application.personal_budget_app.cycle

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class BudgetCycleTest {
    private val oct12 = LocalDate.of(2026, 10, 12)

    @Test fun `date after start day belongs to cycle starting previous month`() {
        val cycle = BudgetCycle.containing(oct12, startDay = 25)
        assertEquals(LocalDate.of(2026, 9, 25), cycle.start)
        assertEquals(LocalDate.of(2026, 10, 24), cycle.end)
        assertEquals(30, cycle.lengthInDays)
    }

    @Test fun `reference data - day 18 of 30 with 12 days remaining`() {
        val cycle = BudgetCycle.containing(oct12, 25)
        assertEquals(18, cycle.dayNumber(oct12))
        assertEquals(12, cycle.daysRemainingAfter(oct12))
    }

    @Test fun `start day itself opens a new cycle`() {
        val cycle = BudgetCycle.containing(LocalDate.of(2026, 10, 25), 25)
        assertEquals(LocalDate.of(2026, 10, 25), cycle.start)
        assertEquals(LocalDate.of(2026, 11, 24), cycle.end)
    }

    @Test fun `start day 1 matches the calendar month, including leap years`() {
        val cycle = BudgetCycle.containing(LocalDate.of(2028, 2, 10), 1)
        assertEquals(LocalDate.of(2028, 2, 1), cycle.start)
        assertEquals(LocalDate.of(2028, 2, 29), cycle.end)
    }

    @Test fun `crosses the year boundary`() {
        val cycle = BudgetCycle.containing(LocalDate.of(2027, 1, 5), 25)
        assertEquals(LocalDate.of(2026, 12, 25), cycle.start)
        assertEquals(LocalDate.of(2027, 1, 24), cycle.end)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects start day above 28`() {
        BudgetCycle.containing(oct12, 31)
    }
}