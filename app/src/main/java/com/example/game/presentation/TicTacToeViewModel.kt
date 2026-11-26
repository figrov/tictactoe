package com.example.game.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.domain.GameState
import com.example.game.domain.GameBoard
import com.example.game.domain.CellState
import com.example.game.domain.Player
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

    // Внутренние флаги для спам-протекции
    private val isCellAnimating = mutableMapOf<Int, Boolean>()
    private val isButtonAnimating = mutableMapOf<String, Boolean>()

    // JSON сериализатор для сохранения состояния
    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadSavedState(savedStateHandle)
        setupRecomposition()
    }

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

        // Спам-протекция
        if (currentState.isCellAnimating[index] == true) {
            return
        }

        // Проверка валидности хода
        if (!board.cells.isValidMove(index)) {
            if (board.cells[index].isMarked) {
                sendEffect(UiEffect.IgnoreOccupiedCell)
            }
            return
        }

        // Проверка блокировки доски
        if (currentState.isBoardLocked) {
            return
        }

        // Запуск анимации клетки
        isCellAnimating[index] = true
        _uiState.value = currentState.copy(
            isCellAnimating = currentState.isCellAnimating + (index to true)
        )

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
            val winner = newBoard.cells[newBoard.isWinningCells!![0]]
            updatedGameState = GameState.Playing(
                newBoard.copy(
                    winner = winner,
                    isWinningCells = winningCells,
                    currentPlayer = winner.opponent // Следующий ход
                )
            )
            updatedMessage = "Победил ${winner.displayName}!"

            // Анимация завершения игры
            _uiState.value = currentState.copy(
                isGameOverAnimating = true,
                isCellAnimating = currentState.isCellAnimating + (index to false)
            )

            // Запускаем таймер для снятия блокировки
            viewModelScope.launch {
                kotlinx.coroutines.delay(300) // Задержка для анимации линии победы
                _uiState.value = currentState.copy(
                    gameState = GameState.GameOver,
                    isBoardLocked = true,
                    isGameOverAnimating = false,
                    isCellAnimating = currentState.isCellAnimating + (index to false)
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
                isCellAnimating = currentState.isCellAnimating + (index to false)
            )

            viewModelScope.launch {
                kotlinx.coroutines.delay(300)
                _uiState.value = currentState.copy(
                    gameState = GameState.GameOver,
                    isBoardLocked = true,
                    isGameOverAnimating = false,
                    isCellAnimating = currentState.isCellAnimating + (index to false)
                )
                sendEffect(UiEffect.DrawFound)
            }
        } else {
            // Продолжение игры
            updatedGameState = GameState.Playing(
                newBoard.copy(currentPlayer = board.currentPlayer.opponent)
            )
            updatedMessage = "Ход: ${newBoard.currentPlayer.displayName}"

            _uiState.value = currentState.copy(
                gameState = updatedGameState,
                isCellAnimating = currentState.isCellAnimating + (index to false),
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

        // Анимация кнопки
        if (currentState.isButtonAnimating["newGame"] == true) return

        isButtonAnimating["newGame"] = true

        viewModelScope.launch {
            kotlinx.coroutines.delay(200)
            _uiState.value = currentState.copy(
                gameState = GameState.Idle,
                board = GameBoard.INITIAL,
                isBoardLocked = false,
                message = "Добро пожаловать в крестики-нолики!"
            )
            sendEffect(UiEffect.NewGame)
            isButtonAnimating["newGame"] = false
        }
    }

    /**
     * Обработка новой партии (продолжение с сохранением счета)
     */
    private fun handleNewRound() {
        val currentState = _uiState.value
        val board = currentState.board

        // Анимация кнопки
        if (currentState.isButtonAnimating["newRound"] == true) return

        isButtonAnimating["newRound"] = true

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
                        message = "Ход: O",
                        isCellAnimating = emptyMap()
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
                        message = "Ход: X",
                        isCellAnimating = emptyMap()
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
                        message = "Ход: X",
                        isCellAnimating = emptyMap()
                    )
                }
            }

            _uiState.value = newState
            sendEffect(UiEffect.NewRound)
            isButtonAnimating["newRound"] = false
        }
    }

    /**
     * Обработка продолжения игры (перезапуск текущей партии)
     */
    private fun handleContinueGame() {
        val currentState = _uiState.value
        val board = currentState.board

        // Анимация кнопки
        if (currentState.isButtonAnimating["continue"] == true) return

        isButtonAnimating["continue"] = true

        viewModelScope.launch {
            kotlinx.coroutines.delay(200)
            _uiState.value = currentState.copy(
                gameState = GameState.Playing(
                    GameBoard(
                        cells = List(9) { CellState.EMPTY },
                        currentPlayer = board.winner?.opponent ?: Player.X,
                        scoreX = board.scoreX,
                        scoreO = board.scoreO
                    )
                ),
                isBoardLocked = false,
                message = "Ход: ${board.winner?.opponent ?: Player.X}.displayName",
                isCellAnimating = emptyMap()
            )
            sendEffect(UiEffect.NewRound)
            isButtonAnimating["continue"] = false
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
     * Загрузка сохраненного состояния
     */
    private fun loadSavedState(savedStateHandle: SavedStateHandle) {
        // Здесь можно добавить логику восстановления состояния
        // из SavedStateHandle
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
        // Закрываем эффекты при удалении ViewModel
        viewModelScope.launch {
            _effects.consumeEach { effect ->
                // Здесь можно добавить очистку канала
            }
        }
    }

    /**
     * Сохраняем текущее состояние при изменении
     */
    private fun saveState() {
        val stateToSave = _uiState.value.copy(
            isCellAnimating = emptyMap(), // Не сохраняем анимации
            isButtonAnimating = false
        )
        try {
            val stateJson = json.encodeToString(UiState.serializer(), stateToSave)
            savedStateHandle["savedGameState"] = stateJson
        } catch (e: Exception) {
            // Игнорируем ошибки сериализации при сохранении
        }
    }

    /**
     * Восстанавливаем состояние из SavedStateHandle
     */
    private fun loadSavedState(savedStateHandle: SavedStateHandle) {
        savedStateHandle.get<String>("savedGameState")?.let { stateJson ->
            try {
                val restoredState = json.decodeFromString(UiState.serializer(), stateJson)
                _uiState.value = restoredState
            } catch (e: Exception) {
                // Восстанавливаем дефолтное состояние при ошибке
                _uiState.value = UiState()
            }
        }
    }
}
