package com.application.personal_budget_app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.application.personal_budget_app.MainActivity
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.widget.WidgetData
import com.application.personal_budget_app.ui.format.shortFr

// Couleurs jour / nuit : le widget suit le thème du téléphone.
private val WidgetBg = ColorProvider(day = Color(0xFFE6EFFA), night = Color(0xFF1B2A3B))
private val LabelColor = ColorProvider(day = Color(0xFF4E5A68), night = Color(0xFFA3AAB4))
private val AmountColor = ColorProvider(day = Color(0xFF1C1C1C), night = Color(0xFFECEFF3))
private val AmberColor = ColorProvider(day = Color(0xFF6B4510), night = Color(0xFFF0C985))
private val ButtonBg = ColorProvider(day = Color(0xFF1C1C1C), night = Color(0xFFECEFF3))
private val ButtonContent = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF121417))

private val SMALL = DpSize(110.dp, 110.dp)
private val WIDE = DpSize(250.dp, 80.dp)

class BudgetWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, WIDE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadWidgetData(context)
        // « + » : ouvre l'app directement sur la saisie rapide.
        val addIntent = Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_OPEN_ENTRY, true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        provideContent { WidgetContent(data, addIntent) }
    }
}

class BudgetWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BudgetWidget()
}

@Composable
private fun WidgetContent(data: WidgetData, addIntent: Intent) {
    val wide = LocalSize.current.width >= WIDE.width
    val (label, amount, detail, amountColor) = texts(data, wide)

    val container = GlanceModifier.fillMaxSize().background(WidgetBg).cornerRadius(24.dp).padding(16.dp)
        .clickable(actionStartActivity<MainActivity>())

    if (wide) {
        Row(container, verticalAlignment = Alignment.CenterVertically) {
            Column(GlanceModifier.defaultWeight()) { Lines(label, amount, detail, amountColor, amountSize = 26) }
            if (data != WidgetData.NotConfigured) AddButton(addIntent)
        }
    } else {
        Column(container) {
            Lines(label, amount, detail, amountColor, amountSize = 22)
            Spacer(GlanceModifier.defaultWeight())
            if (data != WidgetData.NotConfigured) AddButton(addIntent)
        }
    }
}

private data class WidgetTexts(
    val label: String,
    val amount: String?,
    val detail: String,
    val amountColor: androidx.glance.unit.ColorProvider,
)

private fun texts(data: WidgetData, wide: Boolean): WidgetTexts = when (data) {
    WidgetData.NotConfigured -> WidgetTexts("Budget", null, "Ouvre l'app pour commencer.", AmountColor)
    WidgetData.Locked -> WidgetTexts("Budget verrouillé", null, "Ouvre l'app pour voir ton reste.", AmountColor)
    is WidgetData.Remaining -> WidgetTexts(
        label = if (data.isOver) "Budget dépassé de" else "Reste disponible",
        amount = (if (data.isOver) -data.amount else data.amount).format(data.currency),
        detail = if (wide) "${remainingDays(data.daysLeft)} · jusqu'au ${data.cycleEnd.shortFr()}" else "${data.daysLeft} j restants",
        amountColor = if (data.isOver) AmberColor else AmountColor,
    )
    is WidgetData.Spent -> WidgetTexts(
        label = "Dépensé ce cycle",
        amount = data.amount.format(data.currency),
        detail = "Observation · ${data.daysLeft} j",
        amountColor = AmountColor,
    )
}

private fun remainingDays(n: Int) = if (n <= 1) "$n jour restant" else "$n jours restants"

@Composable
private fun Lines(label: String, amount: String?, detail: String, amountColor: androidx.glance.unit.ColorProvider, amountSize: Int) {
    Text(label, style = TextStyle(color = LabelColor, fontSize = 12.sp, fontWeight = FontWeight.Medium))
    if (amount != null) {
        Text(amount, style = TextStyle(color = amountColor, fontSize = amountSize.sp, fontWeight = FontWeight.Bold), maxLines = 1)
    }
    Text(detail, style = TextStyle(color = LabelColor, fontSize = 12.sp), maxLines = 2)
}

@Composable
private fun AddButton(addIntent: Intent) {
    Box(
        GlanceModifier.size(48.dp).background(ButtonBg).cornerRadius(24.dp).clickable(actionStartActivity(addIntent)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_add),
            contentDescription = "Ajouter une dépense",
            colorFilter = ColorFilter.tint(ButtonContent),
            modifier = GlanceModifier.size(22.dp),
        )
    }
}
