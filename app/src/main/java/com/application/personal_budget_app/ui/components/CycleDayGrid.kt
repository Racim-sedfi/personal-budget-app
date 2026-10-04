package com.application.personal_budget_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.application.personal_budget_app.ui.theme.Background
import com.application.personal_budget_app.ui.theme.BudgetGradients
import com.application.personal_budget_app.ui.theme.Ink

/** Les jours 1 à 28 en grille de 7 : le cycle doit exister dans tous les mois. Partagé par l'onboarding et les Paramètres. */
@Composable
fun CycleDayGrid(selected: Int, onPick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.selectableGroup().semantics { contentDescription = "Jour de début du cycle" },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        (1..28).chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { day ->
                    val isSelected = day == selected
                    Box(
                        Modifier.weight(1f).heightIn(min = 44.dp).clip(CircleShape)
                            .background(if (isSelected) BudgetGradients.primaryButton else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                            .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onPick(day) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            day.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Background else Ink,
                        )
                    }
                }
            }
        }
    }
}
