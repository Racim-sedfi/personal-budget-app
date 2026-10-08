package com.application.personal_budget_app.ui.onboarding

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.onboarding.OnboardingStep
import com.application.personal_budget_app.ui.theme.PersonalbudgetappTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class OnboardingContentTest {
    @get:Rule val compose = createComposeRule()

    private val cycle = BudgetCycle.containing(LocalDate.of(2026, 10, 12), 1)
    private var next = false
    private var finished = false
    private var lockAsked = false
    private val toggled = mutableListOf<String>()

    private fun show(step: OnboardingStep, lockAvailable: Boolean = true) = compose.setContent {
        PersonalbudgetappTheme(darkTheme = false) {
            OnboardingContent(
                state = OnboardingUiState(step = step, cycle = cycle),
                onBack = {},
                onNext = { next = true },
                onPickDay = {},
                onPickChoice = {},
                onFinish = { finished = true },
                lockAvailable = lockAvailable,
                onEnableLock = { lockAsked = true },
                onToggleEnvelope = { toggled += it },
            )
        }
    }

    @Test fun firstStepHasNoBackButtonAndGoesForward() {
        show(OnboardingStep.PRIVACY)
        compose.onNodeWithText("Tes données restent sur ton téléphone.").assertExists()
        compose.onNodeWithContentDescription("Retour").assertDoesNotExist()
        compose.onNodeWithText("Continuer").performClick()
        assertTrue(next)
    }

    @Test fun envelopesStepKeepsTheMinimumAndTogglesTheOthers() {
        show(OnboardingStep.ENVELOPES)
        compose.onNodeWithText("Courses").assertIsNotEnabled()     // toujours incluse
        compose.onNodeWithText("Imprévus").assertIsNotEnabled()
        compose.onNodeWithText("Loisirs").performClick()
        assertEquals(listOf("Loisirs"), toggled)
    }

    @Test fun lockStepOffersBothChoicesWhenThePhoneIsLocked() {
        show(OnboardingStep.LOCK, lockAvailable = true)
        compose.onNodeWithText("Activer le verrouillage").performClick()
        assertTrue(lockAsked)
        compose.onNodeWithText("Plus tard").performClick()
        assertTrue(finished)
    }

    @Test fun lockStepOnlyOffersStartWithoutPhoneLock() {
        show(OnboardingStep.LOCK, lockAvailable = false)
        compose.onNodeWithText("Activer le verrouillage").assertDoesNotExist()
        compose.onNodeWithText("Commencer").performClick()
        assertTrue(finished)
    }
}
