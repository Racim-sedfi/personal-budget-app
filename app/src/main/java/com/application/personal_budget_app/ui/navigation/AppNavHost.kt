package com.application.personal_budget_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.application.personal_budget_app.domain.model.Transaction
import com.application.personal_budget_app.ui.analysis.AnalysisScreen
import com.application.personal_budget_app.ui.budget.BudgetScreen
import com.application.personal_budget_app.ui.budget.BudgetSetupScreen
import com.application.personal_budget_app.ui.closing.CycleClosingScreen
import com.application.personal_budget_app.ui.components.PlaceholderScreen
import com.application.personal_budget_app.ui.history.HistoryScreen
import com.application.personal_budget_app.ui.home.HomeScreen
import com.application.personal_budget_app.ui.settings.SettingsScreen
import java.time.LocalDate

@Composable
fun AppNavHost(
    navController: NavHostController,
    onAddClick: (LocalDate?) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(navController, startDestination = HomeRoute, modifier = modifier) {
        composable<HomeRoute> {
            HomeScreen(
                onAddClick = { onAddClick(null) },
                onCompleteDays = { navController.navigateToTopLevel(HistoryRoute) },
                onEditBudget = { navController.navigateToTopLevel(BudgetRoute) },
                onCloseCycle = { navController.navigate(CycleClosingRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<HistoryRoute> {
            HistoryScreen(
                onAddForDate = { date -> onAddClick(date) },
                onEditTransaction = onEditTransaction,
            )
        }
        composable<AnalysisRoute> {
            AnalysisScreen(
                onOpenHistory = { navController.navigateToTopLevel(HistoryRoute) },
                onOpenBudget = { navController.navigateToTopLevel(BudgetRoute) },
            )
        }
        composable<BudgetRoute> {
            BudgetScreen(onOpenSetup = { navController.navigate(BudgetSetupRoute) })
        }
        composable<BudgetSetupRoute> {
            BudgetSetupScreen(
                onClose = { navController.popBackStack() },
                onActivated = {
                    navController.popBackStack()
                    navController.navigateToTopLevel(HomeRoute) // on montre tout de suite « Il te reste… »
                },
            )
        }
        composable<CycleClosingRoute> {
            CycleClosingScreen(
                onDone = { navController.popBackStack() },
                onOpenSetup = {
                    navController.popBackStack()
                    navController.navigate(BudgetSetupRoute)
                },
            )
        }

        composable<SettingsRoute> { SettingsScreen(onBack = { navController.popBackStack() }) }
    }
}