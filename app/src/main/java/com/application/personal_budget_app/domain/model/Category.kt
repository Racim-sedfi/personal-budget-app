package com.application.personal_budget_app.domain.model

/** Enveloppe de dépenses. cap = null : pas de plafond (cycle d'observation). */
data class Category(
    val id: Long = 0,
    val name: String,
    val iconKey: String,
    val cap: Money? = null,
    val isFuse: Boolean = false,   // Imprévus
    val position: Int = 0,
)