package com.application.personal_budget_app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.application.personal_budget_app.ui.theme.Track

/**
 * Barre de progression : la largeur et la couleur s'animent quand elles changent.
 * Le remplissage est toujours borné entre 0 et 1.
 */
@Composable
fun BudgetProgressBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = Track,
    height: Dp = 4.dp,
) {
    val animatedFraction by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(durationMillis = 400), label = "progress")
    val animatedColor by animateColorAsState(color, tween(durationMillis = 400), label = "progressColor")
    val shape = RoundedCornerShape(height / 2)

    Box(modifier.fillMaxWidth().height(height).clip(shape).background(trackColor)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(animatedFraction).clip(shape).background(animatedColor))
    }
}