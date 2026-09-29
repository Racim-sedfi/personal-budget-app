package com.application.personal_budget_app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.model.AppCurrency
import com.application.personal_budget_app.domain.model.AppSettings
import com.application.personal_budget_app.ui.components.CurrencyPicker
import com.application.personal_budget_app.ui.lock.authenticate
import com.application.personal_budget_app.ui.lock.canUseAppLock
import com.application.personal_budget_app.ui.lock.findFragmentActivity
import com.application.personal_budget_app.ui.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings = state ?: return
    val context = LocalContext.current
    val lockAvailable = remember { canUseAppLock(context) }

    SettingsContent(
        settings = settings,
        lockAvailable = lockAvailable,
        onBack = onBack,
        onToggleLock = { enable ->
            if (enable) {
                // On active seulement si l'authentification réussit : impossible de s'enfermer dehors.
                context.findFragmentActivity()?.authenticate("Activer le verrouillage") { viewModel.setLockEnabled(true) }
            } else {
                viewModel.setLockEnabled(false)
            }
        },
        onDelay = viewModel::setLockDelay,
        onCurrency = viewModel::setCurrency,
    )
}

@Composable
fun SettingsContent(
    settings: AppSettings,
    lockAvailable: Boolean,
    onBack: () -> Unit,
    onToggleLock: (Boolean) -> Unit,
    onDelay: (Int) -> Unit,
    onCurrency: (AppCurrency) -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = "Retour", modifier = Modifier.size(20.dp))
            }
            Text(
                "Paramètres", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
            )
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSection("Sécurité") {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 64.dp)
                        .toggleable(
                            value = settings.lockEnabled,
                            enabled = lockAvailable,
                            role = Role.Switch,
                            onValueChange = onToggleLock,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Verrouillage", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            if (lockAvailable) "Empreinte, visage ou code à l'ouverture"
                            else "Configure d'abord un verrouillage dans les réglages du téléphone",
                            style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                        )
                    }
                    Switch(
                        checked = settings.lockEnabled,
                        onCheckedChange = null,   // c'est la ligne entière qui réagit (cible plus grande)
                        enabled = lockAvailable,
                        colors = SwitchDefaults.colors(checkedTrackColor = Ink, checkedThumbColor = Background),
                    )
                }

                if (settings.lockEnabled) {
                    HorizontalDivider(color = Divider)
                    Text("Verrouiller après", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Immédiatement", 1 to "1 min", 5 to "5 min").forEach { (minutes, label) ->
                            FilterChip(
                                selected = settings.lockDelayMinutes == minutes,
                                onClick = { onDelay(minutes) },
                                label = { Text(label) },
                            )
                        }
                    }
                }
            }
            SettingsSection("Monnaie") {
                Text(
                    "Change seulement le symbole affiché : tes montants ne sont pas convertis.",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                CurrencyPicker(settings.currency, onCurrency)
            }
            SettingsSection("Cycle") {
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Jour de début du cycle", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                        if (settings.cycleStartDay == 1) "le 1er" else "le ${settings.cycleStartDay}",
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Text(
                "100 % hors ligne · tes données restent sur ce téléphone.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary,
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).border(1.dp, Divider, shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        content()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SettingsPreview() {
    PersonalbudgetappTheme {
        SettingsContent(
            settings = AppSettings(cycleStartDay = 25, onboardingDone = true, lockEnabled = true, lockDelayMinutes = 1),
            lockAvailable = true, onBack = {}, onToggleLock = {}, onDelay = {}, onCurrency = {},
        )
    }
}