package com.application.personal_budget_app.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.budget.nextMonthlyDate
import com.application.personal_budget_app.domain.budget.parseAmount
import com.application.personal_budget_app.domain.entry.AmountInput
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.ui.format.shortFr
import com.application.personal_budget_app.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditorSheet(
    editor: BudgetEditor,
    today: LocalDate,
    onSaveIncome: (Income) -> Unit,
    onSaveSaving: (PlannedSaving) -> Unit,
    onSaveCharge: (FixedCharge) -> Unit,
    onSaveCap: (Category, Money?) -> Unit,
    onDeleteIncome: (Long) -> Unit,
    onDeleteSaving: (Long) -> Unit,
    onDeleteCharge: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Background,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp).navigationBarsPadding().imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (editor) {
                is BudgetEditor.IncomeEditor -> NameAmountDayForm(
                    title = if (editor.income == null) "Nouveau revenu" else "Modifier le revenu",
                    nameLabel = "Nom",
                    initialName = editor.income?.name.orEmpty(),
                    initialAmount = editor.income?.amount,
                    initialDay = editor.income?.dayOfMonth ?: today.dayOfMonth,
                    onSave = { name, amount, day -> onSaveIncome(Income(editor.income?.id ?: 0, name, amount, day)) },
                    onDelete = editor.income?.let { income -> { onDeleteIncome(income.id) } },
                )
                is BudgetEditor.SavingEditor -> NameAmountDayForm(
                    title = if (editor.saving == null) "Nouvelle épargne" else "Modifier l'épargne",
                    nameLabel = "Libellé",
                    initialName = editor.saving?.label.orEmpty(),
                    initialAmount = editor.saving?.amount,
                    initialDay = editor.saving?.dayOfMonth ?: today.dayOfMonth,
                    onSave = { label, amount, day -> onSaveSaving(PlannedSaving(editor.saving?.id ?: 0, label, amount, day)) },
                    onDelete = editor.saving?.let { saving -> { onDeleteSaving(saving.id) } },
                )
                is BudgetEditor.ChargeEditor -> ChargeForm(
                    charge = editor.charge,
                    today = today,
                    onSave = onSaveCharge,
                    onDelete = editor.charge?.let { charge -> { onDeleteCharge(charge.id) } },
                )
                is BudgetEditor.CapEditor -> CapForm(editor.category) { cap -> onSaveCap(editor.category, cap) }
            }
        }
    }
}

@Composable
private fun NameAmountDayForm(
    title: String,
    nameLabel: String,
    initialName: String,
    initialAmount: Money?,
    initialDay: Int,
    onSave: (String, Money, Int) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var amountText by rememberSaveable { mutableStateOf(initialAmount?.let { AmountInput.from(it).raw }.orEmpty()) }
    var dayText by rememberSaveable { mutableStateOf(initialDay.toString()) }

    val amount = parseAmount(amountText)
    val day = dayText.toIntOrNull()?.takeIf { it in 1..31 }
    val valid = name.isNotBlank() && amount != null && amount.cents > 0 && day != null

    SheetTitle(title)
    OutlinedTextField(
        value = name, onValueChange = { name = it.take(40) },
        label = { Text(nameLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
    )
    AmountField(amountText, { amountText = it }, isError = amountText.isNotEmpty() && amount == null)
    DayField(dayText, { dayText = it.filter(Char::isDigit).take(2) }, isError = dayText.isNotEmpty() && day == null)
    SaveButton(enabled = valid) { if (amount != null && day != null) onSave(name.trim(), amount, day) }
    onDelete?.let { DeleteButton("Supprimer", it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChargeForm(
    charge: FixedCharge?,
    today: LocalDate,
    onSave: (FixedCharge) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by rememberSaveable { mutableStateOf(charge?.name.orEmpty()) }
    var amountText by rememberSaveable { mutableStateOf(charge?.amount?.let { AmountInput.from(it).raw }.orEmpty()) }
    var frequency by rememberSaveable { mutableStateOf(charge?.frequency ?: Frequency.MONTHLY) }
    var dayText by rememberSaveable { mutableStateOf((charge?.nextDueDate?.dayOfMonth ?: today.dayOfMonth).toString()) }
    var dueDate by rememberSaveable { mutableStateOf(charge?.nextDueDate ?: today) }
    var showPicker by remember { mutableStateOf(false) }

    val amount = parseAmount(amountText)
    val day = dayText.toIntOrNull()?.takeIf { it in 1..31 }
    val valid = name.isNotBlank() && amount != null && amount.cents > 0 && (frequency != Frequency.MONTHLY || day != null)

    SheetTitle(if (charge == null) "Nouvelle charge fixe" else "Modifier la charge")
    OutlinedTextField(
        value = name, onValueChange = { name = it.take(40) },
        label = { Text("Nom") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
    )
    AmountField(amountText, { amountText = it }, isError = amountText.isNotEmpty() && amount == null)

    Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Frequency.entries.forEach { f ->
            FilterChip(selected = frequency == f, onClick = { frequency = f }, label = { Text(f.label()) })
        }
    }

    if (frequency == Frequency.MONTHLY) {
        DayField(dayText, { dayText = it.filter(Char::isDigit).take(2) }, isError = dayText.isNotEmpty() && day == null)
    } else {
        OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(painterResource(R.drawable.ic_calendar), contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Prochaine échéance : ${dueDate.shortFr()}", color = Ink)
        }
    }

    SaveButton(enabled = valid) {
        if (amount == null) return@SaveButton
        val next = if (frequency == Frequency.MONTHLY) nextMonthlyDate(day ?: return@SaveButton, today) else dueDate
        onSave(FixedCharge(charge?.id ?: 0, name.trim(), amount, frequency, next))
    }
    onDelete?.let { DeleteButton("Supprimer cette charge", it) }

    if (showPicker) {
        DueDatePicker(
            initial = dueDate,
            onPicked = { dueDate = it; showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun CapForm(category: Category, onSave: (Money?) -> Unit) {
    var amountText by rememberSaveable { mutableStateOf(category.cap?.let { AmountInput.from(it).raw }.orEmpty()) }
    val amount = parseAmount(amountText)

    SheetTitle("Plafond · ${category.name}")
    AmountField(amountText, { amountText = it }, isError = amountText.isNotEmpty() && amount == null, label = "Plafond par cycle")
    SaveButton(enabled = amount != null && amount.cents > 0) { onSave(amount) }
    if (category.cap != null) DeleteButton("Retirer le plafond") { onSave(null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePicker(initial: LocalDate, onPicked: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) onPicked(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()) else onDismiss()
            }) { Text("Valider") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    ) { DatePicker(state) }
}

// ---------- Petits composants communs ----------

@Composable
private fun SheetTitle(text: String) {
    Text(text, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
}

@Composable
private fun AmountField(value: String, onValueChange: (String) -> Unit, isError: Boolean, label: String = "Montant") {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) }, suffix = { Text("€") },
        singleLine = true, isError = isError,
        supportingText = if (isError) { { Text("Par exemple 650 ou 48,90") } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DayField(value: String, onValueChange: (String) -> Unit, isError: Boolean) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text("Jour du mois") },
        singleLine = true, isError = isError,
        supportingText = { Text(if (isError) "Entre 1 et 31" else "Un 31 tombe le dernier jour des mois courts") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SaveButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Ink, contentColor = Background,
            disabledContainerColor = SurfaceSoft, disabledContentColor = TextStrong,
        ),
    ) { Text("Enregistrer", style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)) }
}

@Composable
private fun DeleteButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(48.dp)) {
        Icon(painterResource(R.drawable.ic_trash), contentDescription = null, tint = TextStrong, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = TextStrong)
    }
}