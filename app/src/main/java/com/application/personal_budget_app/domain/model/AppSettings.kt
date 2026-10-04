package com.application.personal_budget_app.domain.model

enum class BudgetMode { BUDGET, OBSERVATION }

data class AppSettings(
    val cycleStartDay: Int = 1,
    val mode: BudgetMode = BudgetMode.OBSERVATION,
    val onboardingDone: Boolean = false,
    val lockEnabled: Boolean = false,
    val lockDelayMinutes: Int = 1,   // 0 = immédiatement
    val currency: AppCurrency = AppCurrency.EUR,
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int = 21 * 60,   // heure du rappel, en minutes depuis minuit
)