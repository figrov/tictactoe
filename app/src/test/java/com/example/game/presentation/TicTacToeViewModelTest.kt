package com.example.game.presentation

import com.example.game.domain.GameBoard
import com.example.game.domain.CellState
import com.example.game.domain.GameState
import com.example.game.domain.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import app.cash.turbine.test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import androidx.lifecycle.SavedStateHandle

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
        viewModel = TicTacToeViewModel(SavedStateHandle(emptyMap()))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onIntent_StartGame_корректно_инициализирует_состояние`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.Playing)
            assertTrue(uiState.board.cells.all { it == CellState.EMPTY })
            assertEquals(Player.X, uiState.board.currentPlayer)
            assertEquals(0, uiState.board.scoreX)
            assertEquals(0, uiState.board.scoreO)
            assertEquals(0, uiState.board.drawCount)
            assertFalse(uiState.isBoardLocked)
        }
    }

    @Test
    fun `onIntent_StartGame_переключает_на_PLAYING_очищает_поле_ход_X`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.Playing)
            assertTrue(uiState.board.cells.all { it == CellState.EMPTY })
            assertEquals(Player.X, uiState.board.currentPlayer)
            assertEquals(0, uiState.board.scoreX)
            assertEquals(0, uiState.board.scoreO)
            assertEquals(0, uiState.board.drawCount)
            assertFalse(uiState.isBoardLocked)
        }
    }

    @Test
    fun `onIntent_CellClicked_0_первый_ход_X_на_клетку_0`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0))
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения корутины

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.Playing)
            assertEquals(CellState.X, uiState.board.cells[0])
            assertEquals(Player.O, uiState.board.currentPlayer)
        }
    }

    @Test
    fun `onIntent_CellClicked_победа_X_по_горизонтали_0_1_2`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(5)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(300)

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.GameOver)
            assertTrue(uiState.isBoardLocked)
            assertTrue(uiState.message.contains("Победил X"))
            assertEquals(Player.X, uiState.board.winner)
            assertEquals(listOf(0, 1, 2), uiState.board.isWinningCells)
        }
    }

    @Test
    fun `onIntent_CellClicked_победа_X_по_вертикали_0_3_6`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(3)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(5)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(6)) // X - победа
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(300)

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.GameOver)
            assertTrue(uiState.isBoardLocked)
            assertTrue(uiState.message.contains("Победил X"))
            assertEquals(Player.X, uiState.board.winner)
            assertEquals(listOf(0, 3, 6), uiState.board.isWinningCells)
        }
    }

    @Test
    fun `onIntent_CellClicked_победа_X_по_диагонали_0_4_8`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(3)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(8)) // X - победа
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(300)

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.GameOver)
            assertTrue(uiState.isBoardLocked)
            assertTrue(uiState.message.contains("Победил X"))
            assertEquals(Player.X, uiState.board.winner)
            assertEquals(listOf(0, 4, 8), uiState.board.isWinningCells)
        }
    }

    @Test
    fun `onIntent_CellClicked_победа_O`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(3)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(8)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(5)) // O - победа (линия 3,4,5)
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(300)

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.GameOver)
            assertTrue(uiState.isBoardLocked)
            assertTrue(uiState.message.contains("Победил O"))
            assertEquals(Player.O, uiState.board.winner)
            assertEquals(listOf(3, 4, 5), uiState.board.isWinningCells)
            
            // Проверяем, что счет O увеличился
            assertEquals(0, uiState.board.scoreX)
            assertEquals(1, uiState.board.scoreO)
        }
    }

    @Test
    fun `onIntent_CellClicked_ничья`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            // Реальная ничья без линий 3 в ряд: listOf(0, 1, 2, 4, 3, 6, 5, 8, 7)
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(2)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(3)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(6)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(5)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(8)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(7)) // X - ничья
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(300)

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.GameOver)
            assertTrue(uiState.isBoardLocked)
            assertTrue(uiState.message.contains("Ничья"))
        }
    }

    @Test
    fun `onIntent_CellClicked_клонирует_доску_после_хода`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            val initialState = viewModel.uiState.value.board.cells.toList()

            viewModel.onIntent(UiIntent.CellClicked(0))
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(300)
            val updatedState = viewModel.uiState.value.board.cells.toList()

            // Проверяем, что старое состояние не изменилось
            assertEquals(List(9) { CellState.EMPTY }, initialState)
            // Проверяем, что новое состояние имеет X в позиции 0
            assertEquals(CellState.X, updatedState[0])
            assertEquals(CellState.EMPTY, updatedState[1])
        }
    }

    @Test
    fun `onIntent_NewRound_сбрасывает_счет_и_поле_в_IDLE`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.NewRound)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.NewGame)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.Idle)
            assertTrue(uiState.board.cells.all { it == CellState.EMPTY })
            assertEquals(0, uiState.board.scoreX)
            assertEquals(0, uiState.board.scoreO)
            assertEquals(0, uiState.board.drawCount)
        }
    }

    @Test
    fun `onIntent_NewRound_сохраняет_счет`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(3)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(300)
            viewModel.onIntent(UiIntent.NewRound)
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(200) в NewRound

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.Playing)
            assertEquals(1, uiState.board.scoreX) // X победил
            assertEquals(0, uiState.board.scoreO)
        }
    }

    @Test
    fun `onIntent_NewRound_счет_X_увеличивается`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            
            // Первая победная партия для X
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(3)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(300)
            
            // Проверяем, что счет X = 1 после первой победы
            val uiStateAfterFirstWin = viewModel.uiState.value
            assertEquals(1, uiStateAfterFirstWin.board.scoreX)
            assertEquals(0, uiStateAfterFirstWin.board.scoreO)
            
            viewModel.onIntent(UiIntent.NewRound)
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(200) в NewRound
            
            // Вторая победная партия для X (после победы X первым ходит O)
            viewModel.onIntent(UiIntent.CellClicked(3)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(8)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(300)
            viewModel.onIntent(UiIntent.NewRound) // еще одна победа X
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(200) в NewRound

            val uiState = viewModel.uiState.value
            assertEquals(2, uiState.board.scoreX)
            assertEquals(0, uiState.board.scoreO)
        }
    }

    @Test
    fun `onIntent_NewRound_счет_O_увеличивается`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(3)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(8)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(5)) // O - победа (линия 3,4,5)
            advanceUntilIdle()
            advanceUntilIdle() // Дожидаемся завершения delay(300)
            
            // Проверяем, что счет O увеличился после победы
            val uiStateAfterWin = viewModel.uiState.value
            assertEquals(0, uiStateAfterWin.board.scoreX)
            assertEquals(1, uiStateAfterWin.board.scoreO)
            
            viewModel.onIntent(UiIntent.NewRound)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertEquals(0, uiState.board.scoreX)
            assertEquals(1, uiState.board.scoreO)
            assertTrue(uiState.gameState is GameState.Playing)
        }
    }

    @Test
    fun `onIntent_NewRound_счет_ничьих_увеличивается`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            // Реальная ничья без линий 3 в ряд: listOf(0, 1, 2, 4, 3, 6, 5, 8, 7)
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(2)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(3)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(6)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(5)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(8)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(7)) // X - ничья
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.NewRound)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertEquals(1, uiState.board.drawCount)
        }
    }

    @Test
    fun `onIntent_CellClicked_игнорирует_клик_по_занятой_клетке`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // O (клетка занята, должен быть игнорирован)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertEquals(CellState.X, uiState.board.cells[0])
            assertEquals(Player.O, uiState.board.currentPlayer)
            assertEquals(1, uiState.board.cells.filter { it == CellState.X }.size)
        }
    }

    @Test
    fun `onIntent_NewRound_корректно_обрабатывает_ничью`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            // Реальная ничья без линий 3 в ряд: listOf(0, 1, 2, 4, 3, 6, 5, 8, 7)
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(2)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(3)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(6)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(5)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(8)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(7)) // X - ничья
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.NewRound)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            assertTrue(uiState.gameState is GameState.Playing)
            assertEquals(1, uiState.board.drawCount)
        }
    }

    @Test
    fun `isBoardLocked_true_после_победы_false_после_NewRound`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(0)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(3)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(1)) // X
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(4)) // O
            advanceUntilIdle()
            viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isBoardLocked)

            viewModel.onIntent(UiIntent.NewRound)
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isBoardLocked)
        }
    }

    @Test
    fun `currentPlayer - корректный игрок после хода`() {
        runTest(testDispatcher) {
            viewModel.onIntent(UiIntent.StartGame)
            advanceUntilIdle()
            assertEquals(Player.X, viewModel.uiState.value.board.currentPlayer)

            viewModel.onIntent(UiIntent.CellClicked(0))
            advanceUntilIdle()
            assertEquals(Player.O, viewModel.uiState.value.board.currentPlayer)

            viewModel.onIntent(UiIntent.CellClicked(1))
            advanceUntilIdle()
            assertEquals(Player.X, viewModel.uiState.value.board.currentPlayer)
        }
    }
}
