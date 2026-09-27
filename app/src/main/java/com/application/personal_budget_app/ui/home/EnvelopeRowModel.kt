package com.application.personal_budget_app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.budget.EnvelopeStatus
import com.application.personal_budget_app.domain.budget.envelopeStatus
import com.application.personal_budget_app.domain.home.EnvelopeLine
import com.application.personal_budget_app.domain.home.HomeSummary
import com.application.personal_budget_app.ui.theme.*
import kotlin.math.roundToInt

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

    // Observation (ou enveloppe sans plafond) : montant dépensé et part du total.
    if (summary.snapshot == null || cap == null) {
        val max = summary.envelopes.maxOfOrNull { it.spent.cents } ?: 0L
        val share = if (summary.spent.cents > 0) (spent.cents * 100.0 / summary.spent.cents).roundToInt() else 0
        return EnvelopeRowModel(
            id = category.id, name = "${category.name} · $share %", iconKey = category.iconKey,
            amountText = spent.format(), amountColor = Ink,
            fraction = if (max > 0) spent.cents.toFloat() / max else 0f, barColor = NearLimit,
        )
    }

    val fraction = if (cap.cents > 0) (spent.cents.toFloat() / cap.cents).coerceIn(0f, 1f) else 1f
    val percent = if (cap.cents > 0) (spent.cents * 100.0 / cap.cents).roundToInt() else 100
    val base = EnvelopeRowModel(
        id = category.id, name = category.name, iconKey = category.iconKey,
        amountText = "${(cap - spent).format()} restants", amountColor = Ink,
        fraction = fraction, barColor = CalmBlue,
    )
    return when (envelopeStatus(spent, cap)) {
        EnvelopeStatus.NORMAL -> base
        EnvelopeStatus.NEAR_LIMIT -> base.copy(
            barColor = NearLimit,
            statusText = "Proche de la limite · $percent %",
            statusIcon = R.drawable.ic_status_near, statusColor = NearLimitText,
        )
        EnvelopeStatus.OVER -> base.copy(
            amountText = "${(spent - cap).format()} de trop", amountColor = OverAmberText,
            fraction = 1f, barColor = OverAmber,
            statusText = "Plafond dépassé · ${spent.format()} sur ${cap.format()}",
            statusIcon = R.drawable.ic_status_over, statusColor = OverAmberText,
        )
    }
}