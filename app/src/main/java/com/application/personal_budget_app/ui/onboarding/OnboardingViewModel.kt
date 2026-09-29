package com.application.personal_budget_app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.personal_budget_app.domain.cycle.BudgetCycle
import com.application.personal_budget_app.domain.model.BudgetMode
import com.application.personal_budget_app.domain.onboarding.OnboardingStep
import com.application.personal_budget_app.domain.onboarding.StartChoice
import com.application.personal_budget_app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.PRIVACY,
    val startDay: Int = 1,
    val choice: StartChoice = StartChoice.OBSERVE,
    val cycle: BudgetCycle,
    val saving: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: SettingsRepository,
    clock: Clock,
) : ViewModel() {

    private val today = LocalDate.now(clock)
    private val _state = MutableStateFlow(OnboardingUiState(cycle = BudgetCycle.containing(today, 1)))
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun next() = _state.update { it.copy(step = it.step.next()) }
    fun back() = _state.update { it.copy(step = it.step.previous()) }

    fun pickStartDay(day: Int) = _state.update {
        it.copy(startDay = day, cycle = BudgetCycle.containing(today, day))
    }

    fun pickChoice(choice: StartChoice) = _state.update { it.copy(choice = choice) }

    fun finish(enableLock: Boolean = false) {
        if (_state.value.saving) return // double appui
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            settings.setCycleStartDay(_state.value.startDay)
            settings.setMode(BudgetMode.OBSERVATION) // le budget s'active depuis l'onglet Budget
            if (enableLock) settings.setLockEnabled(true)
            settings.completeOnboarding()           // en dernier
        }
    }
}