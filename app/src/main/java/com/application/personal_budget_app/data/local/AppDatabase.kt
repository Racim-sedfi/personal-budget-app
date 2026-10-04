package com.application.personal_budget_app.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CategoryEntity::class, TransactionEntity::class, NoExpenseDayEntity::class,
        IncomeEntity::class, PlannedSavingEntity::class, FixedChargeEntity::class,
        ClosedCycleEntity::class,
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = LockDefaultCategories::class),
     ],
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
    override fun onCreate(db: SupportSQLiteDatabase) = seed(db)

    companion object {
        /** Les enveloppes par défaut. Courses et Imprévus sont le minimum : verrouillées. */
        fun seed(db: SupportSQLiteDatabase) {
            // Le minimum. Les autres sont choisies à l'onboarding ou créées dans Budget.
            // Imprévus en position 1000 : toujours en bas de la liste.
            db.execSQL(
                "INSERT INTO categories (name, iconKey, capCents, isFuse, position, isLocked, archived) VALUES ('Courses', 'cart', NULL, 0, 0, 1, 0)",
            )
            db.execSQL(
                "INSERT INTO categories (name, iconKey, capCents, isFuse, position, isLocked, archived) VALUES ('Imprévus', 'umbrella', NULL, 1, $FUSE_POSITION, 1, 0)",
            )
        }
    }
}

/** Position d'Imprévus : après toutes les enveloppes qu'on ajoute. */
const val FUSE_POSITION = 1000

/** v3 → v4 : les colonnes sont ajoutées par Room ; on verrouille Courses et Imprévus déjà présentes. */
class LockDefaultCategories : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE categories SET isLocked = 1 WHERE isFuse = 1 OR name = 'Courses'")
        db.execSQL("UPDATE categories SET position = $FUSE_POSITION WHERE isFuse = 1")
    }
}