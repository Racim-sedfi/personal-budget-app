package com.application.personal_budget_app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.domain.onboarding.StartChoice
import com.application.personal_budget_app.ui.AppViewModel
import com.application.personal_budget_app.ui.BudgetAppRoot
import com.application.personal_budget_app.ui.format.CurrencyState
import com.application.personal_budget_app.ui.lock.AppLockGate
import com.application.personal_budget_app.ui.onboarding.OnboardingScreen
import com.application.personal_budget_app.ui.theme.Background
import com.application.personal_budget_app.ui.theme.PersonalbudgetappTheme
import dagger.hilt.android.AndroidEntryPoint
import android.view.WindowManager
import androidx.compose.runtime.LaunchedEffect

/** FragmentActivity (et non ComponentActivity) : BiometricPrompt en a besoin. */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CurrencyState.install()
        enableEdgeToEdge()
        setContent {
            PersonalbudgetappTheme {
                val appViewModel: AppViewModel = hiltViewModel()
                val settings by appViewModel.settings.collectAsStateWithLifecycle()
                var openBudgetSetup by rememberSaveable { mutableStateOf(false) }
                var justOnboarded by rememberSaveable { mutableStateOf(false) }

                val s = settings

                // FLAG_SECURE : aperçu vide dans les apps récentes, captures d'écran bloquées.
                val hide = s?.hideInRecents == true
                LaunchedEffect(hide) {
                    if (hide) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }

                when {
                    s == null -> Box(Modifier.fillMaxSize().background(Background))
                    !s.onboardingDone -> OnboardingScreen(onChoiceMade = { choice ->
                        openBudgetSetup = choice == StartChoice.CONFIGURE
                        justOnboarded = true   // on vient de s'authentifier : pas de 2e demande tout de suite
                    })
                    else -> AppLockGate(
                        enabled = s.lockEnabled,
                        delayMinutes = s.lockDelayMinutes,
                        startUnlocked = justOnboarded,
                    ) {
                        BudgetAppRoot(openBudgetSetup = openBudgetSetup)
                    }
                }
            }
        }
    }
}