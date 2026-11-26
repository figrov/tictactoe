package com.example.game.ui

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.game.domain.Player

/**
 * Иконка X с анимацией появления
 */
@Composable
fun XIcon(
    size: androidx.compose.ui.unit.Dp = 80.dp,
    player: Player = Player.X,
    alpha: Float = 1f,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "XIconScale"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .scale(scale),
        onDraw = {
            val strokeWidth = 6.dp.toPx()
            val color = when (player) {
                Player.X -> Color(0xFF6750A4) // Primary
                Player.O -> Color(0xFF625B71) // Secondary
            }

            // Рисуем первый диагональный отрезок X
            drawLine(
                color = color,
                start = Offset(0f, 0f),
                end = Offset(size.toPx(), size.toPx()),
                strokeWidth = strokeWidth,
                cap = Stroke.DefaultCap
            )

            // Рисуем второй диагональный отрезок X
            drawLine(
                color = color,
                start = Offset(size.toPx(), 0f),
                end = Offset(0f, size.toPx()),
                strokeWidth = strokeWidth,
                cap = Stroke.DefaultCap
            )
        }
    )
}

/**
 * Иконка O с анимацией появления
 */
@Composable
fun OIcon(
    size: androidx.compose.ui.unit.Dp = 80.dp,
    player: Player = Player.O,
    alpha: Float = 1f,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "OIconScale"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .scale(scale),
        onDraw = {
            val strokeWidth = 6.dp.toPx()
            val color = when (player) {
                Player.X -> Color(0xFF6750A4) // Primary
                Player.O -> Color(0xFF625B71) // Secondary
            }
            val radius = (size.toPx() / 2) - strokeWidth

            drawCircle(
                color = color,
                radius = radius,
                style = Stroke(width = strokeWidth),
                center = Offset(size.toPx() / 2, size.toPx() / 2)
            )
        }
    )
}
