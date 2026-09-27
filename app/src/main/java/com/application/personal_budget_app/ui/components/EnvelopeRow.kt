package com.application.personal_budget_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.application.personal_budget_app.ui.home.EnvelopeRowModel
import com.application.personal_budget_app.ui.theme.Divider
import com.application.personal_budget_app.ui.theme.Track

@Composable
fun EnvelopeRow(model: EnvelopeRowModel, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp).semantics(mergeDescendants = true) {},
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(painterResource(categoryIcon(model.iconKey)), contentDescription = null, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(model.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text(model.amountText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = model.amountColor)
                }
                BudgetProgressBar(fraction = model.fraction, color = model.barColor)
                if (model.statusText != null && model.statusIcon != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(painterResource(model.statusIcon), contentDescription = null, tint = model.statusColor, modifier = Modifier.size(14.dp))
                        Text(model.statusText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = model.statusColor)
                    }
                }
            }
        }
        HorizontalDivider(color = Divider)
    }
}