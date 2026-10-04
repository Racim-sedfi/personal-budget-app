package com.application.personal_budget_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun BudgetPalette.scheme(dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = ink, onPrimary = background, background = background, onBackground = ink,
        surface = background, onSurface = ink, surfaceVariant = surfaceSoft, onSurfaceVariant = textSecondary,
        outline = borderControl, outlineVariant = divider,
    )
} else {
    lightColorScheme(
        primary = ink, onPrimary = background, background = background, onBackground = ink,
        surface = background, onSurface = ink, surfaceVariant = surfaceSoft, onSurfaceVariant = textSecondary,
        outline = borderControl, outlineVariant = divider,
    )
}

private val LightColors = LightPalette.scheme(dark = false)
private val DarkColors = DarkPalette.scheme(dark = true)

@Composable
fun PersonalbudgetappTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    // Avant de dessiner les enfants : nos couleurs (Ink, Background…) suivent le thème.
    if (ThemeState.isDark != darkTheme) ThemeState.isDark = darkTheme
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = Typography) {
        // Couleur par défaut des textes et icônes, même hors d'un Scaffold (onboarding, verrouillage).
        CompositionLocalProvider(LocalContentColor provides colors.onBackground, content = content)
    }
}
