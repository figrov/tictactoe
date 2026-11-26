package com.example.game.domain

/**
 * Игровое поле (внутреннее состояние)
 * @property cells Состояния всех 9 клеток
 * @property currentPlayer Текущий игрок (X или O)
 * @property scoreX Счет игрока X
 * @property scoreO Счет игрока O
 * @property drawCount Счет ничьих
 * @property isWinningCells Индексы выигравших клеток (или null)
 * @property winner Победитель (или null)
 */
data class GameBoard(
    val cells: List<CellState> = List(9) { CellState.EMPTY },
    val currentPlayer: Player = Player.X,
    val scoreX: Int = 0,
    val scoreO: Int = 0,
    val drawCount: Int = 0,
    val isWinningCells: List<Int>? = null,
    val winner: Player? = null
) {
    companion object {
        val INITIAL = GameBoard()
    }

    /**
     * Проверка, может ли игрок сделать ход в указанную клетку
     * @param index Индекс клетки (0-8)
     * @return true если клетка пуста, false если занята
     */
    fun isValidMove(index: Int): Boolean {
        val row = index / 3
        val col = index % 3
        return cells[index] == CellState.EMPTY
    }
}
