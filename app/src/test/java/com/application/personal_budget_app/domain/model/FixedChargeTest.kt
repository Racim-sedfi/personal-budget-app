package com.application.personal_budget_app.domain.model

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FixedChargeTest {
    private val charges = listOf(
        FixedCharge(name = "Loyer", amount = Money.euros(650), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 10, 25)),
        FixedCharge(name = "Électricité", amount = Money.euros(48), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 10, 10)),
        FixedCharge(name = "Internet", amount = Money.euros(30), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 11, 1)),
        FixedCharge(name = "Téléphone", amount = Money.euros(15), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 11, 5)),
        FixedCharge(name = "Eau", amount = Money.euros(45), frequency = Frequency.QUARTERLY, nextDueDate = LocalDate.of(2027, 1, 15)),
        FixedCharge(name = "Assurance", amount = Money.euros(96), frequency = Frequency.YEARLY, nextDueDate = LocalDate.of(2027, 3, 15)),
    )

    @Test fun `reference cycle counts only monthly charges - 743`() {
        val cycle = BudgetCycle.containing(LocalDate.of(2026, 10, 12), 25)
        assertEquals(Money.euros(743), charges.totalFor(cycle))
    }

    @Test fun `quarterly charge counts in the cycle of its due date`() {
        val january = BudgetCycle.containing(LocalDate.of(2027, 1, 15), 25)
        assertEquals(Money.euros(788), charges.totalFor(january))
    }
}