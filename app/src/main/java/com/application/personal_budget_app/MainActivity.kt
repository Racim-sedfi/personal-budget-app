package com.application.personal_budget_app

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.domain.onboarding.StartChoice
import com.application.personal_budget_app.ui.AppViewModel
import com.application.personal_budget_app.ui.BudgetAppRoot
import com.application.personal_budget_app.ui.onboarding.OnboardingScreen
import com.application.personal_budget_app.ui.theme.Background
import com.application.personal_budget_app.ui.theme.PersonalbudgetappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalbudgetappTheme {
                val appViewModel: AppViewModel = hiltViewModel()
                val onboardingDone by appViewModel.onboardingDone.collectAsStateWithLifecycle()
                var openBudgetFirst by rememberSaveable { mutableStateOf(false) }

                when (onboardingDone) {
                    null -> Box(Modifier.fillMaxSize().background(Background))
                    false -> OnboardingScreen(onChoiceMade = { openBudgetFirst = it == StartChoice.CONFIGURE })
                    true -> BudgetAppRoot(openBudgetFirst = openBudgetFirst)
                }
            }
        }
    }
}