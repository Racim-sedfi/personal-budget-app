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
) {
    val canSave: Boolean get() = amount.isValid && selectedCategoryId != null
}

/** Envoyé après un enregistrement, pour le message avec « Annuler ». */
data class SavedTransaction(val id: Long, val amount: Money, val type: TransactionType, val categoryName: String)

@HiltViewModel
class QuickEntryViewModel @Inject constructor(
    private val transactions: TransactionRepository,
    categories: CategoryRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(QuickEntryState(date = LocalDate.now(clock)))
    val state: StateFlow<QuickEntryState> = _state.asStateFlow()

    private val _saved = Channel<SavedTransaction>(Channel.BUFFERED)
    val saved: Flow<SavedTransaction> = _saved.receiveAsFlow()

    init {
        viewModelScope.launch {
            categories.observeAll().collect { list ->
                _state.update { it.copy(categories = list, selectedCategoryId = it.selectedCategoryId ?: list.firstOrNull()?.id) }
            }
        }
    }

    /** Appelé à chaque ouverture. Sans date : aujourd'hui. */
    fun start(date: LocalDate? = null) {
        val today = LocalDate.now(clock)
        val day = date ?: today
        _state.update {
            it.copy(
                date = day, isToday = day == today,
                amount = AmountInput(), type = TransactionType.EXPENSE, note = "", detailsOpen = false,
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
        val category = s.categories.first { it.id == s.selectedCategoryId }
        viewModelScope.launch {
            val id = transactions.add(
                Transaction(
                    amount = s.amount.money,
                    type = s.type,
                    categoryId = category.id,
                    date = s.date,

                    note = s.note.trim().ifEmpty { null },
                )
            )
            _saved.send(SavedTransaction(id, s.amount.money, s.type, category.name))
            start()
        }
    }

    fun undo(id: Long) {
        viewModelScope.launch { transactions.delete(id) }
    }
}