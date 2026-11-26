package com.example.game.domain

/**
 * State Machine для проверки победы и ничьей
 */
class GameStateMachine(
    private val cells: List<CellState>,
    private val winningCells: List<Int>? = null
) {

    /**
     * Проверяет, существует ли победитель
     * @return Pair(есть ли победитель, список выигравших клеток)
     */
    fun checkWin(): Pair<Boolean, List<Int>?> {
        val winningCombinations = getWinningCombinations()

        for (combination in winningCombinations) {
            val firstCell = cells[combination[0]]
            if (firstCell != CellState.EMPTY) {
                val isWinning = combination.all { index ->
                    cells[index] == firstCell
                }
                if (isWinning) {
                    return Pair(true, combination)
                }
            }
        }

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
     * Проверяет, можно ли сделать ход в указанную клетку
     * @param index Индекс клетки (0-8)
     * @return true если клетка пуста
     */
    fun isValidMove(index: Int): Boolean {
        return index in cells.indices && cells[index] == CellState.EMPTY
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
