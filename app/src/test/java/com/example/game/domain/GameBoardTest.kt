package com.example.game.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Тесты для GameBoard и доменных классов
 */
class GameBoardTest {

    @Test
    fun `INITIAL - начальное состояние корректное`() {
        val initial = GameBoard.INITIAL

        assertEquals(9, initial.cells.size)
        assertTrue(initial.cells.all { it == CellState.EMPTY })
        assertEquals(Player.X, initial.currentPlayer)
        assertEquals(0, initial.scoreX)
        assertEquals(0, initial.scoreO)
        assertEquals(0, initial.drawCount)
        assertEquals(null, initial.isWinningCells)
        assertEquals(null, initial.winner)
    }

    @Test
    fun `GameBoard - корректность поля и игрока`() {
        val board = GameBoard(
            cells = listOf(
                CellState.X, CellState.EMPTY, CellState.O,
                CellState.EMPTY, CellState.X, CellState.EMPTY,
                CellState.EMPTY, CellState.O, CellState.EMPTY
            ),
            currentPlayer = Player.O
        )

        assertEquals(9, board.cells.size)
        assertEquals(CellState.X, board.cells[0])
        assertEquals(CellState.EMPTY, board.cells[1])
        assertEquals(CellState.O, board.cells[2])
        assertEquals(Player.O, board.currentPlayer)
    }

    @Test
    fun `GameBoard - корректность счета`() {
        val board = GameBoard(
            cells = List(9) { CellState.EMPTY },
            scoreX = 2,
            scoreO = 1,
            drawCount = 1
        )

        assertEquals(2, board.scoreX)
        assertEquals(1, board.scoreO)
        assertEquals(1, board.drawCount)
    }

    @Test
    fun `GameBoard - корректность состояния победы`() {
        val winningCells = listOf(0, 1, 2)
        val winner = Player.X

        val board = GameBoard(
            cells = listOf(
                CellState.X, CellState.X, CellState.X,
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
                CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
            ),
            isWinningCells = winningCells,
            winner = winner
        )

        assertEquals(winningCells, board.isWinningCells)
        assertEquals(winner, board.winner)
    }

    @Test
    fun `CellState_EMPTY - isEmpty возвращает true`() {
        assertTrue(CellState.EMPTY.isEmpty)
    }

    @Test
    fun `CellState_X - isEmpty возвращает false`() {
        assertFalse(CellState.X.isEmpty)
    }

    @Test
    fun `CellState_O - isEmpty возвращает false`() {
        assertFalse(CellState.O.isEmpty)
    }

    @Test
    fun `CellState_X_isMarked_возвращает_true`() {
        assertTrue(CellState.X.isMarked)
    }

    @Test
    fun `CellState_O_isMarked_возвращает_true`() {
        assertTrue(CellState.O.isMarked)
    }

    @Test
    fun `CellState_EMPTY_isMarked_возвращает_false`() {
        assertFalse(CellState.EMPTY.isMarked)
    }

    @Test
    fun `Player_X_opponent_возвращает_O`() {
        assertEquals(Player.O, Player.X.opponent)
    }

    @Test
    fun `Player_O_opponent_возвращает_X`() {
        assertEquals(Player.X, Player.O.opponent)
    }

    @Test
    fun `Player_X_displayName_возвращает_X`() {
        assertEquals("X", Player.X.displayName)
    }

    @Test
    fun `Player_O_displayName_возвращает_O`() {
        assertEquals("O", Player.O.displayName)
    }

    @Test
    fun `Player_X_name_возвращает_X`() {
        assertEquals("X", Player.X.name)
    }

    @Test
    fun `Player_O_name_возвращает_O`() {
        assertEquals("O", Player.O.name)
    }

    @Test
    fun `создать доску с 9 пустыми клетками - все EMPTY`() {
        val cells = List(9) { CellState.EMPTY }
        assertEquals(9, cells.size)
        assertTrue(cells.all { it == CellState.EMPTY })
    }

    @Test
    fun `создать доску с заполненными клетками - все заняты`() {
        val cells = listOf(
            CellState.X, CellState.O, CellState.X,
            CellState.O, CellState.X, CellState.O,
            CellState.X, CellState.O, CellState.X
        )
        assertEquals(9, cells.size)
        assertTrue(cells.all { it != CellState.EMPTY })
    }

    @Test
    fun `создать доску с чередованием - корректность значений`() {
        val cells = listOf(
            CellState.X, CellState.O, CellState.X,
            CellState.O, CellState.X, CellState.O,
            CellState.X, CellState.O, CellState.X
        )
        assertEquals(CellState.X, cells[0])
        assertEquals(CellState.O, cells[1])
        assertEquals(CellState.X, cells[2])
        assertEquals(CellState.O, cells[3])
        assertEquals(CellState.X, cells[4])
        assertEquals(CellState.O, cells[5])
        assertEquals(CellState.X, cells[6])
        assertEquals(CellState.O, cells[7])
        assertEquals(CellState.X, cells[8])
    }
}
