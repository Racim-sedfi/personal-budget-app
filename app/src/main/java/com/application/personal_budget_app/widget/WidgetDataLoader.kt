package com.application.personal_budget_app.widget

import android.content.Context
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.home.buildHomeSummary
import com.application.personal_budget_app.domain.model.Money
import com.application.personal_budget_app.domain.model.netSpent
import com.application.personal_budget_app.domain.repository.*
import com.application.personal_budget_app.domain.widget.WidgetData
import com.application.personal_budget_app.domain.widget.widgetData
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate

/** Le widget n'est pas créé par Hilt : on va chercher les repositories nous-mêmes. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetDependencies {
    fun settings(): SettingsRepository
    fun transactions(): TransactionRepository
    fun dayStatus(): DayStatusRepository
    fun categories(): CategoryRepository
    fun budget(): BudgetRepository
    fun closedCycles(): ClosedCycleRepository
    fun clock(): Clock
}

/** Même calcul que l'Accueil, lu une fois. */
suspend fun loadWidgetData(context: Context): WidgetData {
    val d = EntryPointAccessors.fromApplication(context.applicationContext, WidgetDependencies::class.java)
    val settings = d.settings().settings.first()
    if (!settings.onboardingDone) return WidgetData.NotConfigured

    val today = LocalDate.now(d.clock())
    val cycle = BudgetCycle.containing(today, settings.cycleStartDay)
    val previous = cycle.shifted(-1)
    val transactions = d.transactions().observeBetween(cycle.start, cycle.end).first()
    val summary = buildHomeSummary(
        today, settings,
        transactions,
        d.dayStatus().observeNoExpenseDays(cycle.start, cycle.end).first(),
        d.categories().observeAll().first(),
        d.budget().observeIncomes().first(),
        d.budget().observeSavings().first(),
        d.budget().observeFixedCharges().first(),
        carryOver = d.closedCycles().observe(previous.start).first()?.carryOver ?: Money.ZERO,
    )
    return widgetData(settings, summary, todaySpent = transactions.filter { it.date == today }.netSpent())
}
