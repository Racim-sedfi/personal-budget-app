package com.application.personal_budget_app.domain.repository

import com.application.personal_budget_app.domain.closing.ClosedCycle
import com.application.personal_budget_app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<Transaction>>
    suspend fun add(transaction: Transaction): Long
    suspend fun delete(id: Long)
    suspend fun update(transaction: Transaction)
}

interface CategoryRepository {
    /** Toutes, archivées comprises. */
    fun observeAll(): Flow<List<Category>>
    /** Sans les archivées. */
    fun observeActive(): Flow<List<Category>>
    suspend fun update(category: Category)
    suspend fun add(category: Category): Long
    /** Supprime si elle n'a jamais servi, sinon l'archive (ses dépenses restent visibles). */
    suspend fun remove(category: Category)
}

/** Programme le rappel quotidien (WorkManager côté Android). */
interface ReminderScheduler {
    fun schedule(minutesOfDay: Int)
    fun cancel()
}

interface DataResetRepository {
    /** Efface tout (base + réglages) et remet les enveloppes par défaut. */
    suspend fun resetAll()
}

interface DayStatusRepository {
    fun observeNoExpenseDays(start: LocalDate, end: LocalDate): Flow<Set<LocalDate>>
    suspend fun declareNoExpense(date: LocalDate)
    suspend fun clearNoExpense(date: LocalDate)
}

interface BudgetRepository {
    fun observeIncomes(): Flow<List<Income>>
    fun observeSavings(): Flow<List<PlannedSaving>>
    fun observeFixedCharges(): Flow<List<FixedCharge>>
    suspend fun upsertIncome(income: Income)
    suspend fun upsertSaving(saving: PlannedSaving)
    suspend fun upsertFixedCharge(charge: FixedCharge)

    suspend fun deleteIncome(id: Long)
    suspend fun deleteSaving(id: Long)
    suspend fun deleteFixedCharge(id: Long)
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setCycleStartDay(day: Int)
    suspend fun setMode(mode: BudgetMode)
    suspend fun completeOnboarding()
    suspend fun setLockEnabled(enabled: Boolean)
    suspend fun setLockDelay(minutes: Int)
    suspend fun setCurrency(currency: AppCurrency)
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setReminderTime(minutes: Int)
    suspend fun setHideInRecents(hide: Boolean)
}

interface ClosedCycleRepository {
    fun observe(start: LocalDate): Flow<ClosedCycle?>
    suspend fun close(cycle: ClosedCycle)
}