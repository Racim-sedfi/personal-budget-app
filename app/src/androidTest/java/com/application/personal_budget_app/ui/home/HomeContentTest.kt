package com.application.personal_budget_app.ui.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.application.personal_budget_app.domain.home.HomeSummary
import com.application.personal_budget_app.domain.home.buildHomeSummary
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.ui.theme.PersonalbudgetappTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class HomeContentTest {
    @get:Rule val compose = createComposeRule()

    private val today = LocalDate.of(2026, 10, 12)
    private var closeAsked = false

    private fun summary(mode: BudgetMode) = buildHomeSummary(
        today,
        AppSettings(cycleStartDay = 25, mode = mode, onboardingDone = true),
        listOf(Transaction(amount = Money.euros(100), type = TransactionType.EXPENSE, categoryId = 1, date = LocalDate.of(2026, 10, 1))),
        emptySet(),
        listOf(Category(1, "Courses", "cart", Money.euros(250))),
        incomes = listOf(Income(name = "Salaire", amount = Money.euros(1000), dayOfMonth = 25)),
        savings = emptyList(),
        charges = emptyList(),
    )

    private fun show(summary: HomeSummary) = compose.setContent {
        PersonalbudgetappTheme(darkTheme = false) {
            HomeContent(summary, onAddClick = {}, onCompleteDays = {}, onCloseCycle = { closeAsked = true })
        }
    }

    @Test fun budgetModeShowsWhatIsLeft() {
        show(summary(BudgetMode.BUDGET))
        compose.onNodeWithText("Il te reste").assertExists()
        compose.onNodeWithText("Dans ton budget").assertExists()
    }

    @Test fun observationModeShowsWhatWasSpent() {
        show(summary(BudgetMode.OBSERVATION))
        compose.onNodeWithText("Tu as dépensé").assertExists()
        compose.onNodeWithText("Il te reste").assertDoesNotExist()
    }

    @Test fun endedCycleOffersTheReview() {
        val s = summary(BudgetMode.BUDGET)
        show(s.copy(cycleToClose = s.cycle.shifted(-1)))
        compose.onNodeWithText("Faire le bilan").performClick()
        assertTrue(closeAsked)
    }
}
