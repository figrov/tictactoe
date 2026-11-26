package com.example.game.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.domain.GameState
import com.example.game.domain.GameBoard
import com.example.game.domain.CellState
import com.example.game.domain.Player
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel с MVI паттерном (Model-View-Intent)
 * Обрабатывает Intents и обновляет UiState
 */
class TicTacToeViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effects = Channel<UiEffect>(Channel.BUFFERED)
    val effects: Flow<UiEffect> = _effects.receiveAsFlow()

    init {
        loadSavedState(savedStateHandle)
        setupRecomposition()
    }

    // Ключи для сохранения состояния
    private val KEY_GAME_STATE = "gameState"
    private val KEY_BOARD_STRING = "boardString"
    private val KEY_MESSAGE = "message"

    /**
     * Обработка intents
     */
    fun onIntent(intent: UiIntent) {
        when (intent) {
            is UiIntent.StartGame -> handleStartGame()
            is UiIntent.CellClicked -> handleCellClicked(intent.index)
            is UiIntent.NewRound -> handleNewRound()
            is UiIntent.NewGame -> handleNewGame()
            is UiIntent.ContinueGame -> handleContinueGame()
        }
    }

    /**
     * Обработка старта игры
     */
    private fun handleStartGame() {
        _uiState.value = _uiState.value.copy(
            gameState = GameState.Playing(GameBoard.INITIAL),
            board = GameBoard.INITIAL,
            isBoardLocked = false,
            message = "Ход: X"
        )
    }

    /**
     * Обработка клика по клетке
     */
    private fun handleCellClicked(index: Int) {
        val currentState = _uiState.value
        val board = currentState.board

        // Проверка блокировки доски
        if (currentState.isBoardLocked) {
            return
        }

        // Проверка валидности хода
        if (!board.cells.isValidMove(index)) {
            if (board.cells[index] != CellState.EMPTY) {
                sendEffect(UiEffect.IgnoreOccupiedCell)
            }
            return
        }

        // Запуск анимации клетки (локальная, не в UiState)
        viewModelScope.launch {
            kotlinx.coroutines.delay(150)
            _uiState.value = currentState.copy(
                isCellAnimating = currentState.isCellAnimating.toMutableMap().apply { this[index] = false }
            )
        }

        // Применяем ход
        val newBoard = applyMove(board, index)

        // Проверяем победу/ничью
        val stateMachine = GameStateMachine(newBoard.cells, newBoard.isWinningCells)
        val (isWin, winningCells) = stateMachine.checkWin()
        val isDraw = stateMachine.checkDraw()

        // Обновляем состояние
        val updatedGameState: GameState
        val updatedMessage: String

        if (isWin) {
            // Победа
            val winner = newBoard.isWinningCells!![0]
            updatedGameState = GameState.Playing(
                newBoard.copy(
                    winner = newBoard.cells[winner],
                    isWinningCells = winningCells,
                    currentPlayer = newBoard.cells[winner].opponent() // Следующий ход
                )
            )
            updatedMessage = "Победил ${newBoard.cells[winner].displayName()}!"

            // Анимация завершения игры
            _uiState.value = currentState.copy(
                isGameOverAnimating = true,
                isCellAnimating = currentState.isCellAnimating.toMutableMap().apply { this[index] = false }
            )

            // Запускаем таймер для снятия блокировки
            viewModelScope.launch {
                kotlinx.coroutines.delay(300) // Задержка для анимации линии победы
                _uiState.value = currentState.copy(
                    gameState = GameState.GameOver,
                    isBoardLocked = true,
                    isGameOverAnimating = false,
                    isCellAnimating = currentState.isCellAnimating.toMutableMap().apply { this[index] = false }
                )
                sendEffect(UiEffect.WinnerFound)
            }
        } else if (isDraw) {
            // Ничья
            updatedGameState = GameState.Playing(
                newBoard.copy(
                    drawCount = board.drawCount + 1
                )
            )
            updatedMessage = "Ничья!"

            _uiState.value = currentState.copy(
                isGameOverAnimating = true,
                isCellAnimating = currentState.isCellAnimating.toMutableMap().apply { this[index] = false }
            )

            viewModelScope.launch {
                kotlinx.coroutines.delay(300)
                _uiState.value = currentState.copy(
                    gameState = GameState.GameOver,
                    isBoardLocked = true,
                    isGameOverAnimating = false,
                    isCellAnimating = currentState.isCellAnimating.toMutableMap().apply { this[index] = false }
                )
                sendEffect(UiEffect.DrawFound)
            }
        } else {
            // Продолжение игры
            updatedGameState = GameState.Playing(
                newBoard.copy(currentPlayer = board.currentPlayer.opponent())
            )
            updatedMessage = "Ход: ${newBoard.currentPlayer.displayName()}"

            _uiState.value = currentState.copy(
                gameState = updatedGameState,
                isCellAnimating = currentState.isCellAnimating.toMutableMap().apply { this[index] = false },
                message = updatedMessage
            )
            sendEffect(UiEffect.ClickEmptyCell)
        }
    }

    /**
     * Применяем ход на доску
     */
    private fun applyMove(board: GameBoard, index: Int): GameBoard {
        val newCells = board.cells.toMutableList()
        newCells[index] = board.currentPlayer

        return board.copy(
            cells = newCells,
            isWinningCells = null, // Сбрасываем выигрышные клетки до проверки
            winner = null
        )
    }

    /**
     * Обработка новой игры (возврат в IDLE с очисткой счета)
     */
    private fun handleNewGame() {
        val currentState = _uiState.value

        viewModelScope.launch {
            kotlinx.coroutines.delay(200)
            _uiState.value = currentState.copy(
                gameState = GameState.Idle,
                board = GameBoard.INITIAL,
                isBoardLocked = false,
                message = "Добро пожаловать в крестики-нолики!"
            )
            sendEffect(UiEffect.NewGame)
        }
    }

    /**
     * Обработка новой партии (продолжение с сохранением счета)
     */
    private fun handleNewRound() {
        val currentState = _uiState.value
        val board = currentState.board

        viewModelScope.launch {
            kotlinx.coroutines.delay(200)
            val newState = when (val winner = board.winner) {
                is Player.X -> {
                    currentState.copy(
                        gameState = GameState.Playing(
                            GameBoard(
                                cells = List(9) { CellState.EMPTY },
                                currentPlayer = Player.O,
                                scoreX = board.scoreX + 1
                            )
                        ),
                        isBoardLocked = false,
                        message = "Ход: O"
                    )
                }
                is Player.O -> {
                    currentState.copy(
                        gameState = GameState.Playing(
                            GameBoard(
                                cells = List(9) { CellState.EMPTY },
                                currentPlayer = Player.X,
                                scoreO = board.scoreO + 1
                            )
                        ),
                        isBoardLocked = false,
                        message = "Ход: X"
                    )
                }
                else -> {
                    currentState.copy(
                        gameState = GameState.Playing(
                            GameBoard(
                                cells = List(9) { CellState.EMPTY },
                                currentPlayer = Player.X,
                                drawCount = board.drawCount + 1
                            )
                        ),
                        isBoardLocked = false,
                        message = "Ход: X"
                    )
                }
            }

            _uiState.value = newState
            sendEffect(UiEffect.NewRound)
        }
    }

    /**
     * Обработка продолжения игры (перезапуск текущей партии)
     */
    private fun handleContinueGame() {
        val currentState = _uiState.value
        val board = currentState.board

        viewModelScope.launch {
            kotlinx.coroutines.delay(200)
            val nextPlayer = board.winner?.opponent() ?: Player.X
            _uiState.value = currentState.copy(
                gameState = GameState.Playing(
                    GameBoard(
                        cells = List(9) { CellState.EMPTY },
                        currentPlayer = nextPlayer,
                        scoreX = board.scoreX,
                        scoreO = board.scoreO
                    )
                ),
                isBoardLocked = false,
                message = "Ход: $nextPlayer",
                isCellAnimating = mutableMapOf()
            )
            sendEffect(UiEffect.NewRound)
        }
    }

    /**
     * Отправка побочного эффекта
     */
    private fun sendEffect(effect: UiEffect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }

    /**
     * Получаем эффекты из канала
     */
    private fun consumeEffects(channel: ReceiveChannel<UiEffect>): Flow<UiEffect> {
        return channel.consumeEach { effect ->
            // Обработка эффектов здесь
        }.flow()
    }

    /**
     * Загрузка сохраненного состояния из SavedStateHandle
     */
    private fun loadSavedState(savedStateHandle: SavedStateHandle) {
        val gameStateOrdinal = savedStateHandle.get<Int>(KEY_GAME_STATE)
        val boardString = savedStateHandle.get<String>(KEY_BOARD_STRING)
        val message = savedStateHandle.get<String>(KEY_MESSAGE)

        if (gameStateOrdinal != null && boardString != null) {
            // Восстанавливаем GameState из ordinal
            val restoredGameState = GameStateType.values()[gameStateOrdinal]
            val gameState = when (restoredGameState) {
                GameStateType.IDLE -> GameState.Idle
                GameStateType.PLAYING -> {
                    val restoredBoard = restoreGameBoardFromString(boardString)
                    if (restoredBoard != null) {
                        GameState.Playing(restoredBoard)
                    } else {
                        GameState.Idle
                    }
                }
                GameStateType.GAME_OVER -> GameState.GameOver
                GameStateType.PAUSED -> GameState.Paused
            }

            // Восстанавливаем UiState
            val restoredUiState = UiState(
                gameState = gameState,
                board = restoredBoard ?: GameBoard.INITIAL,
                isBoardLocked = restoredGameState != GameStateType.PLAYING,
                isGameOverAnimating = false,
                message = message ?: "Добро пожаловать в крестики-нолики!"
            )

            _uiState.value = restoredUiState
        }
    }

    /**
     * Сохранение текущего состояния в SavedStateHandle
     */
    private fun saveState() {
        val currentState = _uiState.value
        val gameStateOrdinal = currentState.gameState.gameState.ordinal
        val boardString = formatGameBoardToString(currentState.board)
        val message = currentState.message

        savedStateHandle[KEY_GAME_STATE] = gameStateOrdinal
        savedStateHandle[KEY_BOARD_STRING] = boardString
        savedStateHandle[KEY_MESSAGE] = message
    }

    /**
     * Форматирует игровое поле в строку с разделителем
     */
    private fun formatGameBoardToString(board: GameBoard): String {
        return board.cells.joinToString(",") { it.name }
    }

    /**
     * Восстанавливает игровое поле из строки
     */
    private fun restoreGameBoardFromString(boardString: String): GameBoard? {
        return try {
            val cells = boardString.split(",").mapNotNull { cellName ->
                CellState.valueOf(cellName)
            }

            if (cells.size == 9) {
                GameBoard(cells = cells)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Настройка автоматического сохранения состояния при изменении
     */
    private fun setupRecomposition() {
        viewModelScope.launch {
            _uiState.collect { state ->
                // Автоматическое сохранение при каждом изменении состояния
                saveState()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Закрываем канал эффектов
        viewModelScope.launch {
            _effects.consumeEach { effect -> }
        }
    }
}
