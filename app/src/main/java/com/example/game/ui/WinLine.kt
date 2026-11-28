package com.example.game.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.game.domain.GameBoard

/**
 * Отрисовка линии победы
 */
@Composable
fun WinLine(
    board: GameBoard,
    modifier: Modifier = Modifier
) {
    val winningCells = board.isWinningCells ?: return
    var alpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(winningCells) {
        if (winningCells.isNotEmpty()) {
            // Запускаем анимацию появления линии
            kotlinx.coroutines.delay(100)
            alpha = 1f
        }
    }

    val strokeWidth by remember { mutableFloatStateOf(12f) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .zIndex(0f), // Контроль z-index: линия под клетками, чтобы не перекрывать клики
        onDraw = {
            if (alpha <= 0f) return@Canvas

            val color = Color(0xFFB3261E) // Error
            val (x1, y1) = getCellCenter(winningCells[0])
            val (x2, y2) = getCellCenter(winningCells.last())

            // Рисуем линию с анимацией толщины
            val currentStrokeWidth = strokeWidth * alpha

            drawLine(
                color = color,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = currentStrokeWidth,
                cap = Stroke.DefaultCap
            )
        }
    )
}

/**
 * Получаем координаты центра клетки
 */
private fun getCellCenter(index: Int): Pair<Float, Float> {
    val row = index / 3
    val col = index % 3
    return Pair(
        col * 300f + 150f, // 3 клетки по 100dp = 300dp = 300f px
        row * 300f + 150f
    )
}
