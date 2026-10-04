package com.application.personal_budget_app.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.entry.AmountInput
import com.application.personal_budget_app.domain.entry.KeypadKey
import com.application.personal_budget_app.domain.model.*
import com.application.personal_budget_app.domain.repository.CategoryRepository
import com.application.personal_budget_app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class QuickEntryState(
    val date: LocalDate,
    val isToday: Boolean = true,
    val amount: AmountInput = AmountInput(),
    val type: TransactionType = TransactionType.EXPENSE,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val note: String = "",
    val detailsOpen: Boolean = false,
    /** Non null : on modifie cette transaction au lieu d'en créer une. */
    val editing: Transaction? = null,
) {
    val isEditing: Boolean get() = editing != null
    val canSave: Boolean get() = amount.isValid &&
            if (type == TransactionType.INCOME) note.isNotBlank() else selectedCategoryId != null
}

/** Ce qui vient de se passer, avec de quoi l'annuler. */
sealed interface EntryEvent {
    val transaction: Transaction
    val categoryName: String

    data class Added(override val transaction: Transaction, override val categoryName: String) : EntryEvent
    data class Updated(val previous: Transaction, override val transaction: Transaction, override val categoryName: String) : EntryEvent
    data class Deleted(override val transaction: Transaction, override val categoryName: String) : EntryEvent
}

@HiltViewModel
class QuickEntryViewModel @Inject constructor(
    private val transactions: TransactionRepository,
    categories: CategoryRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(QuickEntryState(date = LocalDate.now(clock)))
    val state: StateFlow<QuickEntryState> = _state.asStateFlow()

    private val _events = Channel<EntryEvent>(Channel.BUFFERED)
    val events: Flow<EntryEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            categories.observeActive().collect { list ->
                _state.update { s ->
                    // La catégorie choisie a pu être supprimée : on reprend la première.
                    val kept = s.selectedCategoryId?.takeIf { id -> list.any { it.id == id } }
                    s.copy(categories = list, selectedCategoryId = kept ?: list.firstOrNull()?.id)
                }
            }
        }
    }

    /** Nouvelle saisie. Sans date : aujourd'hui. */
    fun start(date: LocalDate? = null) {
        val today = LocalDate.now(clock)
        val day = date ?: today
        _state.update {
            it.copy(
                date = day, isToday = day == today,
                amount = AmountInput(), type = TransactionType.EXPENSE, note = "",
                detailsOpen = false, editing = null,
            )
        }
    }

    /** Modification : le formulaire reprend la transaction telle quelle. */
    fun startEdit(transaction: Transaction) {
        _state.update {
            it.copy(
                date = transaction.date, isToday = transaction.date == LocalDate.now(clock),
                amount = AmountInput.from(transaction.amount), type = transaction.type,
                selectedCategoryId = transaction.categoryId ?: it.selectedCategoryId,
                note = transaction.note.orEmpty(),
                detailsOpen = transaction.note != null, editing = transaction,
            )
        }
    }
    fun onKey(key: KeypadKey) = _state.update { it.copy(amount = it.amount.press(key)) }
    fun onTypeChange(type: TransactionType) = _state.update { it.copy(type = type) }
    fun onCategorySelected(id: Long) = _state.update { it.copy(selectedCategoryId = id) }
    fun onNoteChange(note: String) = _state.update { it.copy(note = note.take(80)) }
    fun openDetails() = _state.update { it.copy(detailsOpen = true) }

    fun save() {
        val s = _state.value
        if (!s.canSave) return
        val categoryId = if (s.type == TransactionType.INCOME) null else s.selectedCategoryId
        val transaction = Transaction(
            id = s.editing?.id ?: 0,
            amount = s.amount.money,
            type = s.type,
            categoryId = categoryId,
            date = s.date,
            note = s.note.trim().ifEmpty { null },
        )
        viewModelScope.launch {
            val event = if (s.editing == null) {
                val id = transactions.add(transaction)
                EntryEvent.Added(transaction.copy(id = id), categoryName(categoryId))
            } else {
                transactions.update(transaction)
                EntryEvent.Updated(s.editing, transaction, categoryName(categoryId))
            }
            _events.send(event)
            start()
        }
    }

    fun delete() {
        val original = _state.value.editing ?: return
        viewModelScope.launch {
            transactions.delete(original.id)
            _events.send(EntryEvent.Deleted(original, categoryName(original.categoryId)))
            start()
        }
    }

    /** Défait exactement ce que l'événement a fait. */
    fun undo(event: EntryEvent) {
        viewModelScope.launch {
            when (event) {
                is EntryEvent.Added -> transactions.delete(event.transaction.id)
                is EntryEvent.Updated -> transactions.update(event.previous)
                is EntryEvent.Deleted -> transactions.add(event.transaction) // même id : elle revient à l'identique
            }
        }
    }

    private fun categoryName(id: Long?): String =
        if (id == null) "Revenu imprévu"
        else _state.value.categories.firstOrNull { it.id == id }?.name ?: "Sans catégorie"
}