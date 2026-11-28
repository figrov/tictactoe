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
    val winner: Player? = null,
    val isBoardLocked: Boolean = false
) : java.io.Serializable {
    companion object {
        val INITIAL = GameBoard()
    }

    /**
     * Проверяет, может ли игрок сделать ход в указанную клетку
     * @param index Индекс клетки (0-8)
     * @return true если клетка пуста, false если занята или индекс вне диапазона
     */
    fun isValidMove(index: Int): Boolean {
        return index in cells.indices && cells[index] == CellState.EMPTY
    }

    /**
     * Проверяет, существует ли победитель (любой непустой символ X или O)
     * @return Pair(есть ли победитель, список выигравших клеток)
     */
    fun checkWin(): Pair<Boolean, List<Int>?> {
        val winningCombinations = getWinningCombinations()

        for (combination in winningCombinations) {
            val firstCell = cells[combination[0]]
            // Проверяем только если клетка не пустая
            if (firstCell != CellState.EMPTY) {
                val isWinning = combination.all { index ->
                    cells[index] == firstCell
                }
                if (isWinning) {
                    return Pair(true, combination)
                }
            }
        }

        // Если нет победителя, возвращаем false, не null
        return Pair(false, null)
    }

    /**
     * Проверяет, является ли партия ничьей
     * @return true если все клетки заполнены и победителя нет
     */
    fun checkDraw(): Boolean {
        val isFull = cells.all { it != CellState.EMPTY }
        val hasWinner = checkWin().first
        return isFull && !hasWinner
    }

    /**
     * Получает выигрышные комбинации для 3x3 поля
     * Горизонтали: (0,1,2), (3,4,5), (6,7,8)
     * Вертикали: (0,3,6), (1,4,7), (2,5,8)
     * Диагонали: (0,4,8), (2,4,6)
     */
    private fun getWinningCombinations(): List<List<Int>> {
        return listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Горизонтали
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Вертикали
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Диагонали
        )
    }
}
