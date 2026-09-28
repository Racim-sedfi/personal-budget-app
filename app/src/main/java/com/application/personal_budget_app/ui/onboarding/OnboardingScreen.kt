package com.application.personal_budget_app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.onboarding.OnboardingStep
import com.application.personal_budget_app.domain.onboarding.StartChoice
import com.application.personal_budget_app.ui.format.cycleRule
import com.application.personal_budget_app.ui.format.label
import com.application.personal_budget_app.ui.theme.*
import java.time.LocalDate

@Composable
fun OnboardingScreen(
    onChoiceMade: (StartChoice) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(enabled = !state.step.isFirst) { viewModel.back() }

    OnboardingContent(
        state = state,
        onBack = viewModel::back,
        onNext = viewModel::next,
        onPickDay = viewModel::pickStartDay,
        onPickChoice = viewModel::pickChoice,
        onFinish = {
            onChoiceMade(state.choice)
            viewModel.finish()
        },
    )
}

private fun stepBackground(step: OnboardingStep): Brush {
    val top = when (step) {
        OnboardingStep.PRIVACY -> Color(0xFFDDEBFF)
        OnboardingStep.CYCLE_START -> Color(0xFFE4E1FC)
        OnboardingStep.START_CHOICE -> Color(0xFFE4F1FF)
    }
    return Brush.verticalGradient(0f to top, 0.38f to Color(0xFFF3F5F9), 1f to Background)
}

@Composable
fun OnboardingContent(
    state: OnboardingUiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPickDay: (Int) -> Unit,
    onPickChoice: (StartChoice) -> Unit,
    onFinish: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(stepBackground(state.step))
            .statusBarsPadding().navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
    ) {
        TopBar(state.step, onBack)

        AnimatedContent(
            targetState = state.step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "onboarding-step",
        ) { step ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                when (step) {
                    OnboardingStep.PRIVACY -> PrivacyStep()
                    OnboardingStep.CYCLE_START -> CycleStartStep(state.startDay, state.cycle, onPickDay)
                    OnboardingStep.START_CHOICE -> StartChoiceStep(state.choice, onPickChoice)
                }
            }
        }

        Button(
            onClick = if (state.step.isLast) onFinish else onNext,
            enabled = !state.saving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Background),
        ) {
            Text(
                if (state.step.isLast) "Commencer" else "Continuer",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
            )
        }
    }
}

@Composable
private fun TopBar(step: OnboardingStep, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        if (!step.isFirst) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = "Retour", modifier = Modifier.size(20.dp))
            }
        }
        Row(
            Modifier.weight(1f).semantics { contentDescription = "Étape ${step.number} sur ${OnboardingStep.count}" },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(OnboardingStep.count) { index ->
                Box(
                    Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                        .background(if (index < step.number) Ink else Color(0xFFD3DAE4)),
                )
            }
        }
    }
}

@Composable
private fun StepHeader(step: OnboardingStep, title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Étape ${step.number} sur ${OnboardingStep.count}",
            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = TextSecondary,
        )
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, lineHeight = 32.sp),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = TextStrong) }
    }
}

// ---------- Étape 1 ----------

@Composable
private fun PrivacyStep() {
    Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(144.dp).clip(CircleShape).background(
                Brush.linearGradient(listOf(Color(0xFFD6C6F6), Color(0xFFBFC8FA), Color(0xFFA9D6FF))),
            ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.size(88.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_shield), contentDescription = null, tint = Ink, modifier = Modifier.size(44.dp))
            }
        }
    }
    StepHeader(OnboardingStep.PRIVACY, "Tes données restent sur ton téléphone.")
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        listOf("100 % hors ligne", "Aucune connexion bancaire", "Aucun compte à créer").forEach { point ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(painterResource(R.drawable.ic_status_ok), contentDescription = null, tint = CalmBlueDark, modifier = Modifier.size(22.dp))
                Text(point, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ---------- Étape 2 ----------

@Composable
private fun CycleStartStep(selected: Int, cycle: BudgetCycle, onPick: (Int) -> Unit) {
    StepHeader(OnboardingStep.CYCLE_START, "Quel jour commence ton mois budgétaire ?", "En général, le jour de ta paie.")

    Column(
        Modifier.selectableGroup().semantics { contentDescription = "Jour de début du cycle" },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        (1..28).chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { day ->
                    val isSelected = day == selected
                    Box(
                        Modifier.weight(1f).heightIn(min = 44.dp).clip(CircleShape)
                            .background(if (isSelected) BudgetGradients.primaryButton else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                            .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onPick(day) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            day.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Background else Ink,
                        )
                    }
                }
            }
        }
    }

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.8f))
            .border(BorderStroke(1.dp, Divider), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Ton cycle en cours", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(cycle.label(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(cycleRule(selected), style = MaterialTheme.typography.bodySmall, color = TextStrong)
    }
}

// ---------- Étape 3 ----------

@Composable
private fun StartChoiceStep(choice: StartChoice, onPick: (StartChoice) -> Unit) {
    StepHeader(OnboardingStep.START_CHOICE, "Comment veux-tu commencer ?", "Modifiable plus tard.")
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChoiceCard(
            icon = R.drawable.ic_budget,
            title = "Configurer mon budget",
            description = "Revenus, charges, plafonds. Environ 5 min.",
            selected = choice == StartChoice.CONFIGURE,
            selectedBrush = Brush.linearGradient(listOf(Color(0xFFBFE3FF), Color(0xFFC8D5FB))),
        ) { onPick(StartChoice.CONFIGURE) }
        ChoiceCard(
            icon = R.drawable.ic_eye,
            title = "Commencer par observer",
            description = "Saisis tes dépenses un cycle, l'app propose ensuite des plafonds.",
            selected = choice == StartChoice.OBSERVE,
            selectedBrush = Brush.linearGradient(listOf(Color(0xFFE3DEFC), Color(0xFFD6E3F8))),
        ) { onPick(StartChoice.OBSERVE) }
    }
}

@Composable
private fun ChoiceCard(
    icon: Int,
    title: String,
    description: String,
    selected: Boolean,
    selectedBrush: Brush,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape)
            .background(if (selected) selectedBrush else Brush.linearGradient(listOf(Background, Background)))
            .border(if (selected) 2.dp else 1.dp, if (selected) Ink else Color(0xFFE3E8EF), shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Background),
                contentAlignment = Alignment.Center,
            ) { Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.size(22.dp).clip(CircleShape).background(Background)
                    .border(if (selected) 7.dp else 1.5.dp, if (selected) Ink else Color(0xFF9AA1AA), CircleShape),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = TextStrong)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CycleStartPreview() {
    PersonalbudgetappTheme {
        OnboardingContent(
            state = OnboardingUiState(
                step = OnboardingStep.CYCLE_START, startDay = 25,
                cycle = BudgetCycle.containing(LocalDate.of(2026, 10, 12), 25),
            ),
            onBack = {}, onNext = {}, onPickDay = {}, onPickChoice = {}, onFinish = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun StartChoicePreview() {
    PersonalbudgetappTheme {
        OnboardingContent(
            state = OnboardingUiState(
                step = OnboardingStep.START_CHOICE,
                cycle = BudgetCycle.containing(LocalDate.of(2026, 10, 12), 1),
            ),
            onBack = {}, onNext = {}, onPickDay = {}, onPickChoice = {}, onFinish = {},
        )
    }
}