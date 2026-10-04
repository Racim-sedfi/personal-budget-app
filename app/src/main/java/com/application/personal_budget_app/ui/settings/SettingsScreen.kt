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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.ui.components.CycleDayGrid
import com.application.personal_budget_app.ui.format.cycleRule
import com.application.personal_budget_app.ui.format.label
import java.time.LocalDate
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.application.personal_budget_app.domain.reminder.formatMinutesOfDay
import com.application.personal_budget_app.domain.model.ThemeMode

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings = state ?: return
    val context = LocalContext.current
    val lockAvailable = remember { canUseAppLock(context) }
    var notificationsRefused by remember { mutableStateOf(false) }

    // Android 13+ : il faut l'accord de l'utilisateur pour afficher une notification.
    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsRefused = !granted
        if (granted) viewModel.setReminderEnabled(true)
    }

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
        onHideInRecents = viewModel::setHideInRecents,
        onThemeMode = viewModel::setThemeMode,
        onCurrency = viewModel::setCurrency,
        onCycleStartDay = viewModel::setCycleStartDay,
        notificationsRefused = notificationsRefused,
        onToggleReminder = { enable ->
            val needsPermission = Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            when {
                !enable -> viewModel.setReminderEnabled(false)
                needsPermission -> askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                else -> viewModel.setReminderEnabled(true)
            }
        },
        onReminderTime = viewModel::setReminderTime,
        onReset = {
            // Verrouillage actif : on confirme l'identité avant de tout effacer.
            if (settings.lockEnabled && lockAvailable) {
                context.findFragmentActivity()?.authenticate("Confirmer la réinitialisation") { viewModel.resetAll() }
            } else {
                viewModel.resetAll()
            }
        },
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
    onReset: () -> Unit,
    onCycleStartDay: (Int) -> Unit = {},
    notificationsRefused: Boolean = false,
    onToggleReminder: (Boolean) -> Unit = {},
    onReminderTime: (Int) -> Unit = {},
    onHideInRecents: (Boolean) -> Unit = {},
    onThemeMode: (ThemeMode) -> Unit = {},
) {
    var pickReminderTime by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    var editCycleDay by remember { mutableStateOf(false) }

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

                HorizontalDivider(color = Divider)
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 64.dp)
                        .toggleable(value = settings.hideInRecents, role = Role.Switch, onValueChange = onHideInRecents),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Masquer dans les apps récentes", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            "Cache tes montants dans l'aperçu des apps. Bloque aussi les captures d'écran.",
                            style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                        )
                    }
                    Switch(
                        checked = settings.hideInRecents,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(checkedTrackColor = Ink, checkedThumbColor = Background),
                    )
                }
            }
            SettingsSection("Rappel") {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 64.dp)
                        .toggleable(value = settings.reminderEnabled, role = Role.Switch, onValueChange = onToggleReminder),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Rappel quotidien", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            if (notificationsRefused) "Notifications refusées : autorise-les dans les réglages du téléphone."
                            else "Seulement si ta journée n'est pas renseignée",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (notificationsRefused) OverAmberText else TextSecondary,
                        )
                    }
                    Switch(
                        checked = settings.reminderEnabled,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(checkedTrackColor = Ink, checkedThumbColor = Background),
                    )
                }
                if (settings.reminderEnabled) {
                    HorizontalDivider(color = Divider)
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .clickable(onClickLabel = "Modifier", role = Role.Button) { pickReminderTime = true },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Heure du rappel", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(formatMinutesOfDay(settings.reminderMinutes), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Icon(
                            painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = TextSecondary,
                            modifier = Modifier.padding(start = 8.dp).size(16.dp),
                        )
                    }
                }
            }

            SettingsSection("Apparence") {
                Row(Modifier.selectableGroup().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ThemeMode.SYSTEM to "Automatique", ThemeMode.LIGHT to "Clair", ThemeMode.DARK to "Sombre").forEach { (mode, label) ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeMode(mode) },
                            label = { Text(label) },
                        )
                    }
                }
                Text(
                    "Automatique suit le réglage du téléphone.",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                )
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
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .clickable(onClickLabel = "Modifier", role = Role.Button) { editCycleDay = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Jour de début du cycle", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(
                        if (settings.cycleStartDay == 1) "le 1er" else "le ${settings.cycleStartDay}",
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = TextSecondary,
                        modifier = Modifier.padding(start = 8.dp).size(16.dp),
                    )
                }
            }

            SettingsSection("Données") {
                Text(
                    "Supprime tout ce qui est enregistré sur ce téléphone : dépenses, revenus, budget, enveloppes et réglages.",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                OutlinedButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Réinitialiser les données", color = OverAmberText) }
            }

            Text(
                "100 % hors ligne · tes données restent sur ce téléphone.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary,
            )
        }
    }

    if (pickReminderTime) {
        ReminderTimeDialog(
            minutes = settings.reminderMinutes,
            onConfirm = { onReminderTime(it); pickReminderTime = false },
            onDismiss = { pickReminderTime = false },
        )
    }

    if (editCycleDay) {
        CycleDayDialog(
            current = settings.cycleStartDay,
            onConfirm = { day -> onCycleStartDay(day); editCycleDay = false },
            onDismiss = { editCycleDay = false },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Tout réinitialiser ?") },
            text = { Text("Toutes tes données seront supprimées de ce téléphone. C'est définitif : l'app repartira de zéro.") },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onReset() }) { Text("Tout supprimer", color = OverAmberText) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Annuler", color = Ink) } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(minutes: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(initialHour = minutes / 60, initialMinute = minutes % 60, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Heure du rappel") },
        text = { TimePicker(state) },
        confirmButton = { TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("Valider", color = Ink) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler", color = Ink) } },
    )
}

/** Choisir un autre jour de début : aperçu du cycle avant de valider. */
@Composable
private fun CycleDayDialog(current: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var day by remember { mutableIntStateOf(current) }
    val cycle = BudgetCycle.containing(LocalDate.now(), day)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jour de début du cycle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CycleDayGrid(day, { day = it })
                Column(
                    Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text("Ton cycle en cours : ${cycle.label()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(cycleRule(day), style = MaterialTheme.typography.bodySmall, color = TextStrong)
                }
                Text(
                    "Tes dépenses ne bougent pas : seuls les cycles sont recalculés. Le bilan du cycle précédent pourra t'être reproposé.",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(day) }, enabled = day != current) { Text("Valider", color = Ink) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler", color = Ink) } },
    )
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
            lockAvailable = true, onBack = {}, onToggleLock = {}, onDelay = {}, onCurrency = {}, onReset = {},
        )
    }
}