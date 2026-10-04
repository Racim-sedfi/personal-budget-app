package com.application.personal_budget_app.domain.widget

import com.application.personal_budget_app.domain.home.HomeSummary
import com.application.personal_budget_app.domain.model.AppCurrency
import com.application.personal_budget_app.domain.model.AppSettings
import com.application.personal_budget_app.domain.model.Money
import java.time.LocalDate

/** Ce que le widget d'écran d'accueil affiche. */
sealed interface WidgetData {
    /** Onboarding pas encore fait. */
    data object NotConfigured : WidgetData

    /** Verrouillage actif : on n'affiche aucun montant sur l'écran d'accueil. */
    data object Locked : WidgetData

    /** Mode budget : « Reste disponible ». */
    data class Remaining(val amount: Money, val currency: AppCurrency, val daysLeft: Int, val cycleEnd: LocalDate) : WidgetData {
        val isOver: Boolean get() = amount.isNegative
    }

    /** Mode observation : « Dépensé ce cycle ». */
    data class Spent(val amount: Money, val currency: AppCurrency, val daysLeft: Int) : WidgetData
}

fun widgetData(settings: AppSettings, summary: HomeSummary?): WidgetData = when {
    !settings.onboardingDone || summary == null -> WidgetData.NotConfigured
    settings.lockEnabled -> WidgetData.Locked
    else -> summary.snapshot
        ?.let { WidgetData.Remaining(it.remaining, settings.currency, summary.daysRemaining, summary.cycle.end) }
        ?: WidgetData.Spent(summary.spent, settings.currency, summary.daysRemaining)
}
