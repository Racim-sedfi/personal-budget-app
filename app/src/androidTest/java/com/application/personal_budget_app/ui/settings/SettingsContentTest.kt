package com.application.personal_budget_app.ui.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.application.personal_budget_app.domain.model.AppSettings
import com.application.personal_budget_app.ui.theme.PersonalbudgetappTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsContentTest {
    @get:Rule val compose = createComposeRule()

    private var reset = false
    private var reminder: Boolean? = null

    private fun show(settings: AppSettings = AppSettings(onboardingDone = true), lockAvailable: Boolean = true) =
        compose.setContent {
            PersonalbudgetappTheme(darkTheme = false) {
                SettingsContent(
                    settings = settings,
                    lockAvailable = lockAvailable,
                    onBack = {},
                    onToggleLock = {},
                    onDelay = {},
                    onCurrency = {},
                    onReset = { reset = true },
                    onToggleReminder = { reminder = it },
                )
            }
        }

    @Test fun resetAsksForConfirmationFirst() {
        show()
        compose.onNodeWithText("Réinitialiser les données").performScrollTo().performClick()
        compose.onNodeWithText("Tout réinitialiser ?").assertExists()
        assertFalse(reset)                                   // rien n'est effacé avant la confirmation
        compose.onNodeWithText("Tout supprimer").performClick()
        assertTrue(reset)
    }

    @Test fun cancellingTheResetKeepsEverything() {
        show()
        compose.onNodeWithText("Réinitialiser les données").performScrollTo().performClick()
        compose.onNodeWithText("Annuler").performClick()
        compose.onNodeWithText("Tout réinitialiser ?").assertDoesNotExist()
        assertFalse(reset)
    }

    @Test fun reminderSwitchTurnsTheReminderOn() {
        show()
        compose.onNodeWithText("Rappel quotidien").performScrollTo().performClick()
        assertEquals(true, reminder)
    }

    @Test fun reminderTimeIsShownOnceEnabled() {
        show(AppSettings(onboardingDone = true, reminderEnabled = true))
        compose.onNodeWithText("21:00").performScrollTo().assertExists()
    }

    @Test fun lockIsDisabledWithoutPhoneLock() {
        show(lockAvailable = false)
        compose.onNodeWithText("Verrouillage").assertIsNotEnabled()
    }
}
