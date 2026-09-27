package com.application.personal_budget_app.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.application.personal_budget_app.domain.model.Transaction
import com.application.personal_budget_app.domain.model.TransactionType
import com.application.personal_budget_app.ui.entry.EntryEvent
import com.application.personal_budget_app.ui.entry.QuickEntrySheet
import com.application.personal_budget_app.ui.entry.QuickEntryViewModel
import com.application.personal_budget_app.ui.navigation.AppNavHost
import com.application.personal_budget_app.ui.navigation.BudgetBottomBar
import java.time.LocalDate

@Composable
fun BudgetAppRoot() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val entryViewModel: QuickEntryViewModel = hiltViewModel()
    var showEntry by rememberSaveable { mutableStateOf(false) }

    val openEntry: (LocalDate?) -> Unit = { date ->
        entryViewModel.start(date)
        showEntry = true
    }

    val openEdit: (Transaction) -> Unit = { transaction ->
        entryViewModel.startEdit(transaction)
        showEntry = true
    }

    // Après chaque ajout, modification ou suppression : on ferme la sheet et on propose d'annuler.
    LaunchedEffect(entryViewModel) {
        entryViewModel.events.collect { event ->
            showEntry = false
            val isRefund = event.transaction.type == TransactionType.REFUND
            val subject = if (isRefund) "Remboursement" else "Dépense"
            val agreement = if (isRefund) "" else "e" // ajouté / ajoutée
            val verb = when (event) {
                is EntryEvent.Added -> "ajouté"
                is EntryEvent.Updated -> "modifié"
                is EntryEvent.Deleted -> "supprimé"
            }
            val result = snackbarHostState.showSnackbar(
                message = "$subject $verb$agreement · ${event.transaction.amount.format()} · ${event.categoryName}",
                actionLabel = "Annuler",
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) entryViewModel.undo(event)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BudgetBottomBar(navController, onAddClick = { openEntry(null) }) },
    ) { innerPadding ->
        AppNavHost(
            navController,
            onAddClick = openEntry,
            onEditTransaction = openEdit,
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (showEntry) {
        QuickEntrySheet(entryViewModel, onDismiss = { showEntry = false })
    }
}
