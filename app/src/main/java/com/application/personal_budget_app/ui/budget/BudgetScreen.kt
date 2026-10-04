package com.application.personal_budget_app.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.budget.BudgetOverview
import com.application.personal_budget_app.domain.budget.usedPercent
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.ui.components.categoryIcon
import com.application.personal_budget_app.ui.format.shortFr
import com.application.personal_budget_app.ui.theme.*
import java.time.LocalDate

@Composable
fun BudgetScreen(onOpenSetup: () -> Unit, viewModel: BudgetViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val current = state ?: return

    BudgetContent(current.overview, onEdit = viewModel::edit, onSetMode = viewModel::setMode, onOpenSetup = onOpenSetup)
    BudgetEditorHost(viewModel, current.editor)
}

@Composable
fun BudgetContent(
    overview: BudgetOverview,
    onEdit: (BudgetEditor) -> Unit,
    onSetMode: (BudgetMode) -> Unit,
    onOpenSetup: () -> Unit,
) {
    val side = Modifier.padding(horizontal = 16.dp)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { BudgetHeader(overview, onSetMode, onOpenSetup) }

        // Revenus
        item { SectionHeader("Revenus", overview.incomeTotal.format(), side) }
        items(overview.incomes, key = { "income-${it.id}" }) { income ->
            BudgetRow(income.name, "le ${income.dayOfMonth}", null, income.amount.format(), side) {
                onEdit(BudgetEditor.IncomeEditor(income))
            }
        }
        item { AddRow("Ajouter un revenu", side) { onEdit(BudgetEditor.IncomeEditor(null)) } }

        // Charges fixes
        item { SectionHeader("Charges fixes", "Ce cycle ${overview.chargesThisCycle.format()}", side) }
        items(overview.charges, key = { "charge-${it.id}" }) { charge ->
            val monthly = charge.frequency == Frequency.MONTHLY
            val inCycle = charge.isDueIn(overview.cycle)
            BudgetRow(
                title = charge.name,
                subtitle = if (monthly) "le ${charge.nextDueDate.dayOfMonth}" else "échéance le ${charge.nextDueDate.shortFr()}",
                frequency = charge.frequency,
                amount = charge.amount.format(),
                modifier = side,
                amountColor = if (inCycle) Ink else TextSecondary, // hors cycle : plus discret
            ) { onEdit(BudgetEditor.ChargeEditor(charge)) }
        }
        item { AddRow("Ajouter une charge fixe", side) { onEdit(BudgetEditor.ChargeEditor(null)) } }

        // Épargne planifiée
        item { SectionHeader("Épargne planifiée", overview.savingsTotal.format(), side) }
        items(overview.savings, key = { "saving-${it.id}" }) { saving ->
            BudgetRow(saving.label, "le ${saving.dayOfMonth}", null, saving.amount.format(), side) {
                onEdit(BudgetEditor.SavingEditor(saving))
            }
        }
        item { AddRow("Ajouter une épargne", side) { onEdit(BudgetEditor.SavingEditor(null)) } }

        // Enveloppes
        item { SectionHeader("Enveloppes", "Plafonds ${overview.capsTotal.format()}", side) }
        items(overview.categories, key = { "category-${it.id}" }) { category ->
            BudgetRow(
                title = category.name,
                subtitle = if (category.isFuse) "Couvre les dépassements" else null,
                frequency = null,
                amount = category.cap?.format() ?: "Pas de plafond",
                modifier = side,
                iconKey = category.iconKey,
                amountColor = if (category.cap == null) TextSecondary else Ink,
            ) { onEdit(BudgetEditor.CategoryEditor(category)) }
        }
        item { AddRow("Ajouter une enveloppe", side) { onEdit(BudgetEditor.CategoryEditor(null)) } }
        item { UnallocatedLine(overview, side) }

        if (overview.mode == BudgetMode.BUDGET) {
            item {
                Box(Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                    TextButton(onClick = { onSetMode(BudgetMode.OBSERVATION) }) {
                        Text("Revenir au mode observation", color = TextStrong)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetHeader(overview: BudgetOverview, onSetMode: (BudgetMode) -> Unit, onOpenSetup: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(BudgetGradients.header).statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Budget · du ${overview.cycle.start.dayOfMonth} au ${overview.cycle.end.dayOfMonth}",
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = OnGradient,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("À dépenser par cycle", style = MaterialTheme.typography.bodyLarge, color = OnGradient, modifier = Modifier.semantics { heading() })
            Text(overview.spendable.format(), style = MaterialTheme.typography.displayLarge)
            Text("après charges fixes et épargne", style = MaterialTheme.typography.bodyMedium, color = OnGradient)
        }
        if (overview.incomeTotal.cents > 0) IncomeSplit(overview)
        if (overview.mode == BudgetMode.OBSERVATION) {
            ObservationBanner(overview.canActivateBudget, onActivate = { onSetMode(BudgetMode.BUDGET) }, onOpenSetup = onOpenSetup)
        }
    }
}

/** Barre de répartition des revenus : charges fixes / épargne / à dépenser. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IncomeSplit(overview: BudgetOverview) {
    val income = overview.incomeTotal.cents.toFloat()
    val charges = (overview.chargesThisCycle.cents / income).coerceIn(0f, 1f)
    val savings = (overview.savingsTotal.cents / income).coerceIn(0f, 1f - charges)
    val rest = (1f - charges - savings).coerceAtLeast(0f)
    val chargesPct = usedPercent(overview.chargesThisCycle, overview.incomeTotal)
    val savingsPct = usedPercent(overview.savingsTotal, overview.incomeTotal)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
                .semantics { contentDescription = "Charges fixes $chargesPct %, épargne $savingsPct % de tes revenus" },
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (charges > 0f) Box(Modifier.weight(charges).fillMaxHeight().background(CalmBlueDark))
            if (savings > 0f) Box(Modifier.weight(savings).fillMaxHeight().background(CalmBlue))
            if (rest > 0f) Box(Modifier.weight(rest).fillMaxHeight().background(Color.White.copy(alpha = 0.85f)))
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LegendItem(CalmBlueDark, "Charges fixes $chargesPct %")
            LegendItem(CalmBlue, "Épargne $savingsPct %")
        }
        if (overview.spendable.isNegative) {
            Text(
                "Tes charges et ton épargne dépassent tes revenus de ${(-overview.spendable).format()}.",
                style = MaterialTheme.typography.bodyMedium, color = OverAmberText,
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = OnGradient)
    }
}

@Composable
private fun ObservationBanner(canActivate: Boolean, onActivate: () -> Unit, onOpenSetup: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.75f)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(R.drawable.ic_eye), contentDescription = null, tint = NearLimitText, modifier = Modifier.size(18.dp))
            Text("Mode observation", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = NearLimitText)
        }
        Text(
            if (canActivate) "Pose tes plafonds, puis active le budget."
            else "Revenus, charges et plafonds en 3 étapes.",
            style = MaterialTheme.typography.bodyMedium, color = TextStrong,
        )
        Button(
            onClick = if (canActivate) onActivate else onOpenSetup,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Background),
        ) {
            Text(if (canActivate) "Activer le budget" else "Configurer pas à pas", style = MaterialTheme.typography.labelLarge)
        }
    }
}
@Composable
internal fun SectionHeader(title: String, total: String, modifier: Modifier) {
    Row(modifier.fillMaxWidth().padding(top = 20.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).semantics { heading() })
        Text(total, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun BudgetRow(
    title: String,
    subtitle: String?,
    frequency: Frequency?,
    amount: String,
    modifier: Modifier,
    iconKey: String? = null,
    amountColor: Color = Ink,
    onClick: () -> Unit,
) {
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp)
                .clickable(onClickLabel = "Modifier", role = Role.Button, onClick = onClick)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (iconKey != null) Icon(painterResource(categoryIcon(iconKey)), contentDescription = null, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                if (frequency != null || subtitle != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        frequency?.let { FrequencyTag(it) }
                        subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                    }
                }
            }
            Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = amountColor)
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        HorizontalDivider(color = Divider)
    }
}

@Composable
private fun FrequencyTag(frequency: Frequency) {
    val (background, content) = when (frequency) {
        Frequency.MONTHLY -> RefundBg to CalmBlueDark
        Frequency.QUARTERLY -> Color(0xFFE6E2FC) to NearLimitText
        Frequency.YEARLY -> SurfaceSoft to TextStrong
    }
    Text(
        frequency.label(),
        style = MaterialTheme.typography.labelSmall, color = content,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(background).padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

fun Frequency.label(): String = when (this) {
    Frequency.MONTHLY -> "Mensuelle"
    Frequency.QUARTERLY -> "Trimestrielle"
    Frequency.YEARLY -> "Annuelle"
}

@Composable
internal fun AddRow(label: String, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun UnallocatedLine(overview: BudgetOverview, modifier: Modifier) {
    val over = overview.unallocated.isNegative
    Row(modifier.fillMaxWidth().heightIn(min = 52.dp).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (over) "Plafonds au-dessus du budget" else "Non alloué",
            style = MaterialTheme.typography.bodyMedium, color = if (over) OverAmberText else TextStrong,
            modifier = Modifier.weight(1f),
        )
        Text(
            (if (over) -overview.unallocated else overview.unallocated).format(),
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
            color = if (over) OverAmberText else Ink,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1700)
@Composable
private fun BudgetPreview() {
    val today = LocalDate.of(2026, 10, 12)
    PersonalbudgetappTheme {
        BudgetContent(
            overview = BudgetOverview(
                mode = BudgetMode.OBSERVATION,
                cycle = BudgetCycle.containing(today, 25),
                incomes = listOf(Income(1, "Salaire", Money.euros(1650), 25), Income(2, "Aide au logement", Money.euros(200), 5)),
                savings = listOf(PlannedSaving(1, "Virement vers le livret", Money.euros(150), 26)),
                charges = listOf(
                    FixedCharge(1, "Loyer", Money.euros(650), Frequency.MONTHLY, LocalDate.of(2026, 10, 25)),
                    FixedCharge(2, "Électricité", Money.euros(48), Frequency.MONTHLY, LocalDate.of(2026, 11, 10)),
                    FixedCharge(3, "Eau", Money.euros(45), Frequency.QUARTERLY, LocalDate.of(2027, 1, 15)),
                    FixedCharge(4, "Assurance habitation", Money.euros(96), Frequency.YEARLY, LocalDate.of(2027, 3, 15)),
                ),
                categories = listOf(
                    Category(1, "Courses", "cart", Money.euros(250)), Category(2, "Restaurants", "restaurant", Money.euros(90)),
                    Category(3, "Transports", "bus"), Category(7, "Imprévus", "umbrella", Money.euros(60), isFuse = true),
                ),
            ),
            onOpenSetup = {},
            onEdit = {}, onSetMode = {},
        )
    }
}