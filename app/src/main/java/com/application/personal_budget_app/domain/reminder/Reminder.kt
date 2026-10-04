package com.application.personal_budget_app.domain.reminder

import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

/** Heure par défaut du rappel : 21 h, en minutes depuis minuit. */
const val DEFAULT_REMINDER_MINUTES = 21 * 60

/** Temps à attendre jusqu'au prochain `at` : aujourd'hui s'il n'est pas passé, sinon demain. */
fun delayUntilNext(now: LocalDateTime, at: LocalTime): Duration {
    var next = now.toLocalDate().atTime(at)
    if (!next.isAfter(now)) next = next.plusDays(1)
    return Duration.between(now, next)
}

/** On ne dérange que si le rappel est actif, l'app configurée, et la journée encore vide. */
fun shouldRemind(enabled: Boolean, onboardingDone: Boolean, dayFilled: Boolean): Boolean =
    enabled && onboardingDone && !dayFilled

/** 1260 → "21:00" */
fun formatMinutesOfDay(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)
