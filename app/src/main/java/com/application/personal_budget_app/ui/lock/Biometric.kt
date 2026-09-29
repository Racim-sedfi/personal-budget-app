package com.application.personal_budget_app.ui.lock

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Empreinte/visage OU code du téléphone : la seule combinaison qui marche de l'API 26 à aujourd'hui. */
private const val AUTHENTICATORS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

/** Le téléphone a-t-il un verrouillage configuré (empreinte, visage, code, schéma) ? */
fun canUseAppLock(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

/** Retrouve l'Activity derrière un Context de Compose. */
fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

fun FragmentActivity.authenticate(title: String, onSuccess: () -> Unit) {
    val prompt = BiometricPrompt(
        this,
        ContextCompat.getMainExecutor(this),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
            // Annulé ou échec : on ne fait rien, l'utilisateur peut réessayer.
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle("Empreinte, visage ou code du téléphone")
        .setAllowedAuthenticators(AUTHENTICATORS)   // pas de bouton « Annuler » avec DEVICE_CREDENTIAL : Android le gère
        .build()
    prompt.authenticate(info)
}