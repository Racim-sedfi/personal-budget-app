package com.application.personal_budget_app.domain.onboarding

import com.application.personal_budget_app.ui.format.cycleRule
import org.junit.Assert.*
import org.junit.Test

class OnboardingStepTest {
    @Test fun `going back stops at the first step`() {
        assertEquals(OnboardingStep.PRIVACY, OnboardingStep.PRIVACY.previous())
        assertTrue(OnboardingStep.PRIVACY.isFirst)
    }

    @Test fun `steps go forward and stop at the last one`() {
        assertEquals(OnboardingStep.CYCLE_START, OnboardingStep.PRIVACY.next())
        assertEquals(OnboardingStep.LOCK, OnboardingStep.START_CHOICE.next())
        assertEquals(OnboardingStep.LOCK, OnboardingStep.LOCK.next())
        assertTrue(OnboardingStep.LOCK.isLast)
    }

    @Test fun `numbers start at one`() {
        assertEquals(1, OnboardingStep.PRIVACY.number)
        assertEquals(4, OnboardingStep.count)
    }

    @Test fun `cycle rule reads naturally`() {
        assertEquals("Du 1er au dernier jour du mois", cycleRule(1))
        assertEquals("Du 2 au 1er du mois suivant", cycleRule(2))
        assertEquals("Du 25 au 24 du mois suivant", cycleRule(25))
    }

}