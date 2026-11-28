package com.example.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.game.ui.theme.TicTacToeTheme
import com.example.game.ui.TicTacToeScreen
import com.example.game.presentation.TicTacToeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TicTacToeViewModel by viewModels<TicTacToeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeTheme {
                val uiState by viewModel.uiState.collectAsState()

                TicTacToeScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Сохраняем состояние игры в Bundle
        // В будущем можно добавить более сложное сохранение через SavedStateHandle
        // val gameStateJson = Gson().toJson(uiState)
        // outState.putString("gameState", gameStateJson)
    }
}
