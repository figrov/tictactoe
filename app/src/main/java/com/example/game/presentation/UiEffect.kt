package com.example.game.presentation

import com.example.game.domain.GameState
import com.example.game.domain.GameBoard
import com.example.game.domain.CellState

/**
 * Побочные эффекты (звуки, вибрация)
 * @property type Тип эффекта
 * @property value Дополнительные данные (опционально)
 */
sealed class UiEffect {
    // Звуковые эффекты
    object ClickEmptyCell : UiEffect()
    object IgnoreOccupiedCell : UiEffect()
    object WinnerFound : UiEffect()
    object DrawFound : UiEffect()
    object NewRound : UiEffect()
    object NewGame : UiEffect()

    // Вибрационные эффекты
    enum class Vibration {
        NONE,
        SHORT,
        MEDIUM,
        LONG
    }

    data class Vibrate(val pattern: Vibration) : UiEffect()
}
