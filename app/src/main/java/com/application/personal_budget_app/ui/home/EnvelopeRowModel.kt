package com.application.personal_budget_app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.budget.EnvelopeStatus
import com.application.personal_budget_app.domain.budget.envelopeStatus
import com.application.personal_budget_app.domain.budget.progressFraction
import com.application.personal_budget_app.domain.budget.usedPercent
import com.application.personal_budget_app.domain.home.EnvelopeLine
import com.application.personal_budget_app.domain.home.HomeSummary
import com.application.personal_budget_app.domain.model.Money
import com.application.personal_budget_app.ui.theme.*

data class EnvelopeRowModel(
    val id: Long,
    val name: String,
    val iconKey: String,
    val amountText: String,
    val amountColor: Color,
    val fraction: Float,
    val barColor: Color,
    val statusText: String? = null,
    @DrawableRes val statusIcon: Int? = null,
    val statusColor: Color = Ink,
)

fun EnvelopeLine.toRowModel(summary: HomeSummary): EnvelopeRowModel {
    val cap = category.cap

    // Observation (ou enveloppe sans plafond) : pas de « prévu », on compare à la plus grosse catégorie.
    if (summary.snapshot == null || cap == null) {
        val biggest = Money(summary.envelopes.maxOfOrNull { it.spent.cents } ?: 0L)
        return EnvelopeRowModel(
            id = category.id,
            name = "${category.name} · ${usedPercent(spent, summary.spent)} %",
            iconKey = category.iconKey,
            amountText = spent.format(), amountColor = Ink,
            fraction = progressFraction(spent, biggest), barColor = NearLimit,
        )
    }

    // Budget : dépensé / prévu, barre bornée à 100 %.
    val base = EnvelopeRowModel(
        id = category.id, name = category.name, iconKey = category.iconKey,
        amountText = "${spent.format()} / ${cap.format()}", amountColor = Ink,
        fraction = progressFraction(spent, cap), barColor = CalmBlue,
    )
    return when (envelopeStatus(spent, cap)) {
        EnvelopeStatus.NORMAL -> base
        EnvelopeStatus.NEAR_LIMIT -> base.copy(
            barColor = NearLimit,
            statusText = "Proche de la limite · ${usedPercent(spent, cap)} %",
            statusIcon = R.drawable.ic_status_near, statusColor = NearLimitText,
        )
        EnvelopeStatus.OVER -> base.copy(
            amountColor = OverAmberText, barColor = OverAmber,
            statusText = "Dépassé de ${(spent - cap).format()} · ${usedPercent(spent, cap)} %",
            statusIcon = R.drawable.ic_status_over, statusColor = OverAmberText,
        )
    }
}