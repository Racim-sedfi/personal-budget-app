package com.application.personal_budget_app.domain.model

import kotlin.math.abs

private const val NBSP = '\u00A0' // espace insécable : « 1 234,56 € » ne se coupe jamais

/** Montant en centimes. Jamais de Double pour de l'argent. */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money) = Money(cents + other.cents)
    operator fun minus(other: Money) = Money(cents - other.cents)
    operator fun unaryMinus() = Money(-cents)
    override fun compareTo(other: Money) = cents.compareTo(other.cents)

    val isNegative: Boolean get() = cents < 0

    /** Format français : 1 234,56 € (signe moins typographique). */
    fun format(): String {
        val absolute = abs(cents)
        val euros = (absolute / 100).toString()
            .reversed().chunked(3).joinToString(NBSP.toString()).reversed()
        val centsPart = (absolute % 100).toString().padStart(2, '0')
        val sign = if (cents < 0) "−" else ""
        return "$sign$euros,$centsPart$NBSP€"
    }

    companion object {
        val ZERO = Money(0)
        fun euros(euros: Long, cents: Long = 0) = Money(euros * 100 + cents)
    }

    /** Arrondi au multiple supérieur de `stepEuros` € : 231,80 → 235 (pas de 5). Zéro ou négatif → 0. */
    fun roundUpTo(stepEuros: Long): Money {
        val step = stepEuros * 100
        return if (cents <= 0) ZERO else Money((cents + step - 1) / step * step)
    }
}

fun Iterable<Money>.sum(): Money = Money(sumOf { it.cents })

