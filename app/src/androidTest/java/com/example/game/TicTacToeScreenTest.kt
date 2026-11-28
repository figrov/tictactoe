package com.example.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.SavedStateHandle
import com.example.game.ui.TicTacToeScreen
import com.example.game.presentation.TicTacToeViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TicTacToeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ticTacToeScreen_displaysText() {
        composeTestRule.setContent {
            TicTacToeScreen(viewModel = TicTacToeViewModel(SavedStateHandle()))
        }
        composeTestRule.onNodeWithText("Крестики-нолики").assertIsDisplayed()
    }
}
