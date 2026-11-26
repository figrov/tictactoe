package com.example.game.presentation

import com.example.game.domain.GameState
import com.example.game.domain.GameBoard
import com.example.game.domain.CellState
import com.example.game.domain.Player

/**
 * Состояние интерфейса (UiState)
 * @property gameState Текущее состояние игры
 * @property board Игровое поле
 * @property isBoardLocked Блокировка доски после завершения раунда
 * @property isGameOverAnimating Анимация завершения игры
 * @property message Текущее сообщение (ход, победитель, ничья)
 */
data class UiState(
    val gameState: GameState = GameState.Idle,
    val board: GameBoard = GameBoard.INITIAL,
    val isBoardLocked: Boolean = false,
    val isGameOverAnimating: Boolean = false,
    val message: String = "Добро пожаловать в крестики-нолики!"
) {
    val currentPlayer: Player?
        get() = when (gameState) {
            is GameState.Playing -> gameState.board.currentPlayer
            else -> null
        }

    val isCellAnimating: Map<Int, Boolean> = mapOf()

    val isButtonAnimating: Boolean = false

    val scoreX: Int
        get() = board.scoreX

    val scoreO: Int
        get() = board.scoreO

    val drawCount: Int
        get() = board.drawCount
}
