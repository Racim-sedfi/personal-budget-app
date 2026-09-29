package com.application.personal_budget_app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.application.personal_budget_app.domain.model.AppCurrency

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CurrencyPicker(selected: AppCurrency, onPick: (AppCurrency) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppCurrency.entries.forEach { currency ->
            FilterChip(
                selected = currency == selected,
                onClick = { onPick(currency) },
                label = { Text("${currency.symbol} · ${currency.label}") },
            )
        }
    }
}