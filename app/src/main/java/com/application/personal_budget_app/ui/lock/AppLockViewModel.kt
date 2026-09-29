package com.application.personal_budget_app.ui.lock

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Verrouillé ou non. Vit dans un ViewModel :
 * survit à une rotation, mais pas à la mort du processus (l'app redémarre alors verrouillée).
 */
@HiltViewModel
class AppLockViewModel @Inject constructor() : ViewModel() {

    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    private var started = false
    private var stoppedAt: Long? = null

    /** Premier affichage de l'app. Sans effet ensuite (rotation…). */
    fun start(lockNow: Boolean) {
        if (started) return
        started = true
        _locked.value = lockNow
    }

    /** L'app passe en arrière-plan. Déjà verrouillée : rien à retenir (ex. écran du code PIN). */
    fun onStop(nowMillis: Long) {
        if (!_locked.value) stoppedAt = nowMillis
    }

    /** L'app revient : on verrouille si l'absence a dépassé le délai choisi. */
    fun onStart(enabled: Boolean, delayMinutes: Int, nowMillis: Long) {
        val since = stoppedAt ?: return
        stoppedAt = null
        if (enabled && nowMillis - since >= delayMinutes * 60_000L) _locked.value = true
    }

    fun unlock() {
        _locked.value = false
    }
}