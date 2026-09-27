package com.application.personal_budget_app.ui.entry

import com.application.personal_budget_app.domain.entry.KeypadKey
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.domain.repository.CategoryRepository
import com.application.personal_budget_app.domain.repository.TransactionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.*

private class FakeTransactions : TransactionRepository {
    val added = mutableListOf<Transaction>()
    val deleted = mutableListOf<Long>()
    override fun observeBetween(start: LocalDate, end: LocalDate) = flowOf(added.toList())
    override suspend fun add(transaction: Transaction): Long { added += transaction; return added.size.toLong() }
    override suspend fun delete(id: Long) { deleted += id }
}

private class FakeCategories(private val list: List<Category>) : CategoryRepository {
    override fun observeAll() = flowOf(list)
    override suspend fun update(category: Category) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class QuickEntryViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneOffset.UTC)
    private val categories = listOf(Category(1, "Courses", "cart"), Category(2, "Restaurants", "restaurant"))
    private lateinit var repo: FakeTransactions
    private lateinit var vm: QuickEntryViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher) // viewModelScope utilise Dispatchers.Main
        repo = FakeTransactions()
        vm = QuickEntryViewModel(repo, FakeCategories(categories), clock)
    }

    @After fun tearDown() = Dispatchers.resetMain()

    private fun typeAmount(vararg keys: KeypadKey) = keys.forEach(vm::onKey)

    @Test fun `first category is preselected`() =
        assertEquals(1L, vm.state.value.selectedCategoryId)

    @Test fun `cannot save without an amount`() {
        assertFalse(vm.state.value.canSave)
        vm.save()
        assertTrue(repo.added.isEmpty())
    }

    @Test fun `saving creates an expense for today and resets the form`() {
        typeAmount(KeypadKey.Digit(8), KeypadKey.Comma, KeypadKey.Digit(4))
        vm.onCategorySelected(2)
        vm.save()

        val tx = repo.added.single()
        assertEquals(Money(840), tx.amount)
        assertEquals(2L, tx.categoryId)
        assertEquals(LocalDate.of(2026, 10, 12), tx.date)
        assertEquals(TransactionType.EXPENSE, tx.type)
        assertEquals("", vm.state.value.amount.raw)
    }

    @Test fun `saved event allows undo`() = runTest(dispatcher) {
        val events = mutableListOf<SavedTransaction>()
        backgroundScope.launch { vm.saved.toList(events) }

        typeAmount(KeypadKey.Digit(5))
        vm.save()

        assertEquals("Courses", events.single().categoryName)
        vm.undo(events.single().id)
        assertEquals(listOf(1L), repo.deleted)
    }
}