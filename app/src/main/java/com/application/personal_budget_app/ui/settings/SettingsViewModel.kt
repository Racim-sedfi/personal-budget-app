package com.application.personal_budget_app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.model.AppCurrency
import com.application.personal_budget_app.domain.model.AppSettings
import com.application.personal_budget_app.domain.repository.DataResetRepository
import com.application.personal_budget_app.domain.repository.ReminderScheduler
import com.application.personal_budget_app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.application.personal_budget_app.domain.model.ThemeMode

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val dataReset: DataResetRepository,
    private val reminder: ReminderScheduler,
) : ViewModel() {

    val state: StateFlow<AppSettings?> = settings.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setLockEnabled(enabled: Boolean) {
        viewModelScope.launch { settings.setLockEnabled(enabled) }
    }

    fun setLockDelay(minutes: Int) {
        viewModelScope.launch { settings.setLockDelay(minutes) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun setHideInRecents(hide: Boolean) {
        viewModelScope.launch { settings.setHideInRecents(hide) }
    }

    fun setCurrency(currency: AppCurrency) {
        viewModelScope.launch { settings.setCurrency(currency) }
    }

    fun setCycleStartDay(day: Int) {
        viewModelScope.launch { settings.setCycleStartDay(day) }
    }

    /** Tout effacer : l'app repart sur l'onboarding. */
    fun resetAll() {
        reminder.cancel()
        viewModelScope.launch { dataReset.resetAll() }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setReminderEnabled(enabled)
            if (enabled) reminder.schedule(settings.settings.first().reminderMinutes) else reminder.cancel()
        }
    }

    fun setReminderTime(minutes: Int) {
        viewModelScope.launch {
            settings.setReminderTime(minutes)
            if (settings.settings.first().reminderEnabled) reminder.schedule(minutes)
        }
    }
}