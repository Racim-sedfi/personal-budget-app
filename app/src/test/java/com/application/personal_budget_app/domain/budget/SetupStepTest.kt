package com.application.personal_budget_app.domain.budget

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class SetupStepTest {
    private val empty = BudgetOverview(
        mode = BudgetMode.OBSERVATION,
        cycle = BudgetCycle.containing(LocalDate.of(2026, 10, 12), 1),
        incomes = emptyList(), savings = emptyList(), charges = emptyList(), categories = emptyList(),
    )
    private val withIncome = empty.copy(incomes = listOf(Income(name = "Salaire", amount = Money.euros(1650), dayOfMonth = 25)))

    @Test fun `incomes step needs at least one income`() {
        assertFalse(SetupStep.INCOMES.canContinue(empty))
        assertTrue(SetupStep.INCOMES.canContinue(withIncome))
    }

    @Test fun `charges are optional`() = assertTrue(SetupStep.CHARGES.canContinue(empty))

    @Test fun `activation needs an income`() {
        assertFalse(SetupStep.CAPS.canContinue(empty))
        assertTrue(SetupStep.CAPS.canContinue(withIncome))
    }

    @Test fun `steps stay within bounds`() {
        assertEquals(SetupStep.INCOMES, SetupStep.INCOMES.previous())
        assertEquals(SetupStep.CAPS, SetupStep.CAPS.next())
        assertEquals(3, SetupStep.count)
    }
}