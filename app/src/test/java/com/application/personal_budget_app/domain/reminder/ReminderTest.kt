package com.application.personal_budget_app.domain.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderTest {
    private val nine = LocalTime.of(21, 0)

    @Test fun `waits until tonight when it is not past yet`() =
        assertEquals(Duration.ofHours(3), delayUntilNext(LocalDateTime.of(2026, 10, 4, 18, 0), nine))

    @Test fun `waits until tomorrow once passed`() =
        assertEquals(Duration.ofHours(23), delayUntilNext(LocalDateTime.of(2026, 10, 4, 22, 0), nine))

    @Test fun `exactly at reminder time means tomorrow`() =
        assertEquals(Duration.ofDays(1), delayUntilNext(LocalDateTime.of(2026, 10, 4, 21, 0), nine))

    @Test fun `reminds only when the day is still empty`() {
        assertTrue(shouldRemind(enabled = true, onboardingDone = true, dayFilled = false))
        assertFalse(shouldRemind(enabled = true, onboardingDone = true, dayFilled = true))
        assertFalse(shouldRemind(enabled = false, onboardingDone = true, dayFilled = false))
        assertFalse(shouldRemind(enabled = true, onboardingDone = false, dayFilled = false))
    }

    @Test fun `formats time of day`() {
        assertEquals("21:00", formatMinutesOfDay(DEFAULT_REMINDER_MINUTES))
        assertEquals("07:05", formatMinutesOfDay(7 * 60 + 5))
    }
}
