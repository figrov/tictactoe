package com.example.game.domain

/**
 * Состояние игры (State Machine)
 * @property gameState Текущее состояние игры
 * @property board Игровое поле
 */
sealed interface GameState {
    val gameState: GameStateType

    object Idle : GameState {
        override val gameState = GameStateType.IDLE
    }

    data class Playing(
        val board: GameBoard
    ) : GameState {
        override val gameState = GameStateType.PLAYING

        val winner: Player? get() = board.winner
        val isWinningCells: List<Int>? get() = board.isWinningCells
        val drawCount: Int get() = board.drawCount
    }

    object GameOver : GameState {
        override val gameState = GameStateType.GAME_OVER
    }

    object Paused : GameState {
        override val gameState = GameStateType.PAUSED
    }
}

enum class GameStateType {
    IDLE,
    PLAYING,
    GAME_OVER,
    PAUSED
}
