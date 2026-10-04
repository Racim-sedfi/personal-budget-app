package com.application.personal_budget_app.ui.onboarding

import com.application.personal_budget_app.domain.model.AppCurrency
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
import com.application.personal_budget_app.domain.model.Category
import com.application.personal_budget_app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.flowOf

private class FakeSettings : SettingsRepository {
    val calls = mutableListOf<String>()
    override val settings = MutableStateFlow(AppSettings())
    override suspend fun setCycleStartDay(day: Int) { calls += "startDay=$day" }
    override suspend fun setMode(mode: BudgetMode) { calls += "mode=$mode" }
    override suspend fun completeOnboarding() { calls += "done" }
    override suspend fun setLockEnabled(enabled: Boolean) { calls += "lock=$enabled" }
    override suspend fun setLockDelay(minutes: Int) = Unit
    override suspend fun setCurrency(currency: AppCurrency) { calls += "currency=${currency.code}" }
}

private class FakeCategories : CategoryRepository {
    val added = mutableListOf<Category>()
    override fun observeAll() = flowOf(emptyList<Category>())
    override fun observeActive() = flowOf(emptyList<Category>())
    override suspend fun update(category: Category) = Unit
    override suspend fun add(category: Category): Long { added += category; return added.size.toLong() }
    override suspend fun remove(category: Category) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-12T10:00:00Z"), ZoneOffset.UTC)
    private lateinit var settings: FakeSettings
    private lateinit var categories: FakeCategories
    private lateinit var vm: OnboardingViewModel

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        settings = FakeSettings()
        categories = FakeCategories()
        vm = OnboardingViewModel(settings, categories, clock)
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `picking a start day updates the cycle preview`() {
        vm.pickStartDay(25)
        assertEquals(LocalDate.of(2026, 9, 25), vm.state.value.cycle.start)
        assertEquals(LocalDate.of(2026, 10, 24), vm.state.value.cycle.end)
    }

    @Test fun `nothing is saved before the end`() {
        vm.next(); vm.pickStartDay(25); vm.next()        // → Enveloppes
        assertEquals(OnboardingStep.ENVELOPES, vm.state.value.step)
        vm.toggleEnvelope("Loisirs"); vm.next()          // → Comment veux-tu commencer ?
        assertEquals(OnboardingStep.START_CHOICE, vm.state.value.step)
        assertEquals(emptyList<String>(), settings.calls)
        assertEquals(emptyList<Category>(), categories.added)
    }

    @Test fun `finish saves everything, onboarding flag last`() {
        vm.pickStartDay(25)
        vm.pickCurrency(AppCurrency.MAD)
        vm.finish()
        assertEquals(listOf("startDay=25", "currency=MAD", "mode=OBSERVATION", "done"), settings.calls)
    }

    @Test fun `double tap on finish saves only once`() {
        vm.finish(); vm.finish()
        assertEquals(1, settings.calls.count { it == "done" })
    }

    @Test fun `finish can enable the lock before completing`() {
        vm.pickCurrency(AppCurrency.EUR)
        vm.finish(enableLock = true)
        assertEquals(listOf("startDay=1", "currency=EUR", "mode=OBSERVATION", "lock=true", "done"), settings.calls)
    }

    @Test fun `only chosen envelopes are created`() {
        vm.toggleEnvelope("Loisirs")
        vm.toggleEnvelope("Santé")
        vm.toggleEnvelope("Santé")   // décochée
        vm.finish()
        assertEquals(listOf("Loisirs"), categories.added.map { it.name })
    }
}
