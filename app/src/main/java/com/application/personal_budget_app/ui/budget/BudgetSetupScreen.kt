package com.application.personal_budget_app.ui.budget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.budget.BudgetOverview
import com.application.personal_budget_app.domain.budget.SetupStep
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.ui.format.shortFr
import com.application.personal_budget_app.ui.theme.*
import java.time.LocalDate

@Composable
fun BudgetSetupScreen(
    onClose: () -> Unit,
    onActivated: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableStateOf(SetupStep.INCOMES) }
    var activating by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = !step.isFirst) { step = step.previous() }

    val current = state ?: return
    BudgetSetupContent(
        overview = current.overview,
        step = step,
        activating = activating,
        onBack = { if (step.isFirst) onClose() else step = step.previous() },
        onNext = { step = step.next() },
        onSkip = onClose,
        onActivate = {
            if (!activating) {
                activating = true
                viewModel.activateBudget(onActivated)
            }
        },
        onEdit = viewModel::edit,
    )
    BudgetEditorHost(viewModel, current.editor)
}

@Composable
fun BudgetSetupContent(
    overview: BudgetOverview,
    step: SetupStep,
    activating: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onActivate: () -> Unit,
    onEdit: (BudgetEditor) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(BudgetGradients.header)
            .statusBarsPadding().navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
    ) {
        SetupTopBar(step, onBack)

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 20.dp, bottom = 16.dp)) {
            item { SetupHeader(step) }
            item { SpendableCard(overview, step) }

            when (step) {
                SetupStep.INCOMES -> {
                    items(overview.incomes, key = { "income-${it.id}" }) { income ->
                        BudgetRow(income.name, "le ${income.dayOfMonth}", null, income.amount.format(), Modifier) {
                            onEdit(BudgetEditor.IncomeEditor(income))
                        }
                    }
                    item { AddRow("Ajouter un revenu", Modifier) { onEdit(BudgetEditor.IncomeEditor(null)) } }
                }

                SetupStep.CHARGES -> {
                    item { SectionHeader("Charges fixes", "Ce cycle ${overview.chargesThisCycle.format()}", Modifier) }
                    items(overview.charges, key = { "charge-${it.id}" }) { charge ->
                        val monthly = charge.frequency == Frequency.MONTHLY
                        val inCycle = charge.isDueIn(overview.cycle)
                        BudgetRow(
                            title = charge.name,
                            subtitle = if (monthly) "le ${charge.nextDueDate.dayOfMonth}" else "échéance le ${charge.nextDueDate.shortFr()}",
                            frequency = charge.frequency,
                            amount = charge.amount.format(),
                            modifier = Modifier,
                            amountColor = if (inCycle) Ink else TextSecondary,
                        ) { onEdit(BudgetEditor.ChargeEditor(charge)) }
                    }
                    item { AddRow("Ajouter une charge fixe", Modifier) { onEdit(BudgetEditor.ChargeEditor(null)) } }

                    item { SectionHeader("Épargne planifiée · facultatif", overview.savingsTotal.format(), Modifier) }
                    items(overview.savings, key = { "saving-${it.id}" }) { saving ->
                        BudgetRow(saving.label, "le ${saving.dayOfMonth}", null, saving.amount.format(), Modifier) {
                            onEdit(BudgetEditor.SavingEditor(saving))
                        }
                    }
                    item { AddRow("Ajouter une épargne", Modifier) { onEdit(BudgetEditor.SavingEditor(null)) } }
                }

                SetupStep.CAPS -> {
                    items(overview.categories, key = { "category-${it.id}" }) { category ->
                        BudgetRow(
                            title = category.name,
                            subtitle = if (category.isFuse) "Couvre les dépassements" else null,
                            frequency = null,
                            amount = category.cap?.format() ?: "Pas de plafond",
                            modifier = Modifier,
                            iconKey = category.iconKey,
                            amountColor = if (category.cap == null) TextSecondary else Ink,
                        ) { onEdit(BudgetEditor.CapEditor(category)) }
                    }
                    item { UnallocatedLine(overview, Modifier) }
                }
            }
        }

        val canContinue = step.canContinue(overview)
        if (!canContinue) {
            Text(
                "Ajoute au moins un revenu pour continuer.",
                style = MaterialTheme.typography.bodySmall, color = TextStrong,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }
        Button(
            onClick = if (step.isLast) onActivate else onNext,
            enabled = canContinue && !activating,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Ink, contentColor = Background,
                disabledContainerColor = SurfaceSoft, disabledContentColor = TextStrong,
            ),
        ) {
            Text(
                if (step.isLast) "Activer mon budget" else "Continuer",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
            )
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Plus tard", color = TextStrong)
        }
    }
}

@Composable
private fun SetupTopBar(step: SetupStep, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(
                painterResource(if (step.isFirst) R.drawable.ic_close else R.drawable.ic_chevron_left),
                contentDescription = if (step.isFirst) "Fermer l'assistant" else "Étape précédente",
                modifier = Modifier.size(20.dp),
            )
        }
        Row(
            Modifier.weight(1f).semantics { contentDescription = "Étape ${step.number} sur ${SetupStep.count}" },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(SetupStep.count) { index ->
                Box(
                    Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                        .background(if (index < step.number) Ink else Color(0xFFD3DAE4)),
                )
            }
        }
    }
}

@Composable
private fun SetupHeader(step: SetupStep) {
    val (title, subtitle) = when (step) {
        SetupStep.INCOMES -> "Qu'est-ce qui rentre chaque mois ?" to
                "Salaire, aides, pension… Le montant net et le jour où il arrive."
        SetupStep.CHARGES -> "Qu'est-ce qui part automatiquement ?" to
                "Loyer, abonnements, assurances. Une charge annuelle ne compte que le cycle où elle tombe."
        SetupStep.CAPS -> "Combien pour chaque enveloppe ?" to
                "Un plafond par cycle. Sans plafond, une enveloppe est seulement suivie."
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Étape ${step.number} sur ${SetupStep.count}",
            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextSecondary,
        )
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, lineHeight = 32.sp),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextStrong)
    }
}

/** Le chiffre qui compte à chaque étape, mis à jour en direct. */
@Composable
private fun SpendableCard(overview: BudgetOverview, step: SetupStep) {
    val showIncome = step == SetupStep.INCOMES
    val value = if (showIncome) overview.incomeTotal else overview.spendable
    val negative = value.isNegative
    val shape = RoundedCornerShape(16.dp)

    Column(
        Modifier.padding(vertical = 16.dp).fillMaxWidth().clip(shape)
            .background(Color.White.copy(alpha = 0.8f)).border(1.dp, Divider, shape)
            .padding(16.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            if (showIncome) "Revenus par cycle" else "À dépenser par cycle",
            style = MaterialTheme.typography.bodySmall, color = TextSecondary,
        )
        Text(
            (if (negative) -value else value).format().let { if (negative) "−$it" else it },
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold,
            color = if (negative) OverAmberText else Ink,
        )
        when {
            negative -> Text(
                "Tes charges et ton épargne dépassent tes revenus.",
                style = MaterialTheme.typography.bodySmall, color = OverAmberText,
            )
            step == SetupStep.CHARGES -> Text(
                "après charges fixes et épargne",
                style = MaterialTheme.typography.bodySmall, color = TextStrong,
            )
            step == SetupStep.CAPS -> Text(
                "dont ${overview.capsTotal.format()} en plafonds",
                style = MaterialTheme.typography.bodySmall, color = TextStrong,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SetupCapsPreview() {
    PersonalbudgetappTheme {
        BudgetSetupContent(
            overview = BudgetOverview(
                mode = BudgetMode.OBSERVATION,
                cycle = BudgetCycle.containing(LocalDate.of(2026, 10, 12), 25),
                incomes = listOf(Income(1, "Salaire", Money.euros(1650), 25)),
                savings = emptyList(),
                charges = listOf(FixedCharge(1, "Loyer", Money.euros(650), Frequency.MONTHLY, LocalDate.of(2026, 10, 25))),
                categories = listOf(
                    Category(1, "Courses", "cart", Money.euros(250)),
                    Category(2, "Restaurants", "restaurant"),
                    Category(7, "Imprévus", "umbrella", Money.euros(60), isFuse = true),
                ),
            ),
            step = SetupStep.CAPS, activating = false,
            onBack = {}, onNext = {}, onSkip = {}, onActivate = {}, onEdit = {},
        )
    }
}