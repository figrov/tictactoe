package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.domain.GameBoard
import com.example.game.domain.GameState
import com.example.game.ui.ActionButton
import com.example.game.ui.ActionButtons
import com.example.game.domain.CellState
import com.example.game.domain.Player
import com.example.game.presentation.TicTacToeViewModel
import com.example.game.presentation.UiIntent
import com.example.game.presentation.UiState

/**
 * Основной экран игры
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeScreen(
    viewModel: TicTacToeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameState = uiState.gameState

    // Локальное состояние для отслеживания кликов по клеткам
    var clickedIndices by remember { mutableStateOf(emptySet<Int>()) }

    LaunchedEffect(clickedIndices) {
        // Сбрасываем кликнутые индексы через 300мс
        kotlinx.coroutines.delay(300)
        clickedIndices = emptySet()
    }

    // Определяем, какие клетки выигрывают
    val winningCells = uiState.board.isWinningCells ?: emptyList()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        // Экран приветствия (IDLE)
        AnimatedVisibility(
            visible = gameState is GameState.Idle,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.fillMaxSize()
        ) {
            IdleScreen(
                onStartGame = { viewModel.onIntent(UiIntent.StartGame) }
            )
        }

        // Игровой экран (PLAYING)
        AnimatedVisibility(
            visible = gameState is GameState.Playing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayingScreen(
                uiState = uiState,
                onCellClick = { index ->
                    viewModel.onIntent(UiIntent.CellClicked(index))
                },
                onNewRound = { viewModel.onIntent(UiIntent.NewRound) },
                onNewGame = { viewModel.onIntent(UiIntent.NewGame) }
            )
        }

        // Экран завершения (GAME_OVER)
        AnimatedVisibility(
            visible = gameState is GameState.GameOver,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.fillMaxSize()
        ) {
            GameOverScreen(
                uiState = uiState,
                onContinue = { viewModel.onIntent(UiIntent.ContinueGame) },
                onNewGame = { viewModel.onIntent(UiIntent.NewGame) }
            )
        }
    }

    // Основной контейнер экрана
    // (testTag уже добавлен к Box выше)
}

/**
 * Экран приветствия
 */
@Composable
fun IdleScreen(
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Крестики-нолики",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Классическая игра для двоих",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        ActionButton(
            text = "Начать игру",
            onClick = onStartGame,
            enabled = true
        )
    }
}

/**
 * Игровой экран (PLAYING)
 */
@Composable
fun PlayingScreen(
    uiState: UiState,
    onCellClick: (Int) -> Unit,
    onNewRound: () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Заголовок
        StatusMessage(
            message = uiState.message,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Счет
        ScoreDisplay(
            scoreX = uiState.scoreX,
            scoreO = uiState.scoreO,
            drawCount = uiState.drawCount
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Игровое поле
        GameBoard(
            cells = uiState.board.cells,
            onCellClick = onCellClick,
            isBoardLocked = uiState.isBoardLocked,
            isGameOverAnimating = uiState.isGameOverAnimating,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Кнопки действий
        ActionButtons(
            gameState = uiState.gameState,
            onNewRound = onNewRound,
            onNewGame = onNewGame
        )
    }
}

/**
 * Экран завершения (GAME_OVER)
 */
@Composable
fun GameOverScreen(
    uiState: UiState,
    onContinue: () -> Unit,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Сообщение о результате
        val resultText = when (uiState.board.winner) {
            Player.X -> "Победил X!"
            Player.O -> "Победил O!"
            null -> "Ничья!"
        }

        Text(
            text = resultText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Счет в конце игры
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "X: ${uiState.scoreX}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "O: ${uiState.scoreO}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Draw: ${uiState.drawCount}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Кнопки
        ActionButton(
            text = "Продолжить",
            onClick = onContinue,
            enabled = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionButton(
            text = "Новая игра",
            onClick = onNewGame,
            enabled = true
        )
    }
}

/**
 * Игровое поле с сеткой и клетками
 */
@Composable
fun GameBoard(
    cells: List<CellState>,
    onCellClick: (Int) -> Unit,
    isBoardLocked: Boolean,
    isGameOverAnimating: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(300.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Сетка
        Box(modifier = Modifier.fillMaxSize()) {
            for (row in 0..2) {
                for (col in 0..2) {
                    val index = row * 3 + col

                    Cell(
                        index = index,
                        cellState = cells[index],
                        onCellClick = { onCellClick(index) },
                        modifier = Modifier
                            .align(
                                if (col == 0) Alignment.TopStart
                                else if (col == 2) Alignment.TopEnd
                                else Alignment.CenterStart
                            )
                            .offset(
                                when (col) {
                                    0 -> 0.dp
                                    1 -> 0.dp
                                    2 -> 0.dp
                                    else -> 0.dp
                                }
                            )
                            .padding(1.dp)
                    )
                }
            }
        }

        // Линия победы (отдельно от клеток для правильного z-index)
        if (isGameOverAnimating) {
            WinLine(
                board = com.example.game.domain.GameBoard(cells = cells),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
