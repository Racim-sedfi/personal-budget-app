package com.application.personal_budget_app.domain.model

import java.time.LocalDate

enum class TransactionType { EXPENSE, REFUND }

data class Transaction(
    val id: Long = 0,
    val amount: Money,          // toujours positif
    val type: TransactionType,
    val categoryId: Long,
    val date: LocalDate,
    val note: String? = null,
) {
    init { require(amount.cents > 0) { "Le montant doit être positif" } }

    /** Une dépense compte en plus, un remboursement en moins. */
    val signedAmount: Money get() = if (type == TransactionType.EXPENSE) amount else -amount
}

/** Dépenses nettes (dépenses − remboursements). */
fun Iterable<Transaction>.netSpent(): Money = map { it.signedAmount }.sum()