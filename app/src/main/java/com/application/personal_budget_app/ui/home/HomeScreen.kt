package com.application.personal_budget_app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.budget.EnvelopeStatus
import com.application.personal_budget_app.domain.budget.progressFraction
import com.application.personal_budget_app.domain.budget.usedPercent
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.home.HomeSummary
import com.application.personal_budget_app.domain.home.buildHomeSummary
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.ui.components.BudgetProgressBar
import com.application.personal_budget_app.ui.components.EnvelopeRow
import com.application.personal_budget_app.ui.components.SectionTitle
import com.application.personal_budget_app.ui.components.StatusBadge
import com.application.personal_budget_app.ui.format.daysLabel
import com.application.personal_budget_app.ui.format.label
import com.application.personal_budget_app.ui.format.shortFr
import com.application.personal_budget_app.ui.theme.*
import java.time.LocalDate

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onCompleteDays: () -> Unit,
    onEditBudget: () -> Unit,
    onCloseCycle: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val s = state) {
        HomeUiState.Loading -> Box(Modifier.fillMaxSize()) // quelques ms au démarrage
        is HomeUiState.Ready -> HomeContent(s.summary, onAddClick, onCompleteDays, onEditBudget, onCloseCycle)
    }
}

@Composable
fun HomeContent(
    summary: HomeSummary,
    onAddClick: () -> Unit,
    onCompleteDays: () -> Unit,
    onEditBudget: () -> Unit,
    onCloseCycle: () -> Unit,
    onOpenSettings: () -> Unit = {},
) {

    val sidePadding = Modifier.padding(horizontal = 16.dp)
    LazyColumn(Modifier.fillMaxSize()) {
        item { HomeHeader(summary) }
        summary.cycleToClose?.let { ended ->
            item { ClosingCard(ended, onCloseCycle, sidePadding.padding(top = 16.dp)) }
        }
        if (!summary.hasTransactions) {
            item { EmptyState(onAddClick, sidePadding) }
        }

        if (summary.completion.missingDays > 0) {
            item { TodoSection(summary.completion.missingDays, onCompleteDays, sidePadding) }
        }

        item {
            val isBudget = summary.snapshot != null
            SectionTitle(
                if (isBudget) "Enveloppes" else "Par catégorie",
                sidePadding.padding(top = 16.dp),
                actionLabel = if (isBudget) "Modifier" else null,
                onAction = onEditBudget,
            )
        }
        items(summary.envelopes, key = { it.category.id }) { line ->
            EnvelopeRow(line.toRowModel(summary), sidePadding)
        }

        if (summary.snapshot != null) {
            item { CycleSection(summary, sidePadding.padding(top = 16.dp)) }
        }
        item { Spacer(Modifier.height(24.dp)) }
        item { HomeHeader(summary, onOpenSettings) }
    }
}

@Composable
private fun ClosingCard(ended: BudgetCycle, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier.fillMaxWidth().clip(shape).background(BudgetGradients.observationHeader).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Cycle terminé", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${ended.label()} · fais le bilan en une minute.", style = MaterialTheme.typography.bodyMedium, color = TextStrong)
        }
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Background),
        ) { Text("Faire le bilan", style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
private fun HomeHeader(summary: HomeSummary, onOpenSettings: () -> Unit){
    val budget = summary.snapshot
    Column(
        Modifier.fillMaxWidth()
            .background(if (budget != null) BudgetGradients.header else BudgetGradients.observationHeader)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                summary.cycle.label(),
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = OnGradient,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenSettings, modifier = Modifier.offset(x = 12.dp)) {
                Icon(painterResource(R.drawable.ic_settings), contentDescription = "Paramètres", tint = Ink)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (budget != null) "Il te reste" else "Tu as dépensé",
                style = MaterialTheme.typography.bodyLarge, color = OnGradient,
                modifier = Modifier.semantics { heading() },
            )
            Text((budget?.remaining ?: summary.spent).format(), style = MaterialTheme.typography.displayLarge)
            Text(
                if (budget != null) "jusqu'au ${summary.cycle.end.shortFr()} · ${daysLabel(summary.daysRemaining)}"
                else "en ${daysLabel(summary.dayNumber)} · ${daysLabel(summary.daysRemaining)} restants",
                style = MaterialTheme.typography.bodyMedium, color = OnGradient,
            )
        }

        when (summary.overallStatus) {
            null -> StatusBadge("Cycle d'observation", R.drawable.ic_eye, NearLimitText)
            EnvelopeStatus.NORMAL -> StatusBadge("Dans ton budget", R.drawable.ic_status_ok, CalmBlueDark)
            EnvelopeStatus.NEAR_LIMIT -> StatusBadge("Proche de la limite", R.drawable.ic_status_near, NearLimitText)
            EnvelopeStatus.OVER -> StatusBadge("Budget dépassé", R.drawable.ic_status_over, OverAmberText)
        }

        if (budget != null) BudgetProgress(summary.spent, budget.spendable, summary.overallStatus ?: EnvelopeStatus.NORMAL)
    }
}

@Composable
private fun BudgetProgress(spent: Money, spendable: Money, status: EnvelopeStatus) {
    val color = when (status) {
        EnvelopeStatus.NORMAL -> Ink
        EnvelopeStatus.NEAR_LIMIT -> NearLimit
        EnvelopeStatus.OVER -> OverAmber
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BudgetProgressBar(
            fraction = progressFraction(spent, spendable),
            color = color,
            trackColor = Color.White.copy(alpha = 0.8f),
            height = 8.dp,
            modifier = Modifier.semantics {
                contentDescription = "${spent.format()} dépensés sur ${spendable.format()}, soit ${usedPercent(spent, spendable)} %"
            },
        )
        Row {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(spent.format()) }
                    append(" dépensés")
                },
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f),
            )
            Text("sur ${spendable.format()}", style = MaterialTheme.typography.bodyMedium, color = OnGradient)
        }
    }
}

@Composable
private fun EmptyState(onAddClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Aucune dépense enregistrée", style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = onAddClick,
            modifier = Modifier.height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Background),
        ) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ajouter une dépense", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TodoSection(missingDays: Int, onComplete: () -> Unit, modifier: Modifier) {
    Column(modifier.padding(top = 4.dp)) {
        SectionTitle("À faire")
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                if (missingDays == 1) "1 journée non renseignée" else "$missingDays journées non renseignées",
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = onComplete, shape = RoundedCornerShape(22.dp)) {
                Text("Compléter", color = Ink, style = MaterialTheme.typography.labelLarge)
            }
        }
        HorizontalDivider(color = Divider)
    }
}

@Composable
private fun CycleSection(summary: HomeSummary, modifier: Modifier) {
    Column(modifier) {
        SectionTitle("Ce cycle")
        CycleLine("Épargne planifiée", summary.plannedSavings.format())
        CycleLine("Charges fixes", summary.fixedCharges.format())
        summary.nextFixedCharge?.let { CycleLine("Prochaine charge", "${it.name} · ${it.nextDueDate.shortFr()}", last = true) }
        summary.snapshot?.carryOver?.takeIf { it.cents > 0 }?.let { CycleLine("Report du cycle précédent", "+ ${it.format()}") }
        summary.snapshot?.extraIncome?.takeIf { it.cents > 0 }?.let { CycleLine("Revenus imprévus", "+ ${it.format()}") }
    }
}

@Composable
private fun CycleLine(label: String, value: String, last: Boolean = false) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextStrong, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
    if (!last) HorizontalDivider(color = Divider)
}

// ---------- Previews : les données de la maquette ----------

private fun previewSummary(mode: BudgetMode): HomeSummary {
    val day = LocalDate.of(2026, 10, 10)
    val cats = listOf(
        Category(1, "Courses", "cart", Money.euros(250)), Category(2, "Restaurants", "restaurant", Money.euros(90)),
        Category(3, "Transports", "bus", Money.euros(75)), Category(4, "Loisirs", "ticket", Money.euros(60)),
        Category(5, "Shopping", "bag", Money.euros(80)), Category(6, "Santé", "health", Money.euros(30)),
        Category(7, "Imprévus", "umbrella", Money.euros(60), isFuse = true),
    )
    val tx = listOf(1L to 14230L, 2L to 6450L, 3L to 3820L, 4L to 9500L, 5L to 4790L, 6L to 1200L).map { (cat, cents) ->
        Transaction(amount = Money(cents), type = TransactionType.EXPENSE, categoryId = cat, date = day)
    }
    return buildHomeSummary(
        today = LocalDate.of(2026, 10, 12),
        settings = AppSettings(cycleStartDay = 25, mode = mode, onboardingDone = true),
        transactions = tx, noExpenseDays = emptySet(), categories = cats,
        incomes = listOf(Income(name = "Salaire", amount = Money.euros(1850), dayOfMonth = 25)),
        savings = listOf(PlannedSaving(label = "Livret", amount = Money.euros(150), dayOfMonth = 26)),
        charges = listOf(FixedCharge(name = "Loyer", amount = Money.euros(743), frequency = Frequency.MONTHLY, nextDueDate = LocalDate.of(2026, 10, 25))),
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1300)
@Composable
private fun HomeBudgetPreview(onCloseCycle: () -> Unit = {},) = PersonalbudgetappTheme {
    HomeContent(previewSummary(BudgetMode.BUDGET), {}, {}, {}, onCloseCycle)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun HomeObservationPreview(onCloseCycle: () -> Unit = {},) = PersonalbudgetappTheme {
    HomeContent(previewSummary(BudgetMode.OBSERVATION), {}, {}, {}, onCloseCycle)
}