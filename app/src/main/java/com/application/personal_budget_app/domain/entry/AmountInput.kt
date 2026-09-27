package com.application.personal_budget_app.domain.entry

import com.application.personal_budget_app.domain.model.Money

sealed interface KeypadKey {
    data class Digit(val value: Int) : KeypadKey {
        init { require(value in 0..9) }
    }
    data object Comma : KeypadKey
    data object Delete : KeypadKey
}

/** Ce que l'utilisateur a tapé, par exemple "8,4". */
data class AmountInput(val raw: String = "") {

    fun press(key: KeypadKey): AmountInput = when (key) {
        KeypadKey.Delete -> AmountInput(raw.dropLast(1))
        KeypadKey.Comma -> if (',' in raw) this else AmountInput(raw.ifEmpty { "0" } + ",")
        is KeypadKey.Digit -> appendDigit(key.value)
    }

    private fun appendDigit(digit: Int): AmountInput {
        val comma = raw.indexOf(',')
        if (comma >= 0 && raw.length - comma > 2) return this          // 2 décimales max
        if (comma < 0 && raw.length >= MAX_INTEGER_DIGITS) return this // 999 999 € max
        return AmountInput(if (raw == "0") "$digit" else raw + digit)
    }

    val money: Money
        get() {
            if (raw.isEmpty()) return Money.ZERO
            val euros = raw.substringBefore(',').ifEmpty { "0" }.toLong()
            val cents = raw.substringAfter(',', "").padEnd(2, '0').toLong()
            return Money(euros * 100 + cents)
        }

    val isValid: Boolean get() = money.cents > 0

    /** Texte affiché en grand : "0", "8,4", "1 250". */
    val display: String
        get() {
            if (raw.isEmpty()) return "0"
            val grouped = raw.substringBefore(',').ifEmpty { "0" }
                .reversed().chunked(3).joinToString("\u00A0").reversed()
            return if (',' in raw) grouped + "," + raw.substringAfter(',') else grouped
        }

    companion object { const val MAX_INTEGER_DIGITS = 6 }
}