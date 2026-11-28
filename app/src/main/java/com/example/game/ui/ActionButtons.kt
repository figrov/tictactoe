package com.example.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.game.domain.GameState

/**
 * Управление кнопками действий (Start, Continue, New Game)
 */
@Composable
fun ActionButtons(
    gameState: GameState,
    onNewRound: () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGameOver = gameState is GameState.GameOver
    val isPlaying = gameState is GameState.Playing
    val isIdle = gameState is GameState.Idle

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            // Продолжить (когда GAME_OVER)
            if (isGameOver) {
                ActionButton(
                    text = "Продолжить",
                    onClick = onNewRound,
                    enabled = true
                )
            }

            // Новая игра (в PLAYING и GAME_OVER)
            if (isPlaying || isGameOver) {
                ActionButton(
                    text = "Новая игра",
                    onClick = onNewGame,
                    enabled = true
                )
            }

            // Начать (только в IDLE)
            if (isIdle) {
                ActionButton(
                    text = "Начать игру",
                    onClick = onNewGame, // В IDLE используем ту же логику
                    enabled = true
                )
            }
        }
    }
}
