package com.application.personal_budget_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.application.personal_budget_app.R

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

// "tnum" = chiffres tabulaires : les montants ne « sautent » pas quand ils changent
private fun inter(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, spacing: Float = 0f) =
    TextStyle(
        fontFamily = Inter,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = spacing.sp,
        fontFeatureSettings = "tnum",
    )

val Typography = Typography(
    displayLarge = inter(52, 60, FontWeight.SemiBold, -1.8f),   // montant principal
    displayMedium = inter(60, 68, FontWeight.SemiBold, -2.4f),  // montant de saisie
    headlineSmall = inter(22, 28, FontWeight.SemiBold),         // titre d'écran
    titleMedium = inter(15, 20, FontWeight.SemiBold),           // titre de section
    bodyLarge = inter(15, 22),
    bodyMedium = inter(14, 20),                                 // texte courant
    bodySmall = inter(12, 16),                                  // secondaire
    labelLarge = inter(14, 20, FontWeight.SemiBold),            // boutons
    labelSmall = inter(11, 14, FontWeight.Medium),              // barre du bas
)