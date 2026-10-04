package com.application.personal_budget_app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC, id DESC")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<TransactionEntity>>

    @Insert suspend fun insert(entity: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    @Update suspend fun update(entity: TransactionEntity)
}

@Dao
interface CategoryDao {
    /** Toutes, archivées comprises (noms dans l'historique et l'analyse). */
    @Query("SELECT * FROM categories ORDER BY position")
    fun observeAll(): Flow<List<CategoryEntity>>

    /** Celles qu'on peut encore utiliser : saisie, budget. */
    @Query("SELECT * FROM categories WHERE archived = 0 ORDER BY position")
    fun observeActive(): Flow<List<CategoryEntity>>

    @Update suspend fun update(entity: CategoryEntity)

    @Insert suspend fun insert(entity: CategoryEntity): Long

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM categories")
    suspend fun nextPosition(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :id")
    suspend fun transactionCount(id: Long): Int

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface NoExpenseDayDao {
    @Query("SELECT date FROM no_expense_days WHERE date BETWEEN :start AND :end")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<LocalDate>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: NoExpenseDayEntity)

    @Query("DELETE FROM no_expense_days WHERE date = :date")
    suspend fun delete(date: LocalDate)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM incomes") fun observeIncomes(): Flow<List<IncomeEntity>>
    @Query("SELECT * FROM planned_savings") fun observeSavings(): Flow<List<PlannedSavingEntity>>
    @Query("SELECT * FROM fixed_charges ORDER BY nextDueDate") fun observeFixedCharges(): Flow<List<FixedChargeEntity>>

    @Upsert suspend fun upsertIncome(entity: IncomeEntity)
    @Upsert suspend fun upsertSaving(entity: PlannedSavingEntity)
    @Upsert suspend fun upsertFixedCharge(entity: FixedChargeEntity)

    @Query("DELETE FROM incomes WHERE id = :id") suspend fun deleteIncome(id: Long)
    @Query("DELETE FROM planned_savings WHERE id = :id") suspend fun deleteSaving(id: Long)
    @Query("DELETE FROM fixed_charges WHERE id = :id") suspend fun deleteFixedCharge(id: Long)
}

@Dao
interface ClosedCycleDao {
    @Query("SELECT * FROM closed_cycles WHERE start = :start")
    fun observe(start: LocalDate): Flow<ClosedCycleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ClosedCycleEntity)
}