package com.application.personal_budget_app.domain.budget

import com.application.personal_budget_app.domain.model.Money

enum class EnvelopeStatus { NORMAL, NEAR_LIMIT, OVER }

/** Seuil « proche de la limite », en pourcentage du plafond. */
const val NEAR_LIMIT_PERCENT = 70

fun envelopeStatus(spent: Money, cap: Money): EnvelopeStatus = when {
    spent > cap -> EnvelopeStatus.OVER
    cap.cents > 0 && spent.cents * 100 >= cap.cents * NEAR_LIMIT_PERCENT -> EnvelopeStatus.NEAR_LIMIT
    else -> EnvelopeStatus.NORMAL
}