package com.example.game.presentation

/**
 * События от UI (Intents)
 */
sealed class UiIntent {
    // Действия пользователя
    object StartGame : UiIntent()
    data class CellClicked(val index: Int) : UiIntent()
    object NewRound : UiIntent()
    object NewGame : UiIntent()

    // Кнопки в Game Over
    object ContinueGame : UiIntent()
}
