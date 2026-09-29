package com.application.personal_budget_app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TransactionTest {
    private val day = LocalDate.of(2026, 10, 10)
    private fun tx(euros: Long, type: TransactionType, category: Long? = 1) =
        Transaction(amount = Money.euros(euros), type = type, categoryId = category, date = day)

    private val list = listOf(
        tx(40, TransactionType.EXPENSE),
        tx(10, TransactionType.REFUND),
        tx(50, TransactionType.INCOME, category = null),
    )

    @Test fun `income is not counted as spending`() = assertEquals(Money.euros(30), list.netSpent())

    @Test fun `extra income sums incomes only`() = assertEquals(Money.euros(50), list.extraIncome())

    @Test(expected = IllegalArgumentException::class)
    fun `income cannot have a category`() { tx(50, TransactionType.INCOME, category = 1) }

    @Test(expected = IllegalArgumentException::class)
    fun `expense needs a category`() { tx(40, TransactionType.EXPENSE, category = null) }
}