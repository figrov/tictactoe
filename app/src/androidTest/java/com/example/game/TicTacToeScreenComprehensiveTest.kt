package com.example.game.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.SavedStateHandle
import com.example.game.domain.GameBoard
import com.example.game.domain.CellState
import com.example.game.domain.Player
import com.example.game.presentation.TicTacToeViewModel
import com.example.game.presentation.UiIntent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

/**
 * UI-тесты для TicTacToeScreen
 */
@RunWith(AndroidJUnit4::class)
class TicTacToeScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `screen_idle_displaysTitleAndStartButton`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        composeTestRule.onNodeWithText("Крестики-нолики").assertIsDisplayed()
        composeTestRule.onNodeWithText("Начать игру").assertIsDisplayed()
    }

    @Test
    fun `screen_playing_displaysStatusAndScore`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)

        composeTestRule.onNodeWithText("Ход: X").assertIsDisplayed()
        composeTestRule.onNodeWithText("X: 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("O: 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Draw: 0").assertIsDisplayed()
    }

    @Test
    fun `screen_playing_displaysBoardCells`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)

        // Проверяем все 9 клеток с правильными testTag
        for (i in 0..8) {
            composeTestRule.onNodeWithTag("board_cell_$i").assertIsDisplayed()
        }
    }

    @Test
    fun `screen_playing_xMakesFirstMove`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0))

        composeTestRule.onNodeWithTag("board_cell_0").assertIsDisplayed()
        composeTestRule.onNodeWithText("X").assertIsDisplayed()
    }

    @Test
    fun `screen_playing_oMakesSecondMove`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O

        composeTestRule.onNodeWithTag("board_cell_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("O").assertIsDisplayed()
    }

    @Test
    fun `screen_gameOver_displaysWinnerMessage`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа

        composeTestRule.onNodeWithText("Победил X!").assertIsDisplayed()
    }

    @Test
    fun `screen_gameOver_displaysGameOverScreen`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа

        composeTestRule.onNodeWithText("Продолжить").assertIsDisplayed()
        composeTestRule.onNodeWithText("Новая игра").assertIsDisplayed()
    }

    @Test
    fun `screen_gameOver_displaysFinalScore`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
        viewModel.onIntent(UiIntent.NewRound)
        viewModel.onIntent(UiIntent.CellClicked(3)) // X
        viewModel.onIntent(UiIntent.CellClicked(4)) // O
        viewModel.onIntent(UiIntent.CellClicked(5)) // X - победа
        viewModel.onIntent(UiIntent.NewRound)
        viewModel.onIntent(UiIntent.CellClicked(6)) // X
        viewModel.onIntent(UiIntent.CellClicked(7)) // O
        viewModel.onIntent(UiIntent.CellClicked(8)) // X - победа

        composeTestRule.onNodeWithText("X: 3").assertIsDisplayed()
        composeTestRule.onNodeWithText("O: 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Draw: 0").assertIsDisplayed()
    }

    @Test
    fun `screen_gameOver_drawDisplaysDrawMessage`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        // Заполняем все клетки для ничьей
        listOf(0, 1, 2, 3, 4, 5, 6, 7, 8).forEach { index ->
            viewModel.onIntent(UiIntent.CellClicked(index))
        }

        composeTestRule.onNodeWithText("Ничья!").assertIsDisplayed()
    }

    @Test
    fun `screen_gameOver_boardIsLockedAfterWin`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа

        // После победы клик по любой клетке должен игнорироваться
        // В UI тесте проверяем, что можно нажать, но ничего не должно измениться
        composeTestRule.onNodeWithTag("board_cell_0").performClick()
        composeTestRule.onNodeWithTag("board_cell_1").performClick()

        val board = viewModel.uiState.value.board
        assertEquals(CellState.X, board.cells[0])
        assertEquals(CellState.O, board.cells[1])
    }

    @Test
    fun `screen_playing_ignoresClickOnOccupiedCell`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(0)) // O (повторный клик по занятой)

        composeTestRule.onNodeWithText("O").assertDoesNotExist() // O не должен появиться
    }

    @Test
    fun `testTag_board_isPresent`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        composeTestRule.onNodeWithTag("board").assertIsDisplayed()
    }

    @Test
    fun `testTag_status_isPresent`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        composeTestRule.onNodeWithTag("status").assertIsDisplayed()
    }

    @Test
    fun `testTag_scores_isPresent`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        composeTestRule.onNodeWithTag("scores").assertIsDisplayed()
    }

    @Test
    fun `testTag_action_buttons_isPresent`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        composeTestRule.onNodeWithTag("action_buttons").assertIsDisplayed()
    }

    @Test
    fun `testTag_btn_start_isPresentInIdleScreen`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        composeTestRule.onNodeWithTag("btn_start_idle").assertIsDisplayed()
    }

    @Test
    fun `testTag_btn_continue_isPresentInGameOverScreen`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа

        composeTestRule.onNodeWithTag("btn_continue").assertIsDisplayed()
    }

    @Test
    fun `testTag_btn_new_game_isPresentInPlayingAndGameOver`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        // Проверяем в PLAYING
        viewModel.onIntent(UiIntent.StartGame)
        composeTestRule.onNodeWithTag("btn_new_game").assertIsDisplayed()

        // Проверяем в GAME_OVER
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа
        composeTestRule.onNodeWithTag("btn_new_game").assertIsDisplayed()
    }

    @Test
    fun `testTag_allCellsArePresent`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)

        for (i in 0..8) {
            composeTestRule.onNodeWithTag("board_cell_$i").assertIsDisplayed()
        }
    }

    @Test
    fun `testTag_win_line_isPresentDuringGameOverAnimation`() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }

        viewModel.onIntent(UiIntent.StartGame)
        viewModel.onIntent(UiIntent.CellClicked(0)) // X
        viewModel.onIntent(UiIntent.CellClicked(1)) // O
        viewModel.onIntent(UiIntent.CellClicked(2)) // X - победа

        // После завершения анимации winLine должен быть скрыт, но компонент существует
        // Мы проверяем, что winLine компонент может быть отрисован
        composeTestRule.onNodeWithTag("win_line").assertIsDisplayed()
    }

    private val viewModel: TicTacToeViewModel
        get() = TicTacToeViewModel(SavedStateHandle())
}
