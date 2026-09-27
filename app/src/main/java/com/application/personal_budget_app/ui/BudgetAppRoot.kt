package com.application.personal_budget_app.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.application.personal_budget_app.domain.model.TransactionType
import com.application.personal_budget_app.ui.entry.QuickEntrySheet
import com.application.personal_budget_app.ui.entry.QuickEntryViewModel
import com.application.personal_budget_app.ui.navigation.AppNavHost
import com.application.personal_budget_app.ui.navigation.BudgetBottomBar

@Composable
fun BudgetAppRoot() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val entryViewModel: QuickEntryViewModel = hiltViewModel()
    var showEntry by rememberSaveable { mutableStateOf(false) }

    // Après chaque enregistrement : on ferme la sheet et on propose d'annuler.
    LaunchedEffect(entryViewModel) {
        entryViewModel.saved.collect { saved ->
            showEntry = false
            val label = if (saved.type == TransactionType.REFUND) "Remboursement ajouté" else "Dépense ajoutée"
            val result = snackbarHostState.showSnackbar(
                message = "$label · ${saved.amount.format()} · ${saved.categoryName}",
                actionLabel = "Annuler",
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) entryViewModel.undo(saved.id)
        }
    }

    val openEntry = {
        entryViewModel.start()
        showEntry = true
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BudgetBottomBar(navController, onAddClick = openEntry) },
    ) { innerPadding ->
        AppNavHost(navController, onAddClick = openEntry, modifier = Modifier.padding(innerPadding))
    }

    if (showEntry) {
        QuickEntrySheet(entryViewModel, onDismiss = { showEntry = false })
    }
}