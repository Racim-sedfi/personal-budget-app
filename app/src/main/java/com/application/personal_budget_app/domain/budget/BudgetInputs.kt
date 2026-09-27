package com.application.personal_budget_app.domain.budget

import com.application.personal_budget_app.domain.model.Money
import java.time.LocalDate
import java.time.YearMonth

private val amountPattern = Regex("""^(\d{1,7})(?:,(\d{1,2}))?$""")

/** Lit un montant tapé au clavier : "650", "48,9", "1 650,00", "12.5 €". null si invalide. */
fun parseAmount(text: String): Money? {
    val cleaned = text.trim()
        .replace("\u00A0", "").replace("\u202F", "").replace(" ", "")
        .replace("€", "")
        .replace('.', ',')
    val match = amountPattern.matchEntire(cleaned) ?: return null
    val euros = match.groupValues[1].toLong()
    val cents = match.groupValues[2].padEnd(2, '0').toLong()
    return Money(euros * 100 + cents)
}

/**
 * Prochaine date qui tombe le jour `day` (aujourd'hui compris).
 * Si le mois est trop court (un 31 en février), on prend son dernier jour.
 */
fun nextMonthlyDate(day: Int, from: LocalDate): LocalDate {
    require(day in 1..31)
    fun inMonth(month: YearMonth) = month.atDay(minOf(day, month.lengthOfMonth()))
    val thisMonth = inMonth(YearMonth.from(from))
    return if (!thisMonth.isBefore(from)) thisMonth else inMonth(YearMonth.from(from).plusMonths(1))
}