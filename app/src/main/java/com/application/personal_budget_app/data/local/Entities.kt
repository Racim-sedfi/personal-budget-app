package com.application.personal_budget_app.data.local

import androidx.room.*
import com.application.personal_budget_app.domain.closing.CycleOutcome
import com.application.personal_budget_app.domain.model.Frequency
import com.application.personal_budget_app.domain.model.TransactionType
import java.time.LocalDate

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val capCents: Long?,
    val isFuse: Boolean,
    val position: Int,
)

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.RESTRICT,   // on ne supprime pas une catégorie utilisée
    )],
    indices = [Index("categoryId"), Index("date")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val type: TransactionType,   // Room stocke le nom de l'enum
    val categoryId: Long?,
    val date: LocalDate,
    val note: String?,
)

@Entity(tableName = "no_expense_days")
data class NoExpenseDayEntity(@PrimaryKey val date: LocalDate)

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amountCents: Long,
    val dayOfMonth: Int,
)

@Entity(tableName = "planned_savings")
data class PlannedSavingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val amountCents: Long,
    val dayOfMonth: Int,
)

@Entity(tableName = "fixed_charges")
data class FixedChargeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amountCents: Long,
    val frequency: Frequency,
    val nextDueDate: LocalDate,
)

@Entity(tableName = "closed_cycles")
data class ClosedCycleEntity(
    @PrimaryKey val start: LocalDate,
    val end: LocalDate,
    val outcome: CycleOutcome,
    val leftoverCents: Long,
)