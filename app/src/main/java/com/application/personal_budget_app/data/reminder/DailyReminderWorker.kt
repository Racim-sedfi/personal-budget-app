package com.application.personal_budget_app.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.application.personal_budget_app.domain.reminder.shouldRemind
import com.application.personal_budget_app.domain.repository.DayStatusRepository
import com.application.personal_budget_app.domain.repository.SettingsRepository
import com.application.personal_budget_app.domain.repository.TransactionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate

/** Lancé chaque soir par WorkManager : notifie seulement si la journée est vide. */
class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    /** Un Worker n'est pas créé par Hilt : on va chercher les repositories nous-mêmes. */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun settings(): SettingsRepository
        fun transactions(): TransactionRepository
        fun dayStatus(): DayStatusRepository
        fun clock(): Clock
    }

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
        val settings = deps.settings().settings.first()
        val today = LocalDate.now(deps.clock())
        val dayFilled = deps.transactions().observeBetween(today, today).first().isNotEmpty() ||
            today in deps.dayStatus().observeNoExpenseDays(today, today).first()

        if (shouldRemind(settings.reminderEnabled, settings.onboardingDone, dayFilled)) {
            ReminderNotifier.show(applicationContext)
        }
        return Result.success()
    }
}
