package com.application.personal_budget_app.domain.widget

import com.application.personal_budget_app.domain.budget.EnvelopeStatus
import com.application.personal_budget_app.domain.budget.progressFraction
import com.application.personal_budget_app.domain.home.HomeSummary
import com.application.personal_budget_app.domain.model.AppCurrency
import com.application.personal_budget_app.domain.model.AppSettings
import com.application.personal_budget_app.domain.model.Money
import java.time.LocalDate
import kotlin.math.roundToInt

/** Ce que le widget d'écran d'accueil affiche. */
sealed interface WidgetData {
    /** Onboarding pas encore fait. */
    data object NotConfigured : WidgetData

    /** Verrouillage actif : on n'affiche aucun montant sur l'écran d'accueil. */
    data object Locked : WidgetData

    /** Mode budget : « Reste disponible ». */
    data class Remaining(
        val amount: Money,
        val currency: AppCurrency,
        val daysLeft: Int,
        val cycleEnd: LocalDate,
        val spent: Money,
        val spendable: Money,
        val status: EnvelopeStatus,
        val todaySpent: Money,
    ) : WidgetData {
        val isOver: Boolean get() = amount.isNegative
        /** Remplissage de la barre (plafonné à 1). */
        val progress: Float get() = progressFraction(spent, spendable)
        /** Ce qu'on peut dépenser par jour d'ici la fin du cycle (aujourd'hui compris). */
        val perDay: Money? get() = if (isOver) null else Money(amount.cents / (daysLeft + 1))
    }

    /** Mode observation : « Dépensé ce cycle ». */
    data class Spent(
        val amount: Money,
        val currency: AppCurrency,
        val daysLeft: Int,
        val todaySpent: Money,
        val topCategory: String?,
        val topPercent: Int,
    ) : WidgetData
}

fun widgetData(settings: AppSettings, summary: HomeSummary?, todaySpent: Money = Money.ZERO): WidgetData {
    if (!settings.onboardingDone || summary == null) return WidgetData.NotConfigured
    if (settings.lockEnabled) return WidgetData.Locked
    val snapshot = summary.snapshot
    if (snapshot != null) {
        return WidgetData.Remaining(
            amount = snapshot.remaining,
            currency = settings.currency,
            daysLeft = summary.daysRemaining,
            cycleEnd = summary.cycle.end,
            spent = summary.spent,
            spendable = snapshot.spendable,
            status = summary.overallStatus ?: EnvelopeStatus.NORMAL,
            todaySpent = todaySpent,
        )
    }
    val top = summary.envelopes.filter { it.spent.cents > 0 }.maxByOrNull { it.spent.cents }
    return WidgetData.Spent(
        amount = summary.spent,
        currency = settings.currency,
        daysLeft = summary.daysRemaining,
        todaySpent = todaySpent,
        topCategory = top?.category?.name,
        topPercent = if (top == null || summary.spent.cents <= 0) 0 else (top.spent.cents * 100.0 / summary.spent.cents).roundToInt(),
    )
}
