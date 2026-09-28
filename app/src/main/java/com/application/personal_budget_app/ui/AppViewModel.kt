package com.application.personal_budget_app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(settings: SettingsRepository) : ViewModel() {
    /** null tant que DataStore n'a pas répondu : on n'affiche rien pour éviter un flash du mauvais écran. */
    val onboardingDone: StateFlow<Boolean?> = settings.settings
        .map { it.onboardingDone }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}