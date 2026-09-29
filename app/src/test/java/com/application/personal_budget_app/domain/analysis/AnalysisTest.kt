package com.application.personal_budget_app.domain.analysis

import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.ui.analysis.insights
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class AnalysisTest {
    private val today = LocalDate.of(2026, 10, 12)
    private val current = BudgetCycle.containing(today, 25)   // 25 sept. → 24 oct.
    private val previous = current.shifted(-1)                // 25 août → 24 sept.
    private val twoAgo = current.shifted(-2)                  // 25 juil. → 24 août

    private val categories = listOf(
        Category(1, "Courses", "cart", Money.euros(250)),
        Category(2, "Restaurants", "restaurant"),
        Category(4, "Loisirs", "ticket", Money.euros(60)),
    )

    private fun tx(cat: Long?, euros: Long, date: LocalDate, type: TransactionType = TransactionType.EXPENSE) =
        Transaction(amount = Money.euros(euros), type = type, categoryId = cat, date = date)

    private val transactions = listOf(
        tx(1, 200, LocalDate.of(2026, 7, 1)),                                        // juin-juil. : peu renseigné
        tx(1, 300, LocalDate.of(2026, 8, 1)), tx(4, 120, LocalDate.of(2026, 8, 2)),  // juil.-août
        tx(1, 250, LocalDate.of(2026, 9, 1)), tx(4, 80, LocalDate.of(2026, 9, 2)),   // août-sept.
        tx(1, 100, LocalDate.of(2026, 10, 1)), tx(4, 50, LocalDate.of(2026, 10, 2)), tx(2, 50, LocalDate.of(2026, 10, 3)),
        tx(null, 500, LocalDate.of(2026, 10, 4), TransactionType.INCOME),           // un revenu n'est pas une dépense
    )

    // Deux cycles entièrement renseignés, donc fiables.
    private val fullDays = previous.days().toSet() + twoAgo.days().toSet()

    private fun analysis(tx: List<Transaction> = transactions, noExpense: Set<LocalDate> = fullDays) = buildAnalysis(
        today, 25, tx, noExpense, categories,
        incomes = listOf(Income(name = "Salaire", amount = Money.euros(1850), dayOfMonth = 25)),
        charges = listOf(FixedCharge(name = "Loyer", amount = Money.euros(743), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 10, 25))),
    )

    @Test fun `cycles start at the first one with data`() {
        val cycles = analysis().cycles
        assertEquals(4, cycles.size)
        assertTrue(cycles.last().isCurrent)
        assertEquals(listOf(200L, 420L, 330L, 200L).map { Money.euros(it) }, cycles.map { it.spent })
    }

    @Test fun `only well filled past cycles are reliable`() {
        val a = analysis()
        assertEquals(listOf(twoAgo, previous), a.reliableCycles.map { it.cycle })
        assertTrue(a.hasAverages)
    }

    @Test fun `averages use reliable cycles only`() {
        val a = analysis()
        assertEquals(Money.euros(275), a.averages.first { it.category.id == 1L }.average)
        assertEquals(Money.euros(100), a.averages.first { it.category.id == 4L }.average)
        assertEquals(Money.euros(375), a.averageSpent)
    }

    @Test fun `biggest overrun is the largest gap above cap`() {
        val overrun = analysis().biggestOverrun!!
        assertEquals("Loisirs", overrun.category.name)
        assertEquals(Money.euros(40), overrun.overBy)
    }

    @Test fun `current cycle shares ignore incomes`() {
        val a = analysis()
        assertEquals(Money.euros(200), a.spent)
        assertEquals("Courses", a.shares.first().category.name)
        assertEquals(mapOf("Courses" to 50, "Restaurants" to 25, "Loisirs" to 25), a.shares.associate { it.category.name to it.percent })
    }

    @Test fun `fixed charges share of income`() = assertEquals(40, analysis().fixedChargesPercent)

    @Test fun `no data shows only the current cycle and no averages`() {
        val a = analysis(tx = emptyList(), noExpense = emptySet())
        assertEquals(1, a.cycles.size)
        assertFalse(a.hasAverages)
        assertTrue(a.shares.isEmpty())
    }

    @Test fun `insights sum up the analysis`() {
        val lines = analysis().insights()
        assertEquals(3, lines.size)
        assertTrue(lines[0].startsWith("En moyenne, Loisirs dépasse son plafond"))
        assertTrue(lines[1].contains("de plus que tes plafonds"))   // 375 € en moyenne pour 310 € de plafonds
        assertEquals("Tes charges fixes représentent 40 % de tes revenus.", lines[2])
    }

    @Test fun `no average insights without enough reliable cycles`() {
        val lines = analysis(noExpense = emptySet()).insights()
        assertEquals(listOf("Tes charges fixes représentent 40 % de tes revenus."), lines)
    }
}