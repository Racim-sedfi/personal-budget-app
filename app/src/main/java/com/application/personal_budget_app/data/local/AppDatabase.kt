package com.application.personal_budget_app.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CategoryEntity::class, TransactionEntity::class, NoExpenseDayEntity::class,
        IncomeEntity::class, PlannedSavingEntity::class, FixedChargeEntity::class,
        ClosedCycleEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun noExpenseDayDao(): NoExpenseDayDao
    abstract fun budgetDao(): BudgetDao
    abstract fun closedCycleDao(): ClosedCycleDao

    companion object { const val NAME = "budget.db" }
}

/** Crée les enveloppes par défaut, une seule fois, à la création de la base. */
class SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val defaults = listOf(
            Triple("Courses", "cart", false),
            Triple("Restaurants", "restaurant", false),
            Triple("Transports", "bus", false),
            Triple("Loisirs", "ticket", false),
            Triple("Shopping", "bag", false),
            Triple("Santé", "health", false),
            Triple("Imprévus", "umbrella", true),
        )
        defaults.forEachIndexed { index, (name, icon, fuse) ->
            db.execSQL(
                "INSERT INTO categories (name, iconKey, capCents, isFuse, position) VALUES (?, ?, NULL, ?, ?)",
                arrayOf<Any>(name, icon, if (fuse) 1 else 0, index),
            )
        }
    }
}