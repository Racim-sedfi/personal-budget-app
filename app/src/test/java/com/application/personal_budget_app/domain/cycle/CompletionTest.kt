package com.application.personal_budget_app.cycle

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.cycle.Completion
import com.application.personal_budget_app.domain.cycle.DayState
import com.application.personal_budget_app.domain.cycle.completion
import com.application.personal_budget_app.domain.cycle.dayState
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CompletionTest {
    private val today = LocalDate.of(2026, 10, 12)
    private val cycle = BudgetCycle.containing(today, 25)

    @Test fun `reference data - 14 of 17 days is 82 percent and reliable`() {
        val missing = setOf(LocalDate.of(2026, 9, 29), LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 9))
        val noExpense = setOf(LocalDate.of(2026, 10, 11))
        val withTx = cycle.days().filter { it.isBefore(today) && it !in missing && it !in noExpense }.toSet()

        val result = completion(cycle, today, withTx, noExpense)

        assertEquals(17, result.elapsedDays)
        assertEquals(14, result.filledDays)
        assertEquals(3, result.missingDays)
        assertEquals(82, result.percent)
        assertTrue(result.isReliable)
    }

    @Test fun `below 80 percent is not reliable`() {
        assertFalse(Completion(filledDays = 11, elapsedDays = 17).isReliable)
    }

    @Test fun `first day of cycle is 100 percent`() {
        val first = cycle.start
        assertEquals(100, completion(cycle, first, emptySet(), emptySet()).percent)
    }

    @Test fun `day states follow priority rules`() {
        val past = LocalDate.of(2026, 10, 9)
        assertEquals(DayState.NOT_FILLED, dayState(past, today, false, false))
        assertEquals(DayState.NO_EXPENSE, dayState(past, today, false, true))
        assertEquals(DayState.WITH_TRANSACTIONS, dayState(past, today, true, true))
        assertEquals(DayState.TODAY_OPEN, dayState(today, today, false, false))
        assertEquals(DayState.FUTURE, dayState(today.plusDays(1), today, false, false))
    }
}