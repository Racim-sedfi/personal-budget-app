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
import androidx.glance.appwidget.LinearProgressIndicator
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
import androidx.glance.unit.ColorProvider as GlanceColor
import com.application.personal_budget_app.MainActivity
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.budget.EnvelopeStatus
import com.application.personal_budget_app.domain.widget.WidgetData
import com.application.personal_budget_app.ui.format.shortFr

// Couleurs jour / nuit : le widget suit le thème du téléphone (le fond, lui, vient de drawable / drawable-night).
private fun dayNight(day: Long, night: Long) = ColorProvider(day = Color(day), night = Color(night))
private val OnGradient = dayNight(0xFF22364A, 0xFFC9D6E6)
private val Ink = dayNight(0xFF1C1C1C, 0xFFECEFF3)
private val Secondary = dayNight(0xFF3E444B, 0xFFC7CDD5)
private val CalmBlue = dayNight(0xFF1F3550, 0xFFA8C5E6)
private val NearLimit = dayNight(0xFF3A3270, 0xFFC4C0F2)
private val Amber = dayNight(0xFF6B4510, 0xFFF0C985)
private val BarCalm = dayNight(0xFF4F7FB0, 0xFF7FA9D6)
private val BarNear = dayNight(0xFF5B55B8, 0xFF9B96E0)
private val BarOver = dayNight(0xFFD39A3A, 0xFFE0B25E)
private val BarTrack = dayNight(0xCCFFFFFF, 0x33FFFFFF)
private val ButtonBg = dayNight(0xFF1C1C1C, 0xFFECEFF3)
private val ButtonContent = dayNight(0xFFFFFFFF, 0xFF121417)

// Tailles de référence : carré 2×2, bandeau 4×1, grand 4×2.
private val SMALL = DpSize(110.dp, 110.dp)
private val STRIP = DpSize(250.dp, 60.dp)
private val LARGE = DpSize(250.dp, 110.dp)

class BudgetWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, STRIP, LARGE))

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
    val size = LocalSize.current
    val wide = size.width >= LARGE.width
    val tall = size.height >= LARGE.height
    val background = if (data is WidgetData.Spent) R.drawable.widget_bg_observation else R.drawable.widget_bg_budget

    val container = GlanceModifier.fillMaxSize()
        .background(ImageProvider(background))
        .cornerRadius(24.dp)
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .clickable(actionStartActivity<MainActivity>())

    when {
        wide && tall -> Large(data, addIntent, container)
        wide -> Strip(data, addIntent, container)
        else -> Small(data, addIntent, container)
    }
}

// ---------- 4×2 : tout ----------

@Composable
private fun Large(data: WidgetData, addIntent: Intent, modifier: GlanceModifier) {
    Column(modifier) {
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(GlanceModifier.defaultWeight()) {
                Text(title(data), style = TextStyle(color = OnGradient, fontSize = 12.sp, fontWeight = FontWeight.Medium))
                amount(data)?.let { Text(it, style = TextStyle(color = amountColor(data), fontSize = 28.sp, fontWeight = FontWeight.Bold), maxLines = 1) }
            }
            if (data != WidgetData.NotConfigured) AddButton(addIntent, 48)
        }
        Spacer(GlanceModifier.height(8.dp))
        when (data) {
            is WidgetData.Remaining -> {
                Bar(data)
                Spacer(GlanceModifier.height(6.dp))
                Row(GlanceModifier.fillMaxWidth()) {
                    Text(
                        "${data.spent.format(data.currency)} dépensés sur ${data.spendable.format(data.currency)}",
                        style = TextStyle(color = Secondary, fontSize = 12.sp), maxLines = 1,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    Text(statusLabel(data.status), style = TextStyle(color = statusColor(data.status), fontSize = 12.sp, fontWeight = FontWeight.Medium), maxLines = 1)
                }
                Text(
                    listOfNotNull(
                        "${remainingDays(data.daysLeft)} · jusqu'au ${data.cycleEnd.shortFr()}",
                        data.perDay?.let { "${it.format(data.currency)} / jour" },
                    ).joinToString(" · "),
                    style = TextStyle(color = Secondary, fontSize = 12.sp), maxLines = 1,
                )
            }
            is WidgetData.Spent -> {
                Text(
                    "Aujourd'hui : ${data.todaySpent.format(data.currency)}",
                    style = TextStyle(color = Secondary, fontSize = 12.sp), maxLines = 1,
                )
                data.topCategory?.let {
                    Text("Surtout : $it · ${data.topPercent} %", style = TextStyle(color = Secondary, fontSize = 12.sp), maxLines = 1)
                }
                Text("Observation · ${remainingDays(data.daysLeft)}", style = TextStyle(color = NearLimit, fontSize = 12.sp, fontWeight = FontWeight.Medium), maxLines = 1)
            }
            else -> Text(message(data), style = TextStyle(color = Secondary, fontSize = 13.sp), maxLines = 2)
        }
    }
}

// ---------- 4×1 : une ligne ----------

@Composable
private fun Strip(data: WidgetData, addIntent: Intent, modifier: GlanceModifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Column(GlanceModifier.defaultWeight()) {
            Text(
                when (data) {
                    is WidgetData.Remaining -> "${title(data)} · ${data.daysLeft} j"
                    is WidgetData.Spent -> "${title(data)} · ${data.daysLeft} j"
                    else -> title(data)
                },
                style = TextStyle(color = OnGradient, fontSize = 12.sp, fontWeight = FontWeight.Medium), maxLines = 1,
            )
            Text(
                amount(data) ?: message(data),
                style = TextStyle(color = amountColor(data), fontSize = if (amount(data) != null) 22.sp else 13.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
        }
        if (data != WidgetData.NotConfigured) AddButton(addIntent, 44)
    }
}

// ---------- 2×2 : l'essentiel ----------

@Composable
private fun Small(data: WidgetData, addIntent: Intent, modifier: GlanceModifier) {
    Column(modifier) {
        Text(title(data), style = TextStyle(color = OnGradient, fontSize = 12.sp, fontWeight = FontWeight.Medium), maxLines = 1)
        Text(
            amount(data) ?: message(data),
            style = TextStyle(color = amountColor(data), fontSize = if (amount(data) != null) 20.sp else 12.sp, fontWeight = FontWeight.Bold),
            maxLines = 2,
        )
        if (data is WidgetData.Remaining) {
            Spacer(GlanceModifier.height(6.dp))
            Bar(data)
        }
        Spacer(GlanceModifier.defaultWeight())
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                when (data) {
                    is WidgetData.Remaining -> "${data.daysLeft} j restants"
                    is WidgetData.Spent -> "Observation · ${data.daysLeft} j"
                    else -> ""
                },
                style = TextStyle(color = Secondary, fontSize = 11.sp), maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
            )
            if (data != WidgetData.NotConfigured) AddButton(addIntent, 36)
        }
    }
}

// ---------- Morceaux communs ----------

@Composable
private fun Bar(data: WidgetData.Remaining) {
    LinearProgressIndicator(
        progress = data.progress,
        modifier = GlanceModifier.fillMaxWidth().height(6.dp),
        color = when (data.status) {
            EnvelopeStatus.NORMAL -> BarCalm
            EnvelopeStatus.NEAR_LIMIT -> BarNear
            EnvelopeStatus.OVER -> BarOver
        },
        backgroundColor = BarTrack,
    )
}

@Composable
private fun AddButton(addIntent: Intent, sizeDp: Int) {
    Box(
        GlanceModifier.size(sizeDp.dp).background(ButtonBg).cornerRadius((sizeDp / 2).dp)
            .clickable(actionStartActivity(addIntent)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_add),
            contentDescription = "Ajouter une dépense",
            colorFilter = ColorFilter.tint(ButtonContent),
            modifier = GlanceModifier.size((sizeDp / 2).dp),
        )
    }
}

private fun title(data: WidgetData): String = when (data) {
    WidgetData.NotConfigured -> "Budget"
    WidgetData.Locked -> "Budget verrouillé"
    is WidgetData.Remaining -> if (data.isOver) "Budget dépassé de" else "Reste disponible"
    is WidgetData.Spent -> "Dépensé ce cycle"
}

private fun amount(data: WidgetData): String? = when (data) {
    is WidgetData.Remaining -> (if (data.isOver) -data.amount else data.amount).format(data.currency)
    is WidgetData.Spent -> data.amount.format(data.currency)
    else -> null
}

private fun message(data: WidgetData): String = when (data) {
    WidgetData.Locked -> "Ouvre l'app pour voir ton reste."
    else -> "Ouvre l'app pour commencer."
}

private fun amountColor(data: WidgetData): GlanceColor =
    if (data is WidgetData.Remaining && data.isOver) Amber else Ink

private fun statusLabel(status: EnvelopeStatus) = when (status) {
    EnvelopeStatus.NORMAL -> "Dans ton budget"
    EnvelopeStatus.NEAR_LIMIT -> "Proche de la limite"
    EnvelopeStatus.OVER -> "Budget dépassé"
}

private fun statusColor(status: EnvelopeStatus): GlanceColor = when (status) {
    EnvelopeStatus.NORMAL -> CalmBlue
    EnvelopeStatus.NEAR_LIMIT -> NearLimit
    EnvelopeStatus.OVER -> Amber
}

private fun remainingDays(n: Int) = if (n <= 1) "$n jour restant" else "$n jours restants"
