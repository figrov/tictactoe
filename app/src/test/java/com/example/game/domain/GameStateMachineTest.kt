package com.example.game.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Тесты для GameStateMachine (логика победы и ничьей)
 */
class GameStateMachineTest {

    @Test
    fun `checkWin - победа по горизонтали (0,1,2)`() {
        val cells = listOf(
            CellState.X, CellState.X, CellState.X,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
        )
        val stateMachine = GameStateMachine(cells)

        val (isWin, winningCells) = stateMachine.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(0, 1, 2), winningCells)
    }

    @Test
    fun `checkWin - победа по горизонтали (3,4,5)`() {
        val cells = listOf(
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
            CellState.O, CellState.O, CellState.O,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
        )
        val stateMachine = GameStateMachine(cells)

        val (isWin, winningCells) = stateMachine.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(3, 4, 5), winningCells)
    }

    @Test
    fun `checkWin - победа по горизонтали (6,7,8)`() {
        val cells = listOf(
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
            CellState.X, CellState.X, CellState.X
        )
        val stateMachine = GameStateMachine(cells)

        val (isWin, winningCells) = stateMachine.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(6, 7, 8), winningCells)
    }

    @Test
    fun `checkWin - победа по вертикали (0,3,6)`() {
        val cells = listOf(
            CellState.X, CellState.EMPTY, CellState.EMPTY,
            CellState.X, CellState.EMPTY, CellState.EMPTY,
            CellState.X, CellState.EMPTY, CellState.EMPTY
        )
        val stateMachine = GameStateMachine(cells)

        val (isWin, winningCells) = stateMachine.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(0, 3, 6), winningCells)
    }

    @Test
    fun `checkWin - победа по вертикали (1,4,7)`() {
        val cells = listOf(
            CellState.EMPTY, CellState.O, CellState.EMPTY,
            CellState.EMPTY, CellState.O, CellState.EMPTY,
            CellState.EMPTY, CellState.O, CellState.EMPTY
        )
        val stateMachine = GameStateMachine(cells)

        val (isWin, winningCells) = stateMachine.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(1, 4, 7), winningCells)
    }

    @Test
    fun `checkWin - победа по вертикали (2,5,8)`() {
        val cells = listOf(
            CellState.EMPTY, CellState.EMPTY, CellState.O,
            CellState.EMPTY, CellState.EMPTY, CellState.O,
            CellState.EMPTY, CellState.EMPTY, CellState.O
        )
        val stateMachine = GameStateMachine(cells)

        val (isWin, winningCells) = stateMachine.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(2, 5, 8), winningCells)
    }

    @Test
    fun `checkWin - победа по диагонали (0,4,8)`() {
        val cells = listOf(
            CellState.X, CellState.EMPTY, CellState.EMPTY,
            CellState.EMPTY, CellState.X, CellState.EMPTY,
            CellState.EMPTY, CellState.EMPTY, CellState.X
        )
        val stateMachine = GameStateMachine(cells)

        val (isWin, winningCells) = stateMachine.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(0, 4, 8), winningCells)
    }

    @Test
    fun `checkWin - победа по диагонали (2,4,6)`() {
        val cells = listOf(
            CellState.EMPTY, CellState.EMPTY, CellState.O,
            CellState.EMPTY, CellState.X, CellState.EMPTY,
            CellState.O, CellState.EMPTY, CellState.EMPTY
        )
        val stateMachine = GameStateMachine(cells)

        val (isWin, winningCells) = stateMachine.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(2, 4, 6), winningCells)
    }

    @Test
    fun `checkWin - нет победителя при всех X`() {
        val cells = listOf(
            CellState.X, CellState.X, CellState.X,
            CellState.X, CellState.X, CellState.X,
            CellState.X, CellState.X, CellState.X
        )
        val stateMachine = GameStateMachine(cells)

        val (isWin, winningCells) = stateMachine.checkWin()

        assertFalse(isWin)
        assertEquals(null, winningCells)
    }

    @Test
    fun `checkWin - победитель приоритетнее ничьей`() {
        val cells = listOf(
            CellState.X, CellState.X, CellState.X,
            CellState.O, CellState.O, CellState.O,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
        )
        val stateMachine = GameStateMachine(cells)

        val (isWin, winningCells) = stateMachine.checkWin()

        assertTrue(isWin) // Победа должна быть приоритетной
        assertEquals(listOf(0, 1, 2), winningCells)
    }

    @Test
    fun `checkDraw - ничья при всех заполненных клетках без победителя`() {
        val cells = listOf(
            CellState.X, CellState.O, CellState.X,
            CellState.O, CellState.X, CellState.O,
            CellState.X, CellState.O, CellState.X
        )
        val stateMachine = GameStateMachine(cells)

        val isDraw = stateMachine.checkDraw()

        assertTrue(isDraw)
    }

    @Test
    fun `checkDraw - не ничья при заполненных клетках с победителем`() {
        val cells = listOf(
            CellState.X, CellState.X, CellState.X,
            CellState.O, CellState.O, CellState.O,
            CellState.X, CellState.O, CellState.EMPTY
        )
        val stateMachine = GameStateMachine(cells)

        val isDraw = stateMachine.checkDraw()

        assertFalse(isDraw)
    }

    @Test
    fun `checkDraw - не ничья при неполной доске`() {
        val cells = listOf(
            CellState.X, CellState.EMPTY, CellState.X,
            CellState.EMPTY, CellState.EMPTY, CellState.O,
            CellState.O, CellState.EMPTY, CellState.EMPTY
        )
        val stateMachine = GameStateMachine(cells)

        val isDraw = stateMachine.checkDraw()

        assertFalse(isDraw)
    }

    @Test
    fun `isValidMove - допустим пустой индекс`() {
        val cells = listOf(
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
        )
        val stateMachine = GameStateMachine(cells)

        assertTrue(stateMachine.isValidMove(0))
        assertTrue(stateMachine.isValidMove(4))
        assertTrue(stateMachine.isValidMove(8))
    }

    @Test
    fun `isValidMove - недопустим занятый индекс`() {
        val cells = listOf(
            CellState.X, CellState.EMPTY, CellState.EMPTY,
            CellState.EMPTY, CellState.O, CellState.EMPTY,
            CellState.EMPTY, CellState.EMPTY, CellState.X
        )
        val stateMachine = GameStateMachine(cells)

        assertFalse(stateMachine.isValidMove(0))
        assertFalse(stateMachine.isValidMove(4))
        assertFalse(stateMachine.isValidMove(8))
    }

    @Test
    fun `isValidMove - недопустим индекс вне диапазона`() {
        val cells = listOf(
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
        )
        val stateMachine = GameStateMachine(cells)

        assertFalse(stateMachine.isValidMove(-1))
        assertFalse(stateMachine.isValidMove(9))
        assertFalse(stateMachine.isValidMove(10))
    }
}
