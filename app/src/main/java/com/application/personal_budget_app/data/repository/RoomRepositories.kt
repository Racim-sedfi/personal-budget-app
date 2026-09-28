package com.application.personal_budget_app.data.repository

import com.application.personal_budget_app.data.local.*
import com.application.personal_budget_app.domain.closing.ClosedCycle
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class RoomTransactionRepository @Inject constructor(
    private val dao: TransactionDao,
) : TransactionRepository {
    override fun observeBetween(start: LocalDate, end: LocalDate) =
        dao.observeBetween(start, end).map { list -> list.map { it.toDomain() } }
    override suspend fun add(transaction: Transaction) = dao.insert(transaction.toEntity())
    override suspend fun delete(id: Long) = dao.delete(id)
    override suspend fun update(transaction: Transaction) = dao.update(transaction.toEntity())
}

class RoomCategoryRepository @Inject constructor(
    private val dao: CategoryDao,
) : CategoryRepository {
    override fun observeAll() = dao.observeAll().map { list -> list.map { it.toDomain() } }
    override suspend fun update(category: Category) = dao.update(category.toEntity())
}

class RoomDayStatusRepository @Inject constructor(
    private val dao: NoExpenseDayDao,
) : DayStatusRepository {
    override fun observeNoExpenseDays(start: LocalDate, end: LocalDate): Flow<Set<LocalDate>> =
        dao.observeBetween(start, end).map { it.toSet() }
    override suspend fun declareNoExpense(date: LocalDate) = dao.insert(NoExpenseDayEntity(date))
    override suspend fun clearNoExpense(date: LocalDate) = dao.delete(date)
}

class RoomBudgetRepository @Inject constructor(
    private val dao: BudgetDao,
) : BudgetRepository {
    override fun observeIncomes() = dao.observeIncomes().map { l -> l.map { it.toDomain() } }
    override fun observeSavings() = dao.observeSavings().map { l -> l.map { it.toDomain() } }
    override fun observeFixedCharges() = dao.observeFixedCharges().map { l -> l.map { it.toDomain() } }
    override suspend fun upsertIncome(income: Income) = dao.upsertIncome(income.toEntity())
    override suspend fun upsertSaving(saving: PlannedSaving) = dao.upsertSaving(saving.toEntity())
    override suspend fun upsertFixedCharge(charge: FixedCharge) = dao.upsertFixedCharge(charge.toEntity())
    override suspend fun deleteIncome(id: Long) = dao.deleteIncome(id)
    override suspend fun deleteSaving(id: Long) = dao.deleteSaving(id)
    override suspend fun deleteFixedCharge(id: Long) = dao.deleteFixedCharge(id)
}

class RoomClosedCycleRepository @Inject constructor(
    private val dao: ClosedCycleDao,
) : ClosedCycleRepository {
    override fun observe(start: LocalDate): Flow<ClosedCycle?> = dao.observe(start).map { it?.toDomain() }
    override suspend fun close(cycle: ClosedCycle) = dao.insert(cycle.toEntity())
}