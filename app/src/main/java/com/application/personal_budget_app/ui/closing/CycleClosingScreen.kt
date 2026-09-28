package com.application.personal_budget_app.ui.closing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.budget.progressFraction
import com.application.personal_budget_app.domain.closing.CapAdjustment
import com.application.personal_budget_app.domain.closing.CycleOutcome
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.cycle.Completion
import com.application.personal_budget_app.domain.home.EnvelopeLine
import com.application.personal_budget_app.domain.model.BudgetMode
import com.application.personal_budget_app.domain.model.Money
import com.application.personal_budget_app.domain.model.sum
import com.application.personal_budget_app.ui.components.BudgetProgressBar
import com.application.personal_budget_app.ui.format.label
import com.application.personal_budget_app.ui.theme.*

@Composable
fun CycleClosingScreen(
    onDone: () -> Unit,
    onOpenSetup: () -> Unit,
    viewModel: CycleClosingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state ?: return
    val result = s.result
    when {
        result != null -> ClosingDone(result, onDone, onOpenSetup)
        s.review.mode == BudgetMode.BUDGET -> BudgetReview(
            s, onClose = onDone,
            onChoose = viewModel::choose, onToggle = viewModel::toggleAdjustment, onValidate = viewModel::validateBudget,
        )
        else -> ObservationReview(
            s, onClose = onDone,
            onStep = viewModel::stepCap, onApply = viewModel::applyProposedCaps, onKeepObserving = viewModel::keepObserving,
        )
    }
}

// ---------- Bilan en mode budget ----------

@Composable
private fun BudgetReview(
    state: CycleClosingUiState,
    onClose: () -> Unit,
    onChoose: (LeftoverChoice) -> Unit,
    onToggle: (Long) -> Unit,
    onValidate: () -> Unit,
) {
    val review = state.review
    val spendable = review.spendable ?: Money.ZERO
    ClosingScaffold(onClose, bottom = { PrimaryButton("Valider le cycle", !state.saving, onValidate) }) {
        item {
            val caption = "${review.spent.format()} dépensés sur ${spendable.format()}"
            if (review.overspent.cents > 0) {
                Hero(review.cycle, "Cycle terminé. Dépassement de", review.overspent.format(), OverAmberText, caption)
            } else {
                Hero(review.cycle, "Cycle terminé. Il te reste", review.leftover.format(), Ink, caption)
            }
        }
        item { ReliabilityNote(review.completion) }

        if (review.leftover.cents > 0) {
            item { ClosingSectionTitle("Que faire des ${review.leftover.format()} ?") }
            item { LeftoverChoices(state.choice, onChoose) }
        }

        if (review.adjustments.isNotEmpty()) {
            item { ClosingSectionTitle("Ce que tu peux ajuster") }
            items(review.adjustments, key = { it.category.id }) { adjustment ->
                AdjustmentRow(adjustment, applied = adjustment.category.id in state.applied) { onToggle(adjustment.category.id) }
            }
        }

        item { EnvelopeDetails(review.envelopes) }
    }
}

@Composable
private fun LeftoverChoices(choice: LeftoverChoice, onChoose: (LeftoverChoice) -> Unit) {
    val options = listOf(
        Triple(LeftoverChoice.SAVE, "Mettre de côté", "Tu le verses toi-même sur ton épargne."),
        Triple(LeftoverChoice.CARRY, "Reporter au cycle suivant", "S'ajoute à ce que tu peux dépenser."),
    )
    Column(Modifier.selectableGroup()) {
        options.forEach { (value, label, hint) ->
            val selected = choice == value
            Row(
                Modifier.fillMaxWidth().heightIn(min = 60.dp)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onChoose(value) }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RadioDot(selected)
                Column {
                    Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(hint, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun AdjustmentRow(adjustment: CapAdjustment, applied: Boolean, onToggle: () -> Unit) {
    val (icon, tint) = when {
        applied -> R.drawable.ic_status_ok to CalmBlueDark
        adjustment.isOver -> R.drawable.ic_status_over to OverAmber
        else -> R.drawable.ic_status_near to TextStrong
    }
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(adjustment.category.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                if (applied) "Nouveau plafond : ${adjustment.suggested.format()}"
                else "Plafond ${adjustment.current.format()} · dépensé ${adjustment.spent.format()}",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    applied -> CalmBlueDark
                    adjustment.isOver -> OverAmberText
                    else -> TextStrong
                },
            )
        }
        if (applied) {
            OutlinedButton(onClick = onToggle, shape = RoundedCornerShape(22.dp)) { Text("Annuler", color = Ink) }
        } else {
            Button(
                onClick = onToggle, shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Background),
            ) { Text("Passer à ${adjustment.suggested.format()}") }
        }
    }
    HorizontalDivider(color = Divider)
}

@Composable
private fun EnvelopeDetails(envelopes: List<EnvelopeLine>) {
    var open by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.padding(top = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .clickable(onClickLabel = if (open) "Masquer" else "Afficher") { open = !open }
                .semantics { stateDescription = if (open) "Déplié" else "Replié" },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Détail des enveloppes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(
                painterResource(R.drawable.ic_chevron_right), contentDescription = null,
                modifier = Modifier.size(16.dp).rotate(if (open) 90f else 0f),
            )
        }
        if (open) {
            envelopes.forEach { line ->
                val cap = line.category.cap
                Column(
                    Modifier.padding(vertical = 8.dp).semantics(mergeDescendants = true) {},
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row {
                        Text(line.category.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(
                            if (cap != null) "${line.spent.format()} / ${cap.format()}" else line.spent.format(),
                            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (cap != null) {
                        BudgetProgressBar(
                            fraction = progressFraction(line.spent, cap),
                            color = if (line.spent > cap) OverAmber else CalmBlue,
                        )
                    }
                }
            }
        }
    }
}

// ---------- Bilan après observation ----------

@Composable
private fun ObservationReview(
    state: CycleClosingUiState,
    onClose: () -> Unit,
    onStep: (Long, Long) -> Unit,
    onApply: () -> Unit,
    onKeepObserving: () -> Unit,
) {
    val review = state.review
    ClosingScaffold(onClose, bottom = {
        PrimaryButton(if (review.hasIncome) "Valider ces plafonds" else "Valider et saisir mes revenus", !state.saving, onApply)
        TextButton(onClick = onKeepObserving, enabled = !state.saving, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Observer un cycle de plus", color = TextStrong)
        }
    }) {
        item {
            Hero(
                review.cycle, "Cycle d'observation terminé. Tu as dépensé", review.spent.format(), Ink,
                "${review.completion.filledDays} journées renseignées sur ${review.completion.elapsedDays}",
            )
        }
        item { ReliabilityNote(review.completion) }
        item {
            ClosingSectionTitle("Tes plafonds")
            Text(
                "Proposés d'après ce cycle, arrondis aux 5 € supérieurs.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary,
            )
        }
        items(review.envelopes, key = { it.category.id }) { line ->
            CapStepperRow(line, state.caps[line.category.id], onStep)
        }
        item { CapsTotal(state.caps.values.filterNotNull().sum(), state.availableNext.takeIf { review.hasIncome }) }
    }
}

@Composable
private fun CapStepperRow(line: EnvelopeLine, cap: Money?, onStep: (Long, Long) -> Unit) {
    val name = line.category.name
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                if (line.category.isFuse) "Couvre les dépassements" else "Dépensé ${line.spent.format()}",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary,
            )
        }
        StepButton("−", "Baisser $name de 5 euros") { onStep(line.category.id, -5) }
        Text(
            cap?.format() ?: "Aucun",
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = if (cap == null) TextSecondary else Ink,
            modifier = Modifier.widthIn(min = 92.dp).semantics { liveRegion = LiveRegionMode.Polite },
        )
        StepButton("+", "Augmenter $name de 5 euros") { onStep(line.category.id, 5) }
    }
    HorizontalDivider(color = Divider)
}

@Composable
private fun StepButton(symbol: String, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).border(1.dp, BorderControl, CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, fontSize = 20.sp, modifier = Modifier.clearAndSetSemantics {})
    }
}

@Composable
private fun CapsTotal(total: Money, available: Money?) {
    val over = available != null && total > available
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(total.format(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            available?.let {
                Text(
                    "sur ${it.format()} disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (over) OverAmberText else TextSecondary,
                )
            }
        }
    }
}

// ---------- Cycle clôturé ----------

@Composable
private fun ClosingDone(result: ClosingResult, onDone: () -> Unit, onOpenSetup: () -> Unit) {
    ClosingScaffold(onClose = null, bottom = {
        PrimaryButton(
            if (result.needsSetup) "Saisir mes revenus" else "Commencer le cycle",
            enabled = true,
            onClick = if (result.needsSetup) onOpenSetup else onDone,
        )
    }) {
        item {
            Column(
                Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 32.dp)
                    .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(96.dp).clip(CircleShape).background(
                        Brush.linearGradient(listOf(Color(0xFFD6C6F6), Color(0xFFBFC8FA), Color(0xFFA9D6FF))),
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_status_ok), contentDescription = null, tint = Ink, modifier = Modifier.size(44.dp))
                }
                Text(
                    "Cycle clôturé",
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(result.message(), style = MaterialTheme.typography.bodyLarge, color = TextStrong, textAlign = TextAlign.Center)
            }
        }
        item {
            val shape = RoundedCornerShape(16.dp)
            Column(
                Modifier.fillMaxWidth().clip(shape).background(Color.White.copy(alpha = 0.8f))
                    .border(1.dp, Divider, shape).padding(16.dp)
                    .semantics(mergeDescendants = true) {},
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Nouveau cycle · ${result.nextCycle.label()}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                result.nextSpendable?.let {
                    Text(it.format(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                    Text("disponibles", style = MaterialTheme.typography.bodySmall, color = TextStrong)
                }
            }
        }
    }
}

private fun ClosingResult.message(): String = when (outcome) {
    CycleOutcome.SAVED -> "${leftover.format()} mis de côté."
    CycleOutcome.CARRIED -> "${leftover.format()} reportés sur le nouveau cycle."
    CycleOutcome.NOTHING_LEFT -> "Nouveau cycle, nouveau départ."
    CycleOutcome.OBSERVED -> when {
        budgetActivated -> "Tes plafonds sont en place. Le budget est activé."
        needsSetup -> "Tes plafonds sont enregistrés. Ajoute tes revenus pour activer le budget."
        else -> "On observe un cycle de plus."
    }
}

// ---------- Morceaux communs ----------

@Composable
private fun ClosingScaffold(
    onClose: (() -> Unit)?,
    bottom: @Composable ColumnScope.() -> Unit,
    content: LazyListScope.() -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(BudgetGradients.header)
            .statusBarsPadding().navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.offset(x = (-12).dp)) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = "Fermer", modifier = Modifier.size(20.dp))
                }
            }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp), content = content)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), content = bottom)
    }
}

@Composable
private fun Hero(cycle: BudgetCycle, title: String, amount: String, amountColor: Color, caption: String) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 24.dp).semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(cycle.label(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = OnGradient)
        Text(
            title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Text(amount, style = MaterialTheme.typography.displayLarge, color = amountColor)
        Text(caption, style = MaterialTheme.typography.bodyMedium, color = TextStrong)
    }
}

@Composable
private fun ReliabilityNote(completion: Completion) {
    if (completion.isReliable) return
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.75f)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(painterResource(R.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(18.dp))
        Text(
            "${completion.filledDays} journées renseignées sur ${completion.elapsedDays} : ce bilan est approximatif.",
            style = MaterialTheme.typography.bodySmall, color = TextStrong,
        )
    }
}

@Composable
private fun ClosingSectionTitle(text: String) {
    Text(
        text, style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 20.dp, bottom = 4.dp).semantics { heading() },
    )
}

@Composable
private fun RadioDot(selected: Boolean) {
    Box(
        Modifier.size(22.dp).clip(CircleShape).background(Background)
            .border(if (selected) 7.dp else 1.5.dp, if (selected) Ink else DashedBorder, CircleShape),
    )
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Ink, contentColor = Background,
            disabledContainerColor = SurfaceSoft, disabledContentColor = TextStrong,
        ),
    ) { Text(label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp)) }
}