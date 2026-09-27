package com.application.personal_budget_app.domain.budget

import com.application.personal_budget_app.domain.model.Money
import kotlin.math.roundToInt

/**
 * Remplissage d'une barre, entre 0 et 1.
 * Au-delà de 100 %, la barre reste pleine : c'est la couleur et le texte qui signalent le dépassement.
 */
fun progressFraction(spent: Money, planned: Money): Float = when {
    spent.cents <= 0 -> 0f                  // rien dépensé, ou plus de remboursements que de dépenses
    planned.cents <= 0 -> 1f                // dépense sans rien de prévu : barre pleine
    else -> (spent.cents.toFloat() / planned.cents).coerceAtMost(1f)
}

/** Pourcentage réel, qui peut dépasser 100 (pour le texte : « 158 % »). */
fun usedPercent(spent: Money, planned: Money): Int = when {
    spent.cents <= 0 -> 0
    planned.cents <= 0 -> 100
    else -> (spent.cents * 100.0 / planned.cents).roundToInt()
}