package com.application.personal_budget_app.ui.format

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.application.personal_budget_app.domain.model.AppCurrency
import com.application.personal_budget_app.domain.model.Money

/**
 * Monnaie d'affichage, lue par Money.format().
 * C'est un état Compose : quand elle change, tout ce qui affiche un montant se redessine.
 */
object CurrencyState {
    var current by mutableStateOf(AppCurrency.EUR)

    /** À appeler une fois au démarrage (MainActivity). */
    fun install() {
        Money.displayCurrency = { current }
    }
}