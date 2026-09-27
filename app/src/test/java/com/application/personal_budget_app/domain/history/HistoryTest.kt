package com.application.personal_budget_app.domain.history

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.cycle.DayState
import com.application.personal_budget_app.domain.model.Money
import com.application.personal_budget_app.domain.model.Transaction
import com.application.personal_budget_app.domain.model.TransactionType
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class HistoryTest {
    private val today = LocalDate.of(2026, 10, 12)
    private val cycle = BudgetCycle.containing(today, 25)
    private fun day(d: Int) = LocalDate.of(2026, 10, d)
    private fun tx(date: LocalDate, cents: Long, type: TransactionType = TransactionType.EXPENSE, id: Long = 0) =
        Transaction(id = id, amount = Money(cents), type = type, categoryId = 1, date = date)

    @Test fun `current cycle lists days from today back to cycle start`() {
        val days = buildHistory(cycle, today, emptyList(), emptySet())
        assertEquals(18, days.size)
        assertEquals(today, days.first().date)
        assertEquals(cycle.start, days.last().date)
    }

    @Test fun `each day gets its state`() {
        val days = buildHistory(cycle, today, listOf(tx(day(10), 3800)), setOf(day(11))).associateBy { it.date }
        assertEquals(DayState.TODAY_OPEN, days.getValue(today).state)
        assertEquals(DayState.NO_EXPENSE, days.getValue(day(11)).state)
        assertEquals(DayState.WITH_TRANSACTIONS, days.getValue(day(10)).state)
        assertEquals(DayState.NOT_FILLED, days.getValue(day(9)).state)
    }

    @Test fun `day total nets refunds`() {
        val oct8 = listOf(tx(day(8), 1735), tx(day(8), 1200, TransactionType.REFUND))
        val day8 = buildHistory(cycle, today, oct8, emptySet()).first { it.date == day(8) }
        assertEquals(Money(535), day8.total)
    }

    @Test fun `latest entry of a day comes first`() {
        val entries = listOf(tx(day(10), 100, id = 1), tx(day(10), 200, id = 2))
        val day10 = buildHistory(cycle, today, entries, emptySet()).first { it.date == day(10) }
        assertEquals(listOf(2L, 1L), day10.transactions.map { it.id })
    }

    @Test fun `previous cycle lists all its days and no today`() {
        val previous = cycle.shifted(-1)
        assertEquals(LocalDate.of(2026, 8, 25), previous.start)
        assertEquals(LocalDate.of(2026, 9, 24), previous.end)
        val days = buildHistory(previous, today, emptyList(), emptySet())
        assertEquals(previous.lengthInDays, days.size)
        assertTrue(days.none { it.state == DayState.TODAY_OPEN })
    }
}