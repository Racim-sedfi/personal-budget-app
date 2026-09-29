package com.application.personal_budget_app.domain.model

import java.time.LocalDate

enum class TransactionType { EXPENSE, REFUND, INCOME }

data class Transaction(
    val id: Long = 0,
    val amount: Money,          // toujours positif
    val type: TransactionType,
    val categoryId: Long?,      // null uniquement pour un revenu
    val date: LocalDate,
    val note: String? = null,
) {
    init {
        require(amount.cents > 0) { "Le montant doit être positif" }
        require((type == TransactionType.INCOME) == (categoryId == null)) {
            "Un revenu n'a pas de catégorie ; une dépense ou un remboursement en a une"
        }
    }

    val isIncome: Boolean get() = type == TransactionType.INCOME

    /** Poids dans les dépenses : + dépense, − remboursement, 0 pour un revenu. */
    val spentAmount: Money
        get() = when (type) {
            TransactionType.EXPENSE -> amount
            TransactionType.REFUND -> -amount
            TransactionType.INCOME -> Money.ZERO
        }
}

/** Dépenses nettes (dépenses − remboursements). Les revenus n'en font pas partie. */
fun Iterable<Transaction>.netSpent(): Money = map { it.spentAmount }.sum()

/** Revenus imprévus (cadeau, vente, prime…). */
fun Iterable<Transaction>.extraIncome(): Money = filter { it.isIncome }.map { it.amount }.sum()