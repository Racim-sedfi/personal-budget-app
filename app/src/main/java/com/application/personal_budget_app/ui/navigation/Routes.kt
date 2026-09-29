package com.application.personal_budget_app.ui.navigation

import androidx.annotation.DrawableRes
import com.application.personal_budget_app.R
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object HistoryRoute
@Serializable data object AnalysisRoute
@Serializable data object BudgetRoute
@Serializable data object CycleClosingRoute
@Serializable data object BudgetSetupRoute
@Serializable data object SettingsRoute
enum class TopLevelDestination(
    val route: Any,
    val label: String,
    @DrawableRes val icon: Int,
) {
    Home(HomeRoute, "Accueil", R.drawable.ic_home),
    History(HistoryRoute, "Historique", R.drawable.ic_history),
    Analysis(AnalysisRoute, "Analyse", R.drawable.ic_analysis),
    Budget(BudgetRoute, "Budget", R.drawable.ic_budget),
}