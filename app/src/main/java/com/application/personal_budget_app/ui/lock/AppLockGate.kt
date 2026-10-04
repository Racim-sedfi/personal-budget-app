package com.application.personal_budget_app.ui.lock

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.application.personal_budget_app.R
import com.application.personal_budget_app.ui.theme.*

/**
 * Pose l'écran « verrouillé » par-dessus l'app, sans la détruire :
 * on retrouve l'onglet où on était après avoir déverrouillé.
 */
@Composable
fun AppLockGate(
    enabled: Boolean,
    delayMinutes: Int,
    startUnlocked: Boolean,
    viewModel: AppLockViewModel = hiltViewModel(),
    content: @Composable (locked: Boolean) -> Unit,
) {
    remember(viewModel) { viewModel.start(lockNow = enabled && !startUnlocked); true }
    val locked by viewModel.locked.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onStop(SystemClock.elapsedRealtime()) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        viewModel.onStart(enabled, delayMinutes, SystemClock.elapsedRealtime())
    }

    val unlock: () -> Unit = {
        if (!canUseAppLock(context)) {
            viewModel.unlock()   // le verrouillage du téléphone a été retiré : on ne bloque pas l'utilisateur
        } else {
            context.findFragmentActivity()?.authenticate("Déverrouiller Budget") { viewModel.unlock() }
        }
    }

    // On demande l'authentification dès que l'écran se verrouille.
    LaunchedEffect(locked) { if (locked) unlock() }

    Box(Modifier.fillMaxSize()) {
        // Verrouillée : l'app reste en mémoire mais TalkBack ne peut rien lire dessous.
        Box(if (locked) Modifier.fillMaxSize().clearAndSetSemantics {} else Modifier.fillMaxSize()) { content(locked) }
        if (locked) LockScreen(onUnlock = unlock)
    }
}

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    Column(
        Modifier.fillMaxSize()
            .background(BudgetGradients.header)
            .background(Background.copy(alpha = 0.6f))
            // Bloque les appuis vers l'app en dessous.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
            .statusBarsPadding().navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(120.dp).clip(CircleShape).background(
                BudgetGradients.hero,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_lock), contentDescription = null, tint = Ink, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Budget verrouillé", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        Text("Tes données restent sur ce téléphone.", style = MaterialTheme.typography.bodyMedium, color = TextStrong)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onUnlock,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Background),
        ) { Text("Déverrouiller", style = MaterialTheme.typography.labelLarge) }
    }
}