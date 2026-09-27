package com.application.personal_budget_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.application.personal_budget_app.ui.navigation.AppNavHost
import com.application.personal_budget_app.ui.navigation.BudgetBottomBar
import com.application.personal_budget_app.ui.theme.PersonalbudgetappTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalbudgetappTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        BudgetBottomBar(
                            navController = navController,
                            onAddClick = { /* saisie rapide : prochaine branche */ },
                        )
                    },
                ) { innerPadding ->
                    AppNavHost(navController, Modifier.padding(innerPadding))
                }
            }
        }
    }
}
