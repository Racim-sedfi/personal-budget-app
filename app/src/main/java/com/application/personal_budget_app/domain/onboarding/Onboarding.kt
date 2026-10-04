package com.application.personal_budget_app.domain.onboarding

enum class OnboardingStep {
    PRIVACY, CYCLE_START, ENVELOPES, START_CHOICE, LOCK;

    val number: Int get() = ordinal + 1
    val isFirst: Boolean get() = ordinal == 0
    val isLast: Boolean get() = ordinal == entries.lastIndex

    fun next(): OnboardingStep = entries[minOf(ordinal + 1, entries.lastIndex)]
    fun previous(): OnboardingStep = entries[maxOf(ordinal - 1, 0)]

    companion object {
        val count: Int get() = entries.size
    }
}

enum class StartChoice { CONFIGURE, OBSERVE }

/** Une enveloppe proposée à l'onboarding. */
data class EnvelopePreset(val name: String, val iconKey: String)

/**
 * Proposées en plus de Courses et Imprévus (toujours présentes).
 * Pas de Transports : un abonnement ou l'essence se suivent mieux en charge fixe.
 */
val OPTIONAL_ENVELOPES = listOf(
    EnvelopePreset("Restaurants", "restaurant"),
    EnvelopePreset("Loisirs", "ticket"),
    EnvelopePreset("Shopping", "bag"),
    EnvelopePreset("Santé", "health"),
)