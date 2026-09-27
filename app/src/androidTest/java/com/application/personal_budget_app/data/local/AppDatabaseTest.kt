package com.application.personal_budget_app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.application.personal_budget_app.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java,
        ).addCallback(SeedCallback()).allowMainThreadQueries().build()
    }

    @After fun tearDown() = db.close()

    @Test fun createsSevenDefaultCategoriesWithImprevusAsFuse() = runBlocking {
        val categories = db.categoryDao().observeAll().first()
        assertEquals(7, categories.size)
        assertEquals(listOf("Imprévus"), categories.filter { it.isFuse }.map { it.name })
    }

    @Test fun observesOnlyTransactionsInsideTheCycle() = runBlocking {
        val courses = db.categoryDao().observeAll().first().first().id
        val dao = db.transactionDao()
        listOf(LocalDate.of(2026, 9, 24), LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 24)).forEach {
            dao.insert(TransactionEntity(amountCents = 1000, type = TransactionType.EXPENSE, categoryId = courses, date = it, note = null))
        }

        val inCycle = dao.observeBetween(LocalDate.of(2026, 9, 25), LocalDate.of(2026, 10, 24)).first()

        assertEquals(2, inCycle.size)
        assertEquals(LocalDate.of(2026, 10, 24), inCycle.first().date) // plus récent d'abord
    }

    @Test fun declaringNoExpenseTwiceKeepsOneDay() = runBlocking {
        val day = LocalDate.of(2026, 10, 11)
        db.noExpenseDayDao().insert(NoExpenseDayEntity(day))
        db.noExpenseDayDao().insert(NoExpenseDayEntity(day))
        assertEquals(listOf(day), db.noExpenseDayDao().observeBetween(day, day).first())
    }
    @Test fun updateChangesAmountAndKeepsId() = runBlocking {
        val courses = db.categoryDao().observeAll().first().first().id
        val dao = db.transactionDao()
        val day = LocalDate.of(2026, 10, 10)
        val id = dao.insert(TransactionEntity(amountCents = 840, type = TransactionType.EXPENSE, categoryId = courses, date = day, note = null))

        dao.update(TransactionEntity(id = id, amountCents = 480, type = TransactionType.EXPENSE, categoryId = courses, date = day, note = "Boulangerie"))

        val saved = dao.observeBetween(day, day).first().single()
        assertEquals(id, saved.id)
        assertEquals(480L, saved.amountCents)
        assertEquals("Boulangerie", saved.note)
    }
}