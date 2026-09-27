package com.application.personal_budget_app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Neutres
val Ink = Color(0xFF1C1C1C)             // texte principal, boutons
val TextSecondary = Color(0xFF5B6068)
val TextStrong = Color(0xFF3E444B)
val Background = Color(0xFFFFFFFF)
val SurfaceSoft = Color(0xFFF1F4F8)      // pavé numérique, segments
val Divider = Color(0xFFEEF1F5)
val BorderControl = Color(0xFFC9D0D9)
val Track = Color(0xFFEDF0F4)            // fond des jauges

// États (jamais rouge/vert)
val CalmBlue = Color(0xFF4F7FB0)         // normal / sous-consommation
val CalmBlueDark = Color(0xFF1F3550)
val NearLimit = Color(0xFF5B55B8)        // proche de la limite
val NearLimitText = Color(0xFF3A3270)
val OverAmber = Color(0xFFD39A3A)        // dépassement
val OverAmberText = Color(0xFF6B4510)
val OverAmberBg = Color(0xFFFCF0D8)

// Texte posé sur les dégradés
val OnGradient = Color(0xFF22364A)

// Historique
val Hatch = Color(0xFFE9EDF2)          // hachures « Non renseignée »
val DashedBorder = Color(0xFF8A929C)   // bordure pointillée (contraste ≥ 3:1)
val RefundBg = Color(0xFFE1ECF8)       // fond de l'icône remboursement
object BudgetGradients {
    val header = Brush.verticalGradient(
        0f to Color(0xFFD4EAFF),
        0.72f to Color(0xFFE4E1FC),
        1f to Background,
    )
    val observationHeader = Brush.verticalGradient(
        0f to Color(0xFFE4E1FC),
        0.72f to Color(0xFFD4EAFF),
        1f to Background,
    )
    val primaryButton = Brush.linearGradient(
        listOf(Color(0xFF2F3540), Ink)
    )
}