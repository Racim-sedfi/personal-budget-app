package com.application.personal_budget_app.domain.onboarding

enum class OnboardingStep {
    PRIVACY, CYCLE_START, START_CHOICE;

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