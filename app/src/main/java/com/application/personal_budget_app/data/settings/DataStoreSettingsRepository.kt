package com.application.personal_budget_app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.application.personal_budget_app.domain.model.AppCurrency
import com.application.personal_budget_app.domain.model.AppSettings
import com.application.personal_budget_app.domain.model.BudgetMode
import com.application.personal_budget_app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreSettingsRepository @Inject constructor(
    private val store: DataStore<Preferences>,
) : SettingsRepository {

    private object Keys {
        val START_DAY = intPreferencesKey("cycle_start_day")
        val MODE = stringPreferencesKey("budget_mode")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val LOCK_ENABLED = booleanPreferencesKey("lock_enabled")
        val LOCK_DELAY = intPreferencesKey("lock_delay_minutes")
        val CURRENCY = stringPreferencesKey("currency")
    }

    override val settings: Flow<AppSettings> = store.data.map { prefs ->
        AppSettings(
            cycleStartDay = prefs[Keys.START_DAY] ?: 1,
            mode = prefs[Keys.MODE]?.let { BudgetMode.valueOf(it) } ?: BudgetMode.OBSERVATION,
            onboardingDone = prefs[Keys.ONBOARDING_DONE] ?: false,
            lockEnabled = prefs[Keys.LOCK_ENABLED] ?: false,
            lockDelayMinutes = prefs[Keys.LOCK_DELAY] ?: 1,
            currency = prefs[Keys.CURRENCY]?.let { AppCurrency.fromCode(it) } ?: AppCurrency.EUR,
        )
    }

    override suspend fun setCycleStartDay(day: Int) {
        require(day in 1..28)
        store.edit { it[Keys.START_DAY] = day }
    }

    override suspend fun setMode(mode: BudgetMode) {
        store.edit { it[Keys.MODE] = mode.name }
    }

    override suspend fun completeOnboarding() {
        store.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    override suspend fun setLockEnabled(enabled: Boolean) {
        store.edit { it[Keys.LOCK_ENABLED] = enabled }
    }

    override suspend fun setLockDelay(minutes: Int) {
        require(minutes >= 0)
        store.edit { it[Keys.LOCK_DELAY] = minutes }
    }
    override suspend fun setCurrency(currency: AppCurrency) {
        store.edit { it[Keys.CURRENCY] = currency.code }
    }
}