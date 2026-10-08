package com.application.personal_budget_app.ui.entry

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.application.personal_budget_app.domain.entry.AmountInput
import com.application.personal_budget_app.domain.entry.KeypadKey
import com.application.personal_budget_app.domain.model.Category
import com.application.personal_budget_app.domain.model.TransactionType
import com.application.personal_budget_app.ui.theme.PersonalbudgetappTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** Saisie rapide : on teste l'écran seul (sans ViewModel), avec des états préparés. */
@RunWith(AndroidJUnit4::class)
class QuickEntryContentTest {
    @get:Rule val compose = createComposeRule()

    private val categories = listOf(Category(1, "Courses", "cart"), Category(2, "Loisirs", "ticket"))
    private val emptyState = QuickEntryState(date = LocalDate.now(), categories = categories, selectedCategoryId = 1)

    private val keys = mutableListOf<KeypadKey>()
    private var saved = false
    private var selectedCategory: Long? = null

    private fun show(state: QuickEntryState) = compose.setContent {
        PersonalbudgetappTheme(darkTheme = false) {
            QuickEntryContent(
                state = state,
                onKey = { keys += it },
                onTypeChange = {},
                onCategorySelected = { selectedCategory = it },
                onNoteChange = {},
                onOpenDetails = {},
                onSave = { saved = true },
                onClose = {},
            )
        }
    }

    @Test fun saveIsDisabledWithoutAmount() {
        show(emptyState)
        compose.onNodeWithText("Enregistrer").assertIsNotEnabled()
    }

    @Test fun saveWorksOnceAnAmountIsTyped() {
        show(emptyState.copy(amount = AmountInput("8,4")))
        compose.onNodeWithText("Enregistrer").assertIsEnabled().performClick()
        assertTrue(saved)
    }

    @Test fun keypadSendsDigitsCommaAndDelete() {
        show(emptyState)
        compose.onNodeWithText("7").performClick()
        compose.onNodeWithContentDescription("Virgule").performClick()
        compose.onNodeWithContentDescription("Effacer").performClick()
        assertEquals(listOf(KeypadKey.Digit(7), KeypadKey.Comma, KeypadKey.Delete), keys)
    }

    @Test fun tappingAnEnvelopeSelectsIt() {
        show(emptyState)
        compose.onNodeWithText("Loisirs").performClick()
        assertEquals(2L, selectedCategory)
    }

    @Test fun incomeHidesEnvelopesAndAsksForALabel() {
        show(emptyState.copy(type = TransactionType.INCOME))
        compose.onNodeWithText("Courses").assertDoesNotExist()
        compose.onNodeWithText("D'où vient ce revenu ?").assertExists()
    }

    @Test fun dateChipOpensTheCalendar() {
        show(emptyState)
        compose.onNodeWithText("Aujourd'hui", substring = true).performClick()
        compose.onNodeWithText("Valider").assertExists()
    }
}
