package com.application.personal_budget_app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Tant que l'app tourne : chaque saisie, réglage ou changement de budget rafraîchit le widget. */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val transactions: TransactionRepository,
    private val dayStatus: DayStatusRepository,
    private val categories: CategoryRepository,
    private val budget: BudgetRepository,
    private val clock: Clock,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun start() {
        settings.settings
            .flatMapLatest { s ->
                val cycle = BudgetCycle.containing(LocalDate.now(clock), s.cycleStartDay)
                merge(
                    flowOf(Unit),
                    transactions.observeBetween(cycle.start, cycle.end).map { },
                    dayStatus.observeNoExpenseDays(cycle.start, cycle.end).map { },
                    categories.observeAll().map { },
                    budget.observeIncomes().map { },
                    budget.observeSavings().map { },
                    budget.observeFixedCharges().map { },
                )
            }
            .debounce(1_000)   // plusieurs changements d'affilée : une seule mise à jour
            // Le widget ne doit jamais faire planter l'app : une erreur est ignorée, la suivante réessaie.
            .onEach { runCatching { BudgetWidget().updateAll(context) } }
            .launchIn(scope)
    }
}
