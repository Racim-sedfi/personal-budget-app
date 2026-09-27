package com.application.personal_budget_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.application.personal_budget_app.ui.components.PlaceholderScreen
import com.application.personal_budget_app.ui.home.HomeScreen

@Composable
fun AppNavHost(navController: NavHostController, onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = HomeRoute, modifier = modifier) {
        composable<HomeRoute> {
            HomeScreen(
                onAddClick = onAddClick,
                onCompleteDays = { navController.navigateToTopLevel(HistoryRoute) },
                onEditBudget = { navController.navigateToTopLevel(BudgetRoute) },
            )
        }
        composable<HistoryRoute> { PlaceholderScreen("Historique") }
        composable<AnalysisRoute> { PlaceholderScreen("Analyse") }
        composable<BudgetRoute> { PlaceholderScreen("Budget") }
    }
}