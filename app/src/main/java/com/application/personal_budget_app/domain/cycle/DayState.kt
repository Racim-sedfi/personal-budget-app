package com.application.personal_budget_app.domain.cycle

import java.time.LocalDate

enum class DayState {
    WITH_TRANSACTIONS, // au moins une saisie
    NO_EXPENSE,        // « Aucune dépense » déclarée
    NOT_FILLED,        // jour passé sans rien : jamais affiché comme 0 €
    TODAY_OPEN,        // aujourd'hui, pas encore renseigné
    FUTURE,
}

fun dayState(
    date: LocalDate,
    today: LocalDate,
    hasTransactions: Boolean,
    declaredNoExpense: Boolean,
): DayState = when {
    date.isAfter(today) -> DayState.FUTURE
    hasTransactions -> DayState.WITH_TRANSACTIONS
    declaredNoExpense -> DayState.NO_EXPENSE
    date == today -> DayState.TODAY_OPEN
    else -> DayState.NOT_FILLED
}