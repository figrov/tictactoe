package com.example.game.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Тесты для GameBoard (логика победы и ничьей)
 */
class GameStateMachineTest {

    @Test
    fun `checkWin - победа по горизонтали (0,1,2)`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.X, CellState.X, CellState.X,
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
            )
        )

        val (isWin, winningCells) = gameBoard.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(0, 1, 2), winningCells)
    }

    @Test
    fun `checkWin - победа по горизонтали (3,4,5)`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
                CellState.O, CellState.O, CellState.O,
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
            )
        )

        val (isWin, winningCells) = gameBoard.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(3, 4, 5), winningCells)
    }

    @Test
    fun `checkWin - победа по горизонтали (6,7,8)`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
                CellState.X, CellState.X, CellState.X
            )
        )

        val (isWin, winningCells) = gameBoard.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(6, 7, 8), winningCells)
    }

    @Test
    fun `checkWin - победа по вертикали (0,3,6)`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.X, CellState.EMPTY, CellState.EMPTY,
                CellState.X, CellState.EMPTY, CellState.EMPTY,
                CellState.X, CellState.EMPTY, CellState.EMPTY
            )
        )

        val (isWin, winningCells) = gameBoard.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(0, 3, 6), winningCells)
    }

    @Test
    fun `checkWin - победа по вертикали (1,4,7)`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.EMPTY, CellState.O, CellState.EMPTY,
                CellState.EMPTY, CellState.O, CellState.EMPTY,
                CellState.EMPTY, CellState.O, CellState.EMPTY
            )
        )

        val (isWin, winningCells) = gameBoard.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(1, 4, 7), winningCells)
    }

    @Test
    fun `checkWin - победа по вертикали (2,5,8)`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.EMPTY, CellState.EMPTY, CellState.O,
                CellState.EMPTY, CellState.EMPTY, CellState.O,
                CellState.EMPTY, CellState.EMPTY, CellState.O
            )
        )

        val (isWin, winningCells) = gameBoard.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(2, 5, 8), winningCells)
    }

    @Test
    fun `checkWin - победа по диагонали (0,4,8)`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.X, CellState.EMPTY, CellState.EMPTY,
                CellState.EMPTY, CellState.X, CellState.EMPTY,
                CellState.EMPTY, CellState.EMPTY, CellState.X
            )
        )

        val (isWin, winningCells) = gameBoard.checkWin()

        assertTrue(isWin)
        assertEquals(listOf(0, 4, 8), winningCells)
    }

    @Test
    fun `checkDraw - не ничья при неполной доске`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.X, CellState.O, CellState.X,
                CellState.O, CellState.EMPTY, CellState.O,
                CellState.X, CellState.O, CellState.X
            )
        )

        val isDraw = gameBoard.checkDraw()

        assertFalse(isDraw)
    }

    @Test
    fun `checkDraw - не ничья при заполненных клетках с победителем`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.X, CellState.X, CellState.X,
                CellState.O, CellState.O, CellState.O,
                CellState.X, CellState.O, CellState.EMPTY
            )
        )

        val isDraw = gameBoard.checkDraw()

        assertFalse(isDraw)
    }

    @Test
    fun `isValidMove - допустим пустой индекс`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
            )
        )

        assertTrue(gameBoard.isValidMove(0))
        assertTrue(gameBoard.isValidMove(4))
        assertTrue(gameBoard.isValidMove(8))
    }

    @Test
    fun `isValidMove - недопустим занятый индекс`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.X, CellState.EMPTY, CellState.EMPTY,
                CellState.EMPTY, CellState.O, CellState.EMPTY,
                CellState.EMPTY, CellState.EMPTY, CellState.X
            )
        )

        assertFalse(gameBoard.isValidMove(0))
        assertFalse(gameBoard.isValidMove(4))
        assertFalse(gameBoard.isValidMove(8))
    }

    @Test
    fun `isValidMove - недопустим индекс вне диапазона`() {
        val gameBoard = GameBoard(
            cells = listOf(
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
            )
        )

        assertFalse(gameBoard.isValidMove(-1))
        assertFalse(gameBoard.isValidMove(9))
        assertFalse(gameBoard.isValidMove(10))
    }
}
