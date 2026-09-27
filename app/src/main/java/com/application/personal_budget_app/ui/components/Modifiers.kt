package com.application.personal_budget_app.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fond hachuré en diagonale. À utiliser après un clip() pour rester dans les coins arrondis. */
fun Modifier.hatched(color: Color, spacing: Dp = 8.dp, strokeWidth: Dp = 2.dp): Modifier = drawBehind {
    val step = spacing.toPx()
    val stroke = strokeWidth.toPx()
    var x = -size.height
    while (x < size.width) {
        drawLine(color, start = Offset(x, size.height), end = Offset(x + size.height, 0f), strokeWidth = stroke)
        x += step
    }
}

/** Bordure en pointillés, coins arrondis. */
fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 1.dp): Modifier = drawBehind {
    val stroke = strokeWidth.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2, stroke / 2),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
    )
}