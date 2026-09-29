package com.application.personal_budget_app.ui.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.domain.analysis.*
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.ui.components.BudgetProgressBar
import com.application.personal_budget_app.ui.format.label
import com.application.personal_budget_app.ui.format.monthShortFr
import com.application.personal_budget_app.ui.theme.*
import java.time.LocalDate
import com.application.personal_budget_app.ui.format.CurrencyState

/** Palette de l'anneau : bleus et violets, jamais de rouge ni de vert. */
private val ShareColors = listOf(
    Color(0xFF1F3550), Color(0xFF4F7FB0), Color(0xFF5B55B8), Color(0xFF7FA3C6),
    Color(0xFF3A3270), Color(0xFF9B96E0), Color(0xFF8A929C),
)
private fun shareColor(index: Int) = ShareColors[index % ShareColors.size]

private val UnreliableBar = Color(0xFFC9D0D9)

@Composable
fun AnalysisScreen(
    onOpenHistory: () -> Unit,
    onOpenBudget: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val analysis = state
    if (analysis == null) {
        Box(Modifier.fillMaxSize())
    } else {
        AnalysisContent(analysis, onOpenHistory, onOpenBudget)
    }
}

@Composable
fun AnalysisContent(a: Analysis, onOpenHistory: () -> Unit, onOpenBudget: () -> Unit) {
    val side = Modifier.padding(horizontal = 16.dp)
    val insights = a.insights()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { AnalysisHeader(a) }
        item { SharesCard(a, side) }
        if (a.cycles.size > 1) item { EvolutionCard(a, side) }
        item { if (a.hasAverages) AveragesCard(a, side) else AveragesComingCard(a, onOpenHistory, side) }
        if (insights.isNotEmpty()) {
            item { InsightsCard(insights, showBudgetButton = a.capsTotal.cents > 0, onOpenBudget, side) }
        }
    }
}

@Composable
private fun AnalysisHeader(a: Analysis) {
    Column(
        Modifier.fillMaxWidth().background(BudgetGradients.header).statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "Analyse", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            "Cycle en cours ${a.cycle.label()} · jour ${a.cycle.dayNumber(a.today)} sur ${a.cycle.lengthInDays}",
            style = MaterialTheme.typography.bodyMedium, color = OnGradient,
        )
    }
}

@Composable
private fun AnalysisCard(
    title: String,
    subtitle: String?,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.fillMaxWidth().clip(shape).background(Background).border(1.dp, Divider, shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
        }
        content()
    }
}

// ---------- Où va ton argent ----------

@Composable
private fun SharesCard(a: Analysis, modifier: Modifier) {
    AnalysisCard("Où va ton argent", "Dépenses de ce cycle, par catégorie", modifier) {
        if (a.shares.isEmpty()) {
            Text("Aucune dépense ce cycle pour l'instant.", style = MaterialTheme.typography.bodyMedium, color = TextStrong)
            return@AnalysisCard
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                Donut(a.shares.map { it.spent.cents.toFloat() }, Modifier.fillMaxSize())
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics(mergeDescendants = true) {},
                ) {
                    Text(a.spent.format(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("à J${a.cycle.dayNumber(a.today)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                a.shares.forEachIndexed { index, share ->
                    Row(
                        Modifier.semantics(mergeDescendants = true) {},
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(shareColor(index)))
                        Spacer(Modifier.width(8.dp))
                        Text(share.category.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text("${share.percent} %", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/** Anneau : un arc par catégorie, avec un petit espace entre chaque. */
@Composable
private fun Donut(values: List<Float>, modifier: Modifier) {
    val total = values.sum()
    Canvas(modifier) {
        val stroke = 18.dp.toPx()
        val gap = if (values.size > 1) 2f else 0f   // degrés
        val arcSize = Size(size.width - stroke, size.height - stroke)
        var start = -90f                              // on commence en haut
        values.forEachIndexed { index, value ->
            val sweep = value / total * 360f
            drawArc(
                color = shareColor(index),
                startAngle = start + gap / 2,
                sweepAngle = (sweep - gap).coerceAtLeast(0.5f),
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = arcSize,
                style = Stroke(stroke),
            )
            start += sweep
        }
    }
}

// ---------- Évolution ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EvolutionCard(a: Analysis, modifier: Modifier) {
    val barMax = 120.dp
    val maxValue = maxOf(a.cycles.maxOf { it.spent.cents }, a.capsTotal.cents, 1L).toFloat()
    val spoken = a.cycles.joinToString(", ") { stat ->
        val status = when {
            stat.isCurrent -> "en cours"
            !stat.isReliable -> "renseigné à ${stat.completion.percent} %"
            else -> ""
        }
        "${stat.cycle.start.monthShortFr()} ${stat.spent.format()} $status".trim()
    }

    AnalysisCard("Évolution sur ${a.cycles.size} cycles", "Dépenses par cycle, en ${CurrencyState.current.symbol}", modifier) {
        Box(
            Modifier.fillMaxWidth().height(barMax + 20.dp)
                .clearAndSetSemantics { contentDescription = "Dépenses par cycle : $spoken" },
        ) {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                a.cycles.forEach { stat ->
                    val fraction = (stat.spent.cents.coerceAtLeast(0) / maxValue).coerceIn(0f, 1f)
                    val color = when {
                        stat.isCurrent -> CalmBlue.copy(alpha = 0.45f)
                        stat.isReliable -> CalmBlue
                        else -> UnreliableBar
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${stat.spent.cents / 100}", style = MaterialTheme.typography.labelSmall, color = TextStrong)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            Modifier.fillMaxWidth(0.7f).height(barMax * fraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(color),
                        )
                    }
                }
            }
            if (a.capsTotal.cents > 0) {
                val capFraction = (a.capsTotal.cents / maxValue).coerceIn(0f, 1f)
                Canvas(Modifier.matchParentSize()) {
                    val y = size.height - capFraction * barMax.toPx()
                    drawLine(
                        color = TextStrong,
                        start = Offset(0f, y), end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                    )
                }
            }
        }

        // Mois, et état du cycle sous chaque barre.
        Row(Modifier.fillMaxWidth().clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            a.cycles.forEach { stat ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stat.cycle.start.monthShortFr(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        when {
                            stat.isCurrent -> "en cours"
                            !stat.isReliable -> "${stat.completion.percent} %"
                            else -> " "
                        },
                        style = MaterialTheme.typography.labelSmall, color = TextSecondary,
                    )
                }
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LegendItem(CalmBlue, "Renseigné ≥ 80 %")
            LegendItem(UnreliableBar, "Renseigné < 80 %")
            if (a.capsTotal.cents > 0) LegendItem(TextStrong, "Plafonds ${a.capsTotal.format()}")
        }

        val excluded = a.cycles.filter { !it.isCurrent && !it.isReliable }
        if (excluded.isNotEmpty()) {
            Text(
                excluded.joinToString(", ") { "${it.cycle.start.monthShortFr()} (${it.completion.percent} %)" } +
                        " : renseigné à moins de 80 %, exclu des moyennes.",
                style = MaterialTheme.typography.bodySmall, color = TextStrong,
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextStrong)
    }
}

// ---------- Moyennes ----------

@Composable
private fun AveragesCard(a: Analysis, modifier: Modifier) {
    AnalysisCard(
        "Moyenne par cycle et plafond",
        "${a.reliableCycles.size} cycles renseignés à plus de 80 %",
        modifier,
    ) {
        a.averages.forEach { AverageRow(it) }
        if (a.averages.any { it.category.cap != null }) {
            Text("Le trait marque le plafond de chaque enveloppe.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun AverageRow(avg: EnvelopeAverage) {
    val cap = avg.category.cap
    val over = avg.overBy
    Column(Modifier.semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(avg.category.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(
                if (cap != null) "${avg.average.format()} / ${cap.format()}" else avg.average.format(),
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold,
            )
        }
        if (cap != null) {
            // Le plafond est au milieu de la barre : on voit jusqu'à 2× le plafond.
            val fraction = (avg.average.cents / (cap.cents * 2f)).coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Track)) {
                Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(if (over != null) OverAmber else CalmBlue))
                Box(Modifier.fillMaxWidth(0.5f).fillMaxHeight()) {
                    Box(Modifier.align(Alignment.CenterEnd).width(2.dp).fillMaxHeight().background(Ink))
                }
            }
            over?.let {
                Text("${it.format()} au-dessus du plafond en moyenne", style = MaterialTheme.typography.bodySmall, color = OverAmberText)
            }
        }
    }
}

@Composable
private fun AveragesComingCard(a: Analysis, onOpenHistory: () -> Unit, modifier: Modifier) {
    val usable = a.reliableCycles.size.coerceAtMost(MIN_RELIABLE_CYCLES)
    AnalysisCard(
        "Tes moyennes arrivent bientôt",
        "Il faut $MIN_RELIABLE_CYCLES cycles renseignés à plus de 80 %.",
        modifier,
    ) {
        Column(
            Modifier.semantics(mergeDescendants = true) { stateDescription = "$usable cycle sur $MIN_RELIABLE_CYCLES" },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row {
                Text("Cycles utilisables", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text("$usable sur $MIN_RELIABLE_CYCLES", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            BudgetProgressBar(fraction = usable / MIN_RELIABLE_CYCLES.toFloat(), color = CalmBlue, modifier = Modifier.fillMaxWidth())
        }
        a.cycles.takeLast(3).forEach { stat ->
            Row {
                Text(stat.cycle.label(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text(
                    "${stat.completion.percent} % · " + when {
                        stat.isCurrent -> "en cours"
                        stat.isReliable -> "utilisé"
                        else -> "non utilisé"
                    },
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                )
            }
        }
        if (a.missingDays > 0) {
            OutlinedButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp)) {
                Text(
                    if (a.missingDays == 1) "Compléter la journée manquante" else "Compléter les ${a.missingDays} journées manquantes",
                    color = Ink,
                )
            }
        }
    }
}

// ---------- À retenir ----------

@Composable
private fun InsightsCard(insights: List<String>, showBudgetButton: Boolean, onOpenBudget: () -> Unit, modifier: Modifier) {
    AnalysisCard("À retenir", null, modifier) {
        insights.forEach { Text(it, style = MaterialTheme.typography.bodyMedium, color = TextStrong) }
        if (showBudgetButton) {
            Button(
                onClick = onOpenBudget,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Background),
            ) { Text("Ajuster mes plafonds", style = MaterialTheme.typography.labelLarge) }
        }
    }
}

// ---------- Preview ----------

@Preview(showBackground = true, widthDp = 360, heightDp = 1500)
@Composable
private fun AnalysisPreview() {
    val today = LocalDate.of(2026, 10, 12)
    val cats = listOf(
        Category(1, "Courses", "cart", Money.euros(250)), Category(2, "Restaurants", "restaurant", Money.euros(90)),
        Category(3, "Transports", "bus", Money.euros(75)), Category(4, "Loisirs", "ticket", Money.euros(60)),
    )
    fun tx(cat: Long, euros: Long, month: Int, day: Int) =
        Transaction(amount = Money.euros(euros), type = TransactionType.EXPENSE, categoryId = cat, date = LocalDate.of(2026, month, day))
    val transactions = listOf(
        tx(1, 230, 6, 1), tx(2, 95, 6, 5), tx(4, 110, 6, 9),
        tx(1, 250, 7, 1), tx(2, 80, 7, 5), tx(3, 60, 7, 9),
        tx(1, 245, 8, 1), tx(2, 100, 8, 5), tx(4, 105, 8, 9),
        tx(1, 140, 10, 1), tx(4, 95, 10, 3), tx(2, 60, 10, 5), tx(3, 30, 10, 8),
    )
    val start = BudgetCycleForPreview.start(today)
    val analysis = buildAnalysis(
        today, 25, transactions,
        noExpenseDays = (0L..90L).map { start.plusDays(it) }.toSet(),
        categories = cats,
        incomes = listOf(Income(name = "Salaire", amount = Money.euros(1850), dayOfMonth = 25)),
        charges = listOf(FixedCharge(name = "Loyer", amount = Money.euros(743), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 10, 25))),
    )
    PersonalbudgetappTheme { AnalysisContent(analysis, onOpenHistory = {}, onOpenBudget = {}) }
}

/** Pour la preview : 3 cycles bien renseignés avant le cycle en cours. */
private object BudgetCycleForPreview {
    fun start(today: LocalDate): LocalDate =
        com.application.personal_budget_app.domain.cycle.BudgetCycle.containing(today, 25).shifted(-3).start
}