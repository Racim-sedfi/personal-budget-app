package com.application.personal_budget_app.domain.model

private const val NBSP = '\u00A0'

/** Monnaies proposées. Le format des nombres reste français (1 234,56), seul le symbole change. */
enum class AppCurrency(val code: String, val symbol: String, val label: String) {
    EUR("EUR", "€", "Euro"),
    USD("USD", "$", "Dollar américain"),
    GBP("GBP", "£", "Livre sterling"),
    CHF("CHF", "CHF", "Franc suisse"),
    CAD("CAD", "\$${NBSP}CA", "Dollar canadien"),
    MAD("MAD", "DH", "Dirham marocain"),
    DZD("DZD", "DA", "Dinar algérien");

    companion object {
        fun fromCode(code: String): AppCurrency? = entries.firstOrNull { it.code == code }
    }
}