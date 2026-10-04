package com.application.personal_budget_app.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.application.personal_budget_app.domain.model.Transaction
import com.application.personal_budget_app.domain.model.TransactionType
import com.application.personal_budget_app.ui.entry.EntryEvent
import com.application.personal_budget_app.ui.entry.QuickEntrySheet
import com.application.personal_budget_app.ui.entry.QuickEntryViewModel
import com.application.personal_budget_app.ui.navigation.AppNavHost
import com.application.personal_budget_app.ui.navigation.BudgetBottomBar
import com.application.personal_budget_app.ui.navigation.BudgetSetupRoute
import com.application.personal_budget_app.ui.navigation.CycleClosingRoute
import com.application.personal_budget_app.ui.navigation.SettingsRoute
import java.time.LocalDate

@Composable
fun BudgetAppRoot(
    openBudgetSetup: Boolean = false,
    locked: Boolean = false,
    openEntryRequest: Boolean = false,
    onEntryRequestHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val entryViewModel: QuickEntryViewModel = hiltViewModel()
    var showEntry by rememberSaveable { mutableStateOf(false) }

    // Choix « Configurer mon budget » : on ouvre l'onglet Budget une seule fois
    // (le flag sauvegardé évite de rebasculer dessus à chaque rotation).
    // Choix « Configurer mon budget » à l'onboarding : on ouvre l'assistant une seule fois.
    var setupOpened by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (openBudgetSetup && !setupOpened) {
            setupOpened = true
            navController.navigate(BudgetSetupRoute)
        }
    }

    // Pas de barre du bas pendant l'assistant : il est plein écran.
    val backStackEntry by navController.currentBackStackEntryAsState()

    val openEntry: (LocalDate?) -> Unit = { date ->
        entryViewModel.start(date)
        showEntry = true
    }

    val openEdit: (Transaction) -> Unit = { transaction ->
        entryViewModel.startEdit(transaction)
        showEntry = true
    }

    // « + » du widget : on ouvre la saisie, mais seulement une fois l'app déverrouillée.
    LaunchedEffect(openEntryRequest, locked) {
        if (openEntryRequest && !locked) {
            openEntry(null)
            onEntryRequestHandled()
        }
    }
    val inSetup = backStackEntry?.destination?.let {
        it.hasRoute<BudgetSetupRoute>() || it.hasRoute<CycleClosingRoute>() || it.hasRoute<SettingsRoute>()
    } == true
    // Après chaque ajout, modification ou suppression : on ferme la sheet et on propose d'annuler.
    LaunchedEffect(entryViewModel) {
        entryViewModel.events.collect { event ->
            showEntry = false
            val isRefund = event.transaction.type == TransactionType.REFUND
            val tx = event.transaction
            val subject = when (tx.type) {
                TransactionType.EXPENSE -> "Dépense"
                TransactionType.REFUND -> "Remboursement"
                TransactionType.INCOME -> "Revenu"
            }
            val agreement = if (tx.type == TransactionType.EXPENSE) "e" else "" // ajoutée / ajouté
            val verb = when (event) {
                is EntryEvent.Added -> "ajouté"
                is EntryEvent.Updated -> "modifié"
                is EntryEvent.Deleted -> "supprimé"
            }
            val detail = if (tx.isIncome) tx.note else event.categoryName
            val result = snackbarHostState.showSnackbar(
                message = listOfNotNull("$subject $verb$agreement", tx.amount.format(), detail).joinToString(" · "),
                actionLabel = "Annuler",
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) entryViewModel.undo(event)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { if (!inSetup) BudgetBottomBar(navController, onAddClick = { openEntry(null) }) },
    ) { innerPadding ->
        AppNavHost(
            navController,
            onAddClick = openEntry,
            onEditTransaction = openEdit,
            modifier = Modifier.padding(innerPadding),
        )
    }

    // La sheet est une fenêtre à part : on ne l'affiche jamais par-dessus l'écran verrouillé.
    if (showEntry && !locked) {
        QuickEntrySheet(entryViewModel, onDismiss = { showEntry = false })
    }
}
