package com.application.personal_budget_app.ui.onboarding

import com.application.personal_budget_app.domain.model.AppSettings
import com.application.personal_budget_app.domain.model.BudgetMode
import com.application.personal_budget_app.domain.onboarding.OnboardingStep
import com.application.personal_budget_app.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.*

private class FakeSettings : SettingsRepository {
    val calls = mutableListOf<String>()
    override val settings = MutableStateFlow(AppSettings())
    override suspend fun setCycleStartDay(day: Int) { calls += "startDay=$day" }
    override suspend fun setMode(mode: BudgetMode) { calls += "mode=$mode" }
    override suspend fun completeOnboarding() { calls += "done" }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneOffset.UTC)
    private lateinit var settings: FakeSettings
    private lateinit var vm: OnboardingViewModel

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        settings = FakeSettings()
        vm = OnboardingViewModel(settings, clock)
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `picking a start day updates the cycle preview`() {
        vm.pickStartDay(25)
        assertEquals(LocalDate.of(2026, 9, 25), vm.state.value.cycle.start)
        assertEquals(LocalDate.of(2026, 10, 24), vm.state.value.cycle.end)
    }

    @Test fun `nothing is saved before the end`() {
        vm.next(); vm.pickStartDay(25); vm.next()
        assertEquals(OnboardingStep.START_CHOICE, vm.state.value.step)
        assertEquals(emptyList<String>(), settings.calls)
    }

    @Test fun `finish saves everything, onboarding flag last`() {
        vm.pickStartDay(25)
        vm.finish()
        assertEquals(listOf("startDay=25", "mode=OBSERVATION", "done"), settings.calls)
    }

    @Test fun `double tap on finish saves only once`() {
        vm.finish(); vm.finish()
        assertEquals(1, settings.calls.count { it == "done" })
    }
}