package com.application.personal_budget_app.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/** Navigation vers un onglet : pas de doublon dans la pile, l'état de chaque onglet est conservé. */
fun NavHostController.navigateToTopLevel(route: Any) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}