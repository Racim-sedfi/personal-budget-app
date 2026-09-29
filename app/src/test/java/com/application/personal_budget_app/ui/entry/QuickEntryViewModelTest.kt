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
    val updated = mutableListOf<Transaction>()
    override suspend fun update(transaction: Transaction) { updated += transaction }
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

    private val existing = Transaction(
        id = 42, amount = Money(840), type = TransactionType.EXPENSE,
        categoryId = 2, date = LocalDate.of(2026, 10, 10), note = "Pizzeria",
    )

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
        assertEquals("", vm.state.value.amount.raw)
    }

    @Test fun `starting on a past day saves on that day`() {
        vm.start(LocalDate.of(2026, 10, 9))
        assertFalse(vm.state.value.isToday)
        typeAmount(KeypadKey.Digit(3))
        vm.save()
        assertEquals(LocalDate.of(2026, 10, 9), repo.added.single().date)
    }

    @Test fun `added event can be undone`() = runTest(dispatcher) {
        val events = mutableListOf<EntryEvent>()
        backgroundScope.launch { vm.events.toList(events) }

        typeAmount(KeypadKey.Digit(5))
        vm.save()

        val added = events.single() as EntryEvent.Added
        assertEquals("Courses", added.categoryName)
        vm.undo(added)
        assertEquals(listOf(added.transaction.id), repo.deleted)
    }

    @Test fun `editing prefills the form`() {
        vm.startEdit(existing)
        val s = vm.state.value
        assertTrue(s.isEditing)
        assertEquals("8,40", s.amount.raw)
        assertEquals(2L, s.selectedCategoryId)
        assertEquals("Pizzeria", s.note)
        assertEquals(LocalDate.of(2026, 10, 10), s.date)
    }

    @Test fun `saving an edit updates instead of adding`() = runTest(dispatcher) {
        val events = mutableListOf<EntryEvent>()
        backgroundScope.launch { vm.events.toList(events) }

        vm.startEdit(existing)
        typeAmount(KeypadKey.Delete, KeypadKey.Delete, KeypadKey.Digit(8), KeypadKey.Digit(0)) // 8,40 → 8,80
        vm.save()

        assertTrue(repo.added.isEmpty())
        val updated = repo.updated.single()
        assertEquals(42L, updated.id)
        assertEquals(Money(880), updated.amount)

        val event = events.single() as EntryEvent.Updated
        vm.undo(event)
        assertEquals(existing, repo.updated.last()) // l'annulation remet l'ancienne version
    }

    @Test fun `deleting can be undone with the same transaction`() = runTest(dispatcher) {
        val events = mutableListOf<EntryEvent>()
        backgroundScope.launch { vm.events.toList(events) }

        vm.startEdit(existing)
        vm.delete()
        assertEquals(listOf(42L), repo.deleted)

        vm.undo(events.single())
        assertEquals(existing, repo.added.single())
    }

    @Test fun `income is saved without category`() {
        vm.onTypeChange(TransactionType.INCOME)
        typeAmount(KeypadKey.Digit(5), KeypadKey.Digit(0))
        vm.onNoteChange("Cadeau")
        vm.save()
        val tx = repo.added.single()
        assertEquals(TransactionType.INCOME, tx.type)
        assertNull(tx.categoryId)
        assertEquals("Cadeau", tx.note)
    }

    @Test fun `income needs a label`() {
        vm.onTypeChange(TransactionType.INCOME)
        typeAmount(KeypadKey.Digit(5), KeypadKey.Digit(0))
        assertFalse(vm.state.value.canSave)
        vm.save()
        assertTrue(repo.added.isEmpty())
    }
}