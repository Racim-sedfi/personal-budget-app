package com.application.personal_budget_app.domain.budget

import com.application.personal_budget_app.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BudgetSnapshotTest {

    @Test fun `reference data gives 957 spendable and 557,10 remaining`() {
        val snapshot = BudgetSnapshot(
            income = Money.euros(1850),
            plannedSavings = Money.euros(150),
            fixedCharges = Money.euros(743),
            variableSpent = Money.euros(399, 90),
        )
        assertEquals(Money.euros(957), snapshot.spendable)
        assertEquals(Money.euros(557, 10), snapshot.remaining)
    }

    @Test fun `refund reduces net spending`() {
        val day = LocalDate.of(2026, 10, 8)
        val tx = listOf(
            Transaction(amount = Money.euros(17, 35), type = TransactionType.EXPENSE, categoryId = 3, date = day),
            Transaction(amount = Money.euros(12), type = TransactionType.REFUND, categoryId = 2, date = day),
        )
        assertEquals(Money.euros(5, 35), tx.netSpent())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `transaction amount must be positive`() {
        Transaction(amount = Money.ZERO, type = TransactionType.EXPENSE, categoryId = 1, date = LocalDate.now())
    }

    @Test fun `carry over increases what can be spent`() {
        val snapshot = BudgetSnapshot(Money.euros(1850), Money.euros(150), Money.euros(743), Money.ZERO, carryOver = Money.euros(355, 60))
        assertEquals(Money.euros(1312, 60), snapshot.spendable)
    }

    @Test fun `extra income increases what can be spent`() {
        val snapshot = BudgetSnapshot(Money.euros(1850), Money.euros(150), Money.euros(743), Money.ZERO, extraIncome = Money.euros(50))
        assertEquals(Money.euros(1007), snapshot.spendable)
    }

}