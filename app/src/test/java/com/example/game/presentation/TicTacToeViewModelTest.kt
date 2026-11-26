package com.example.game.presentation

import com.example.game.domain.GameBoard
import com.example.game.domain.CellState
import com.example.game.domain.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Тесты для TicTacToeViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TicTacToeViewModelTest {

    private lateinit var viewModel: TicTacToeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TicTacToeViewModel(suppressedSavedStateHandle())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun suppressedSavedStateHandle() = object : androidx.lifecycle.SavedStateHandle {}

    @Test
    fun `onIntent.StartGame - переключает на PLAYING, очищает поле, ход X`() {
        viewModel.onIntent(UiIntent.StartGame)

        val uiState = viewModel.uiState.value
        assertEquals(GameState.Playing(GameBoard.INITIAL), uiState.gameState)
        assertTrue(uiState.board.cells.all { it == CellState.EMPTY })
        assertEquals(Player.X, uiState.board.currentPlayer)
        assertEquals(0, uiState.message)
        assertFalse(uiState.isBoardLocked)
    }

    @Test
    fun `onIntent.CellClicked(0) - первый ход X на клетку 0`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0))

        val uiState = viewModel.uiState.value
        assertTrue(uiState.gameState is GameState.Playing)
        assertEquals(CellState.X, uiState.board.cells[0])
        assertEquals(Player.O, uiState.board.currentPlayer)
    }

    @Test
    fun `onIntent.CellClicked - победа X по горизонтали (0,1,2)`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(3)) // O
        viewModel.onIntent(UiIntent.CellClicked(1)) // X
        viewModel.onIntent(UiIntent.CellClicked(4)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа

        val uiState = viewModel.uiState.value
        assertTrue(uiState.gameState is GameState.GameOver)
        assertTrue(uiState.isBoardLocked)
        assertEquals(listOf(0, 1, 2), uiState.board.isWinningCells)
        assertEquals(Player.X, uiState.board.winner)
        assertTrue(uiState.message.contains("Победил X"))
    }

    @Test
    fun `onIntent.CellClicked - победа X по вертикали (0,3,6)`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(4)) // O
        viewModel.onIntent(UiIntent.CellClicked(3)) // X
        viewModel.onIntent(UiIntent.CellClicked(5)) // O
        viewModel.onIntent(UiIntent.CellClicked(6)) // X - победа

        val uiState = viewModel.uiState.value
        assertTrue(uiState.gameState is GameState.GameOver)
        assertTrue(uiState.isBoardLocked)
        assertEquals(listOf(0, 3, 6), uiState.board.isWinningCells)
        assertEquals(Player.X, uiState.board.winner)
    }

    @Test
    fun `onIntent.CellClicked - победа X по диагонали (0,4,8)`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(4)) // X
        viewModel.onIntent(UiIntent.CellClicked(2)) // O
        viewModel.onIntent(UiIntent.CellClicked(8)) // X - победа

        val uiState = viewModel.uiState.value
        assertTrue(uiState.gameState is GameState.GameOver)
        assertTrue(uiState.isBoardLocked)
        assertEquals(listOf(0, 4, 8), uiState.board.isWinningCells)
        assertEquals(Player.X, uiState.board.winner)
    }

    @Test
    fun `onIntent.CellClicked - победа O`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(2)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(1)) // X (клетка занята, ход игнорируется)
        viewModel.onIntent(UiIntent.CellClicked(5)) // O
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(4)) // O - победа

        val uiState = viewModel.uiState.value
        assertTrue(uiState.gameState is GameState.GameOver)
        assertTrue(uiState.isBoardLocked)
        assertTrue(uiState.message.contains("Победил O"))
    }

    @Test
    fun `onIntent.CellClicked - ничья`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X
        viewModel.onIntent(UiIntent.CellClicked(3)) // O
        viewModel.onIntent(UiIntent.CellClicked(4)) // X
        viewModel.onIntent(UiIntent.CellClicked(5)) // O
        viewModel.onIntent(UiIntent.CellClicked(6)) // X
        viewModel.onIntent(UiIntent.CellClicked(7)) // O
        viewModel.onIntent(UiIntent.CellClicked(8)) // X - ничья

        val uiState = viewModel.uiState.value
        assertTrue(uiState.gameState is GameState.GameOver)
        assertTrue(uiState.isBoardLocked)
        assertTrue(uiState.message.contains("Ничья"))
    }

    @Test
    fun `onIntent.CellClicked - клонирует доску после хода`() {
        viewModel.onIntent(UiIntent.StartGame)
        val initialState = viewModel.uiState.value.board.cells.toList()

        viewModel.onIntent(UiIntent.CellClicked(0))
        val updatedState = viewModel.uiState.value.board.cells.toList()

        assertEquals(initialState, initialState) // Не изменяем старое состояние
        assertEquals(CellState.X, updatedState[0])
        assertEquals(CellState.EMPTY, updatedState[1])
    }

    @Test
    fun `onIntent.NewGame - сбрасывает счет и поле в IDLE`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
        viewModel.onIntent(UiIntent.NewRound) // продолжение
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.NewGame)

        val uiState = viewModel.uiState.value
        assertEquals(GameState.Idle, uiState.gameState)
        assertTrue(uiState.board.cells.all { it == CellState.EMPTY })
        assertEquals(0, uiState.scoreX)
        assertEquals(0, uiState.scoreO)
        assertEquals(0, uiState.drawCount)
    }

    @Test
    fun `onIntent.NewRound - сохраняет счет`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
        viewModel.onIntent(UiIntent.NewRound)

        val uiState = viewModel.uiState.value
        assertEquals(GameState.Playing(GameBoard.INITIAL), uiState.gameState)
        assertEquals(1, uiState.scoreX) // X победил
        assertEquals(0, uiState.scoreO)
    }

    @Test
    fun `onIntent.NewRound - счет X увеличивается`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
        viewModel.onIntent(UiIntent.NewRound)
        viewModel.onIntent(UiIntent.NewRound) // еще одна победа X

        val uiState = viewModel.uiState.value
        assertEquals(2, uiState.scoreX)
        assertEquals(0, uiState.scoreO)
    }

    @Test
    fun `onIntent.NewRound - счет O увеличивается`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(2)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(4)) // O - победа
        viewModel.onIntent(UiIntent.NewRound)

        val uiState = viewModel.uiState.value
        assertEquals(0, uiState.scoreX)
        assertEquals(1, uiState.scoreO)
    }

    @Test
    fun `onIntent.NewRound - счет ничьих увеличивается`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X
        viewModel.onIntent(UiIntent.CellClicked(3)) // O
        viewModel.onIntent(UiIntent.CellClicked(4)) // X
        viewModel.onIntent(UiIntent.CellClicked(5)) // O
        viewModel.onIntent(UiIntent.CellClicked(6)) // X
        viewModel.onIntent(UiIntent.CellClicked(7)) // O
        viewModel.onIntent(UiIntent.CellClicked(8)) // X - ничья
        viewModel.onIntent(UiIntent.NewRound)

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.drawCount)
    }

    @Test
    fun `onIntent.CellClicked - игнорирует клик по занятой клетке`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(0)) // O (клетка занята, должен быть игнорирован)

        val uiState = viewModel.uiState.value
        assertEquals(CellState.X, uiState.board.cells[0])
        assertEquals(Player.O, uiState.board.currentPlayer)
        assertEquals(1, uiState.board.cells.filter { it == CellState.X }.size)
    }

    @Test
    fun `onIntent.NewRound - корректно обрабатывает ничью`() {
        viewModel.onIntent(UiIntent.StartGame)
        // Заполняем доску для ничьей
        listOf(0, 1, 2, 3, 4, 5, 6, 7, 8).forEach { index ->
            viewModel.onIntent(UiIntent.CellClicked(index))
        }
        viewModel.onIntent(UiIntent.NewRound)

        val uiState = viewModel.uiState.value
        assertEquals(GameState.Playing(GameBoard.INITIAL), uiState.gameState)
        assertEquals(1, uiState.drawCount)
    }

    @Test
    fun `isBoardLocked - true после победы, false после NewRound`() {
        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа

        assertTrue(viewModel.uiState.value.isBoardLocked)

        viewModel.onIntent(UiIntent.NewRound)
        assertFalse(viewModel.uiState.value.isBoardLocked)
    }

    @Test
    fun `currentPlayer - корректный игрок после хода`() {
        viewModel.onIntent(UiIntent.StartGame)
        assertEquals(Player.X, viewModel.uiState.value.currentPlayer)

        viewModel.onIntent(UiIntent.CellClicked(0))
        assertEquals(Player.O, viewModel.uiState.value.currentPlayer)

        viewModel.onIntent(UiIntent.CellClicked(1))
        assertEquals(Player.X, viewModel.uiState.value.currentPlayer)
    }
}
