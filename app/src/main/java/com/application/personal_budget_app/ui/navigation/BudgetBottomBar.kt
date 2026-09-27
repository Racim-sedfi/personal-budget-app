package com.application.personal_budget_app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.application.personal_budget_app.R
import com.application.personal_budget_app.ui.theme.*

@Composable
fun BudgetBottomBar(navController: NavHostController, onAddClick: () -> Unit) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination

    Column(Modifier.background(Background).navigationBarsPadding()) {
        HorizontalDivider(color = Divider)
        Row(
            Modifier.fillMaxWidth().height(72.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopLevelDestination.entries.forEachIndexed { index, item ->
                if (index == 2) AddButton(onAddClick, Modifier.weight(1f))
                val selected = current?.hierarchy?.any { it.hasRoute(item.route::class) } == true
                NavItem(item, selected, Modifier.weight(1f)) {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    item: TopLevelDestination,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val color = if (selected) Ink else TextSecondary
    Column(
        modifier
            .height(56.dp)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(item.icon), contentDescription = null, tint = color)
        Spacer(Modifier.height(4.dp))
        Text(
            item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = color,
        )
    }
}

@Composable
private fun AddButton(onClick: () -> Unit, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(56.dp)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(BudgetGradients.primaryButton)
                .semantics { contentDescription = "Ajouter une dépense" },
        ) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null, tint = Background)
        }
    }
}