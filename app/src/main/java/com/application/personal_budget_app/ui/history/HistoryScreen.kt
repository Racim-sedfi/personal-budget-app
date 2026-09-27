package com.application.personal_budget_app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.R
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.cycle.DayState
import com.application.personal_budget_app.domain.cycle.completion
import com.application.personal_budget_app.domain.history.HistoryDay
import com.application.personal_budget_app.domain.history.buildHistory
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.ui.components.categoryIcon
import com.application.personal_budget_app.ui.components.dashedBorder
import com.application.personal_budget_app.ui.components.hatched
import com.application.personal_budget_app.ui.format.dayTitle
import com.application.personal_budget_app.ui.format.label
import com.application.personal_budget_app.ui.theme.*
import java.time.LocalDate

@Composable
fun HistoryScreen(
    onAddForDate: (LocalDate) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    state?.let {
        HistoryContent(
            state = it,
            onPrevious = viewModel::previousCycle,
            onNext = viewModel::nextCycle,
            onAddForDate = onAddForDate,
            onEditTransaction = onEditTransaction,
            onDeclareNoExpense = viewModel::declareNoExpense,
            onClearNoExpense = viewModel::clearNoExpense,
        )
    }
}

@Composable
fun HistoryContent(
    state: HistoryUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onAddForDate: (LocalDate) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onDeclareNoExpense: (LocalDate) -> Unit,
    onClearNoExpense: (LocalDate) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { HistoryHeader(state, onPrevious, onNext) }
        items(state.days, key = { it.date.toEpochDay() }) { day ->
            DaySection(
                day = day,
                today = state.today,
                categories = state.categories,
                onAdd = { onAddForDate(day.date) },
                onEdit = onEditTransaction,
                onDeclare = { onDeclareNoExpense(day.date) },
                onClear = { onClearNoExpense(day.date) },
            )
        }
    }
}

@Composable
private fun HistoryHeader(state: HistoryUiState, onPrevious: () -> Unit, onNext: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Historique", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) {
                Icon(painterResource(R.drawable.ic_chevron_left), contentDescription = "Cycle précédent")
            }
            Text(
                state.cycle.label(),
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onNext, enabled = !state.isCurrentCycle) {
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "Cycle suivant")
            }
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            SummaryItem("Dépenses", state.spent.format(), Modifier.weight(1f))
            SummaryItem(
                "Renseigné",
                "${state.completion.percent} % · ${state.completion.filledDays} j sur ${state.completion.elapsedDays}",
                Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = Divider)
    }
}

@Composable
private fun SummaryItem(label: String, value: String, modifier: Modifier) {
    Column(modifier.semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DaySection(
    day: HistoryDay,
    today: LocalDate,
    categories: Map<Long, Category>,
    onAdd: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDeclare: () -> Unit,
    onClear: () -> Unit,
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                day.date.dayTitle(today),
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            val total = when (day.state) {
                DayState.WITH_TRANSACTIONS -> "Total ${day.total.format()}"
                DayState.NO_EXPENSE -> Money.ZERO.format()
                DayState.TODAY_OPEN -> "En cours"
                DayState.NOT_FILLED, DayState.FUTURE -> "—" // jamais « 0 € » pour un jour non renseigné
            }
            Text(
                total,
                style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                modifier = if (total == "—") Modifier.semantics { contentDescription = "Pas de total" } else Modifier,
            )
        }

        when (day.state) {
            DayState.WITH_TRANSACTIONS -> day.transactions.forEach { tx ->
                TransactionRow(tx, categories[tx.categoryId], onClick = { onEdit(tx) })
            }
            DayState.NO_EXPENSE -> NoExpenseRow(onClear)
            DayState.NOT_FILLED -> NotFilledCard(onAdd, onDeclare)
            DayState.TODAY_OPEN -> TodayOpenCard(onDeclare)
            DayState.FUTURE -> Unit
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction, category: Category?, onClick: () -> Unit) {
    val isRefund = tx.type == TransactionType.REFUND
    val categoryName = category?.name ?: "Sans catégorie"
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClickLabel = "Modifier", role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(if (isRefund) RefundBg else SurfaceSoft),
            contentAlignment = Alignment.Center,
        ) {
            val icon = if (isRefund) R.drawable.ic_refund else categoryIcon(category?.iconKey.orEmpty())
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(tx.note ?: categoryName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            when {
                isRefund -> Text("Remboursement · $categoryName", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                tx.note != null -> Text(categoryName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        Text(
            (if (isRefund) "+" else "") + tx.amount.format(),
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NoExpenseRow(onClear: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).border(1.dp, Divider, shape).padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(painterResource(R.drawable.ic_status_ok), contentDescription = null, tint = CalmBlueDark, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
            Text("Aucune dépense", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("Journée déclarée", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        TextButton(onClick = onClear) { Text("Annuler", color = Ink) }
    }
}

@Composable
private fun NotFilledCard(onAdd: () -> Unit, onDeclare: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(Background)
            .hatched(Hatch).dashedBorder(DashedBorder, cornerRadius = 16.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Non renseignée", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onDeclare,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Background, contentColor = Ink),
            ) { Text("Aucune dépense") }
            Button(
                onClick = onAdd,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Background),
            ) { Text("Ajouter") }
        }
    }
}

@Composable
private fun TodayOpenCard(onDeclare: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier.fillMaxWidth().border(1.dp, Divider, shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Rien de saisi pour l'instant.", style = MaterialTheme.typography.bodyMedium, color = TextStrong)
        Button(
            onClick = onDeclare,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Background),
        ) {
            Icon(painterResource(R.drawable.ic_status_ok), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Aucune dépense aujourd'hui", style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ---------- Preview : les journées de la maquette ----------

@Preview(showBackground = true, widthDp = 360, heightDp = 1400)
@Composable
private fun HistoryPreview() {
    val today = LocalDate.of(2026, 10, 12)
    val cycle = BudgetCycle.containing(today, 25)
    fun d(day: Int) = LocalDate.of(2026, 10, day)
    fun tx(id: Long, day: Int, cents: Long, cat: Long, note: String?, type: TransactionType = TransactionType.EXPENSE) =
        Transaction(id, Money(cents), type, cat, d(day), note)
    val transactions = listOf(
        tx(1, 10, 3800, 4, "Place de concert"), tx(2, 10, 1650, 2, "Pizzeria"), tx(3, 10, 2340, 1, "Carrefour City"),
        tx(4, 8, 1735, 3, "Carnet de tickets"), tx(5, 8, 1200, 2, "Part de Léa", TransactionType.REFUND),
        tx(6, 6, 3180, 1, "Lidl"), tx(7, 6, 1200, 6, "Pharmacie"),
    )
    val noExpense = setOf(d(11))
    val categories = listOf(
        Category(1, "Courses", "cart"), Category(2, "Restaurants", "restaurant"), Category(3, "Transports", "bus"),
        Category(4, "Loisirs", "ticket"), Category(6, "Santé", "health"),
    ).associateBy { it.id }

    PersonalbudgetappTheme {
        HistoryContent(
            state = HistoryUiState(
                cycle = cycle, today = today, isCurrentCycle = true,
                days = buildHistory(cycle, today, transactions, noExpense),
                spent = transactions.netSpent(),
                completion = completion(cycle, today, transactions.map { it.date }.toSet(), noExpense),
                categories = categories,
            ),
            onPrevious = {}, onNext = {}, onAddForDate = {}, onEditTransaction = {},
            onDeclareNoExpense = {}, onClearNoExpense = {},
        )
    }
}