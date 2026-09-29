package com.application.personal_budget_app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.model.AppSettings
import com.application.personal_budget_app.domain.repository.SettingsRepository
import com.application.personal_budget_app.ui.format.CurrencyState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class AppViewModel @Inject constructor(settingsRepository: SettingsRepository) : ViewModel() {
    /** null tant que DataStore n'a pas répondu : on n'affiche rien pour éviter un flash du mauvais écran. */
    val settings: StateFlow<AppSettings?> = settingsRepository.settings
        .onEach { CurrencyState.current = it.currency }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}