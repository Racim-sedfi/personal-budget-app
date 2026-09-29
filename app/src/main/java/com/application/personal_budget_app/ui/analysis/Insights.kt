package com.application.personal_budget_app.ui.analysis

import com.application.personal_budget_app.domain.analysis.Analysis

/** Deux ou trois constats simples, sans jugement. */
fun Analysis.insights(): List<String> = buildList {
    val overrun = biggestOverrun
    if (hasAverages && overrun != null) {
        add("En moyenne, ${overrun.category.name} dépasse son plafond de ${overrun.overBy!!.format()}. C'est l'enveloppe où l'écart est le plus grand.")
    }
    val average = averageSpent
    if (hasAverages && average != null && capsTotal.cents > 0) {
        add(
            if (average <= capsTotal) {
                "Tu dépenses ${average.format()} en moyenne pour ${capsTotal.format()} de plafonds. Le total tient" +
                        (if (overrun != null) " : c'est la répartition entre enveloppes qui se décale." else ".")
            } else {
                "Tu dépenses ${average.format()} en moyenne, soit ${(average - capsTotal).format()} de plus que tes plafonds."
            },
        )
    }
    fixedChargesPercent?.let { add("Tes charges fixes représentent $it % de tes revenus.") }
}