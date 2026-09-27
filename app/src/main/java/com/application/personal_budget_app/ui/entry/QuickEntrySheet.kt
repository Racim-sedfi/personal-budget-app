package com.application.personal_budget_app.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.entry.AmountInput
import com.application.personal_budget_app.domain.entry.KeypadKey
import com.application.personal_budget_app.domain.model.Category
import com.application.personal_budget_app.domain.model.TransactionType
import com.application.personal_budget_app.ui.components.categoryIcon
import com.application.personal_budget_app.ui.format.withWeekdayFr
import com.application.personal_budget_app.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEntrySheet(viewModel: QuickEntryViewModel, onDismiss: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Background,
    ) {
        QuickEntryContent(
            state = state,
            onKey = viewModel::onKey,
            onTypeChange = viewModel::onTypeChange,
            onCategorySelected = viewModel::onCategorySelected,
            onNoteChange = viewModel::onNoteChange,
            onOpenDetails = viewModel::openDetails,
            onSave = viewModel::save,
            onClose = onDismiss,
        )
    }
}

/** Sans ViewModel : facile à prévisualiser et à tester. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickEntryContent(
    state: QuickEntryState,
    onKey: (KeypadKey) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onNoteChange: (String) -> Unit,
    onOpenDetails: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    val dayText = if (state.isToday) "Aujourd'hui" else state.date.withWeekdayFr()
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TypeToggle(state.type, onTypeChange)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = "Fermer")
            }
        }

        AmountDisplay(state)

        FlowRow(
            Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.categories.forEach { category ->
                CategoryChip(category, category.id == state.selectedCategoryId) { onCategorySelected(category.id) }
            }
        }

        if (state.detailsOpen) {
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { Text("Note · $dayText") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            TextButton(onClick = onOpenDetails, modifier = Modifier.fillMaxWidth()) {
                Text("$dayText · ajouter une note", color = TextStrong)
            }
        }

        Keypad(onKey)

        Button(
            onClick = onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Ink, contentColor = Background,
                disabledContainerColor = SurfaceSoft, disabledContentColor = TextStrong,
            ),
        ) {
            Text(
                if (state.amount.isValid) "Enregistrer ${state.amount.money.format()}" else "Saisis un montant",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
            )
        }
    }
}

@Composable
private fun TypeToggle(type: TransactionType, onChange: (TransactionType) -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(12.dp)).background(SurfaceSoft).padding(3.dp).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(TransactionType.EXPENSE to "Dépense", TransactionType.REFUND to "Remboursement").forEach { (value, label) ->
            val selected = value == type
            Box(
                Modifier.height(44.dp).clip(RoundedCornerShape(9.dp))
                    .background(if (selected) Ink else Color.Transparent)
                    .selectable(selected, role = Role.RadioButton) { onChange(value) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = if (selected) Background else TextStrong)
            }
        }
    }
}

@Composable
private fun AmountDisplay(state: QuickEntryState) {
    val empty = state.amount.raw.isEmpty()
    val prefix = if (state.type == TransactionType.REFUND && !empty) "+" else ""
    val spoken = if (empty) "Montant vide" else "Montant : ${state.amount.money.format()}"
    Row(
        Modifier.fillMaxWidth().clearAndSetSemantics {
            liveRegion = LiveRegionMode.Polite   // TalkBack annonce chaque changement
            contentDescription = spoken
        },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(prefix + state.amount.display, style = MaterialTheme.typography.displayMedium, color = if (empty) TextSecondary else Ink, maxLines = 1)
        Spacer(Modifier.width(6.dp))
        Text("€", fontSize = 30.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 10.dp))
    }
}

@Composable
private fun CategoryChip(category: Category, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    val content = if (selected) Background else Ink
    Row(
        Modifier.height(44.dp).clip(shape)
            .background(if (selected) Ink else Background)
            .border(1.dp, if (selected) Ink else BorderControl, shape)
            .selectable(selected, role = Role.RadioButton, onClick = onClick)
            .padding(start = 12.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(painterResource(categoryIcon(category.iconKey)), contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        Text(category.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = content)
    }
}

@Composable
private fun Keypad(onKey: (KeypadKey) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { digit -> Key(digit.toString(), Modifier.weight(1f)) { onKey(KeypadKey.Digit(digit)) } }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Key(",", Modifier.weight(1f), description = "Virgule") { onKey(KeypadKey.Comma) }
            Key("0", Modifier.weight(1f)) { onKey(KeypadKey.Digit(0)) }
            Key(null, Modifier.weight(1f), description = "Effacer") { onKey(KeypadKey.Delete) }
        }
    }
}

@Composable
private fun Key(label: String?, modifier: Modifier, description: String? = null, onClick: () -> Unit) {
    Box(
        modifier.height(48.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceSoft)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { if (description != null) contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (label != null) Text(label, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        else Icon(painterResource(R.drawable.ic_backspace), contentDescription = null)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun QuickEntryPreview() {
    PersonalbudgetappTheme {
        QuickEntryContent(
            state = QuickEntryState(
                date = LocalDate.of(2026, 10, 12),
                amount = AmountInput("8,4"),
                categories = listOf(
                    Category(1, "Courses", "cart"), Category(2, "Restaurants", "restaurant"),
                    Category(3, "Transports", "bus"), Category(4, "Loisirs", "ticket"),
                    Category(5, "Shopping", "bag"), Category(6, "Santé", "health"),
                    Category(7, "Imprévus", "umbrella", isFuse = true),
                ),
                selectedCategoryId = 1,
            ),
            onKey = {}, onTypeChange = {}, onCategorySelected = {}, onNoteChange = {},
            onOpenDetails = {}, onSave = {}, onClose = {},
        )
    }
}