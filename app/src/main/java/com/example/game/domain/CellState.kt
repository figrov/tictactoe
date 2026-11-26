package com.example.game.domain

/**
 * Состояние клетки на игровом поле
 * @property isEmpty Является ли клетка пустой
 */
enum class CellState {
    EMPTY,
    X,
    O;

    val isEmpty: Boolean
        get() = this === EMPTY

    val isMarked: Boolean
        get() = this !== EMPTY
}
