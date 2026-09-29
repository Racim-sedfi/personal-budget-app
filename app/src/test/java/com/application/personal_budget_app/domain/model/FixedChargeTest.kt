package com.application.personal_budget_app.domain.model

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    private fun charge(frequency: Frequency, due: LocalDate) =
        FixedCharge(name = "Test", amount = Money.euros(45), frequency = frequency, nextDueDate = due)

    @Test fun `quarterly charge moves forward by three months`() =
        assertEquals(
            LocalDate.of(2026, 10, 15),
            charge(Frequency.QUARTERLY, LocalDate.of(2026, 1, 15)).nextDueOnOrAfter(LocalDate.of(2026, 10, 12)),
        )

    @Test fun `yearly charge moves to next year once passed`() =
        assertEquals(
            LocalDate.of(2027, 3, 15),
            charge(Frequency.YEARLY, LocalDate.of(2025, 3, 15)).nextDueOnOrAfter(LocalDate.of(2026, 10, 12)),
        )

    @Test fun `due date itself counts`() =
        assertEquals(
            LocalDate.of(2026, 10, 15),
            charge(Frequency.QUARTERLY, LocalDate.of(2026, 7, 15)).nextDueOnOrAfter(LocalDate.of(2026, 10, 15)),
        )

    @Test fun `never goes before the saved date`() =
        assertEquals(
            LocalDate.of(2027, 1, 15),
            charge(Frequency.QUARTERLY, LocalDate.of(2027, 1, 15)).nextDueOnOrAfter(LocalDate.of(2026, 10, 12)),
        )

    @Test fun `day 31 comes back after february`() {
        val rent = charge(Frequency.MONTHLY, LocalDate.of(2026, 1, 31))
        assertEquals(LocalDate.of(2026, 2, 28), rent.nextDueOnOrAfter(LocalDate.of(2026, 2, 10)))
        assertEquals(LocalDate.of(2026, 3, 31), rent.nextDueOnOrAfter(LocalDate.of(2026, 3, 1)))
    }

    @Test fun `a quarterly charge saved months ago still counts in its cycle`() {
        val cycle = BudgetCycle.containing(LocalDate.of(2026, 10, 12), 25) // 25 sept. → 24 oct.
        val water = charge(Frequency.QUARTERLY, LocalDate.of(2026, 4, 15)) // 15 avr. → 15 juil. → 15 oct.
        assertTrue(water.isDueIn(cycle))
        assertEquals(Money.euros(788), (charges.filter { it.frequency == Frequency.MONTHLY } + water).totalFor(cycle))
    }

    @Test fun `not due in the cycles between two due dates`() {
        val november = BudgetCycle.containing(LocalDate.of(2026, 11, 12), 25)
        assertFalse(charge(Frequency.QUARTERLY, LocalDate.of(2026, 4, 15)).isDueIn(november))
    }
}