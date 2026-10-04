package com.application.personal_budget_app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Toutes les couleurs de l'app, en version claire et sombre. Jamais de rouge ni de vert. */
class BudgetPalette(
    val ink: Color,            // texte principal, boutons
    val textSecondary: Color,
    val textStrong: Color,
    val background: Color,
    val surfaceSoft: Color,    // pavé numérique, segments
    val divider: Color,
    val borderControl: Color,
    val track: Color,          // fond des jauges
    val stepTrack: Color,      // étapes pas encore faites (onboarding, assistant)
    val cardOverlay: Color,    // cartes posées sur un dégradé
    val calmBlue: Color,       // normal / sous-consommation
    val calmBlueDark: Color,   // texte et icônes « bleu calme »
    val nearLimit: Color,
    val nearLimitText: Color,
    val overAmber: Color,      // dépassement
    val overAmberText: Color,
    val overAmberBg: Color,
    val onGradient: Color,     // texte posé sur les dégradés
    val hatch: Color,          // hachures « Non renseignée »
    val dashedBorder: Color,   // bordure pointillée (contraste ≥ 3:1)
    val refundBg: Color,       // fond de l'icône remboursement / revenu
)

val LightPalette = BudgetPalette(
    ink = Color(0xFF1C1C1C), textSecondary = Color(0xFF5B6068), textStrong = Color(0xFF3E444B),
    background = Color(0xFFFFFFFF), surfaceSoft = Color(0xFFF1F4F8), divider = Color(0xFFEEF1F5),
    borderControl = Color(0xFFC9D0D9), track = Color(0xFFEDF0F4), stepTrack = Color(0xFFD3DAE4),
    cardOverlay = Color.White.copy(alpha = 0.8f),
    calmBlue = Color(0xFF4F7FB0), calmBlueDark = Color(0xFF1F3550),
    nearLimit = Color(0xFF5B55B8), nearLimitText = Color(0xFF3A3270),
    overAmber = Color(0xFFD39A3A), overAmberText = Color(0xFF6B4510), overAmberBg = Color(0xFFFCF0D8),
    onGradient = Color(0xFF22364A), hatch = Color(0xFFE9EDF2), dashedBorder = Color(0xFF8A929C),
    refundBg = Color(0xFFE1ECF8),
)

val DarkPalette = BudgetPalette(
    ink = Color(0xFFECEFF3), textSecondary = Color(0xFFA3AAB4), textStrong = Color(0xFFC7CDD5),
    background = Color(0xFF121417), surfaceSoft = Color(0xFF1D2126), divider = Color(0xFF262B31),
    borderControl = Color(0xFF3A414A), track = Color(0xFF262B31), stepTrack = Color(0xFF3A414A),
    cardOverlay = Color(0xFF1D2126).copy(alpha = 0.92f),
    calmBlue = Color(0xFF7FA9D6), calmBlueDark = Color(0xFFA8C5E6),
    nearLimit = Color(0xFF9B96E0), nearLimitText = Color(0xFFC4C0F2),
    overAmber = Color(0xFFE0B25E), overAmberText = Color(0xFFF0C985), overAmberBg = Color(0xFF3A2E17),
    onGradient = Color(0xFFC9D6E6), hatch = Color(0xFF23272D), dashedBorder = Color(0xFF6C747E),
    refundBg = Color(0xFF1E2B3A),
)

/**
 * Thème actif. C'est un état Compose : quand il change, tout ce qui lit une couleur se redessine.
 * Les couleurs ci-dessous restent de simples `val` : utilisables partout, y compris dans un Canvas.
 */
object ThemeState {
    var isDark by mutableStateOf(false)
}

private val palette: BudgetPalette get() = if (ThemeState.isDark) DarkPalette else LightPalette

/** Pour une couleur ponctuelle : sa version claire et sa version sombre. */
fun themed(light: Color, dark: Color): Color = if (ThemeState.isDark) dark else light

val Ink: Color get() = palette.ink
val TextSecondary: Color get() = palette.textSecondary
val TextStrong: Color get() = palette.textStrong
val Background: Color get() = palette.background
val SurfaceSoft: Color get() = palette.surfaceSoft
val Divider: Color get() = palette.divider
val BorderControl: Color get() = palette.borderControl
val Track: Color get() = palette.track
val StepTrack: Color get() = palette.stepTrack
val CardOverlay: Color get() = palette.cardOverlay
val CalmBlue: Color get() = palette.calmBlue
val CalmBlueDark: Color get() = palette.calmBlueDark
val NearLimit: Color get() = palette.nearLimit
val NearLimitText: Color get() = palette.nearLimitText
val OverAmber: Color get() = palette.overAmber
val OverAmberText: Color get() = palette.overAmberText
val OverAmberBg: Color get() = palette.overAmberBg
val OnGradient: Color get() = palette.onGradient
val Hatch: Color get() = palette.hatch
val DashedBorder: Color get() = palette.dashedBorder
val RefundBg: Color get() = palette.refundBg

object BudgetGradients {
    val header: Brush
        get() = Brush.verticalGradient(
            0f to themed(Color(0xFFD4EAFF), Color(0xFF1B2A3B)),
            0.72f to themed(Color(0xFFE4E1FC), Color(0xFF221F36)),
            1f to Background,
        )
    val observationHeader: Brush
        get() = Brush.verticalGradient(
            0f to themed(Color(0xFFE4E1FC), Color(0xFF221F36)),
            0.72f to themed(Color(0xFFD4EAFF), Color(0xFF1B2A3B)),
            1f to Background,
        )
    val primaryButton: Brush
        get() = Brush.linearGradient(listOf(themed(Color(0xFF2F3540), Color(0xFFFFFFFF)), Ink))

    /** Les grands cercles d'illustration (onboarding, verrouillage, cycle clôturé). */
    val hero: Brush
        get() = Brush.linearGradient(
            listOf(
                themed(Color(0xFFD6C6F6), Color(0xFF3A3270)),
                themed(Color(0xFFBFC8FA), Color(0xFF2A3A5E)),
                themed(Color(0xFFA9D6FF), Color(0xFF1F3550)),
            ),
        )
}
