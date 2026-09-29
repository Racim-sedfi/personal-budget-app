package com.application.personal_budget_app.ui.lock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLockViewModelTest {
    private val vm = AppLockViewModel()

    @Test fun `locked at launch when enabled`() {
        vm.start(lockNow = true)
        assertTrue(vm.locked.value)
    }

    @Test fun `start only counts once`() {
        vm.start(lockNow = false)
        vm.start(lockNow = true)   // ex. après une rotation
        assertFalse(vm.locked.value)
    }

    @Test fun `short trip outside does not lock`() {
        vm.start(false)
        vm.onStop(0)
        vm.onStart(enabled = true, delayMinutes = 1, nowMillis = 30_000)
        assertFalse(vm.locked.value)
    }

    @Test fun `locks once the delay has passed`() {
        vm.start(false)
        vm.onStop(0)
        vm.onStart(enabled = true, delayMinutes = 1, nowMillis = 60_000)
        assertTrue(vm.locked.value)
    }

    @Test fun `immediate delay locks on every return`() {
        vm.start(false)
        vm.onStop(0)
        vm.onStart(enabled = true, delayMinutes = 0, nowMillis = 1)
        assertTrue(vm.locked.value)
    }

    @Test fun `never locks when disabled`() {
        vm.start(false)
        vm.onStop(0)
        vm.onStart(enabled = false, delayMinutes = 0, nowMillis = 10_000_000)
        assertFalse(vm.locked.value)
    }

    @Test fun `unlock opens the app`() {
        vm.start(true)
        vm.unlock()
        assertFalse(vm.locked.value)
    }
}