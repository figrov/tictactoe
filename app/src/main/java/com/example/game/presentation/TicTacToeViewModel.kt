package com.example.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.example.game.domain.GameState
import com.example.game.domain.GameBoard
import com.example.game.domain.CellState
import com.example.game.domain.Player
import kotlinx.coroutines.delay
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

    private val savedStateHandle: SavedStateHandle = savedStateHandle

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effects = Channel<UiEffect>(Channel.BUFFERED)
    val effects: Flow<UiEffect> = _effects.receiveAsFlow()

    init {
        loadSavedState(savedStateHandle)
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
        // Сохраняем состояние после обработки каждого intent
        saveState()
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

        // Проверка валидности хода (клетка должна быть пустой)
        if (board.cells[index] != CellState.EMPTY) {
            sendEffect(UiEffect.IgnoreOccupiedCell)
            return
        }

        // Сначала ставим символ в клетку
        val newBoard = applyMove(board, index, null)

        // Теперь проверяем победу/ничью на новой доске
        val (isWin, winningCells) = newBoard.checkWin()
        val isDraw = newBoard.checkDraw()

        // Обновляем состояние
        val updatedGameState: GameState
        val updatedMessage: String
        val finalBoard: GameBoard

        if (isWin) {
            // Победа - обновляем доску с информацией о победе и счетом
            val winner = if (newBoard.cells[index] == CellState.X) Player.X else Player.O
            val updatedScoreX = if (winner == Player.X) board.scoreX + 1 else board.scoreX
            val updatedScoreO = if (winner == Player.O) board.scoreO + 1 else board.scoreO
            
            // Важно: используем board.scoreX и board.scoreO, а не newBoard.scoreX
            finalBoard = newBoard.copy(
                isWinningCells = winningCells,
                winner = winner,
                scoreX = updatedScoreX,
                scoreO = updatedScoreO,
                currentPlayer = winner // Победивший игрок остается текущим для отображения результата
            )

            updatedGameState = GameState.GameOver
            updatedMessage = "Победил ${finalBoard.winner?.displayName}!"

            // Сразу блокируем поле и обновляем счет
            _uiState.value = currentState.copy(
                gameState = GameState.GameOver,
                board = finalBoard,
                isBoardLocked = true,
                isGameOverAnimating = true,
                message = updatedMessage
            )

            // Запускаем анимацию
            viewModelScope.launch {
                delay(300) // Задержка для анимации линии победы
                _uiState.value = _uiState.value.copy(
                    gameState = GameState.GameOver,
                    board = finalBoard,
                    isBoardLocked = true,
                    isGameOverAnimating = false,
                    message = updatedMessage
                )
                sendEffect(UiEffect.WinnerFound)
            }
        } else if (isDraw) {
            // Ничья - обновляем доску с увеличением счетчика ничьих
            finalBoard = newBoard.copy(
                drawCount = board.drawCount + 1,
                currentPlayer = board.currentPlayer.opponent // При ничьей ход переходит к другому игроку
            )

            updatedGameState = GameState.GameOver
            updatedMessage = "Ничья!"

            // Сразу блокируем поле
            _uiState.value = currentState.copy(
                gameState = GameState.GameOver,
                board = finalBoard,
                isBoardLocked = true,
                isGameOverAnimating = true,
                message = updatedMessage
            )

            viewModelScope.launch {
                delay(300)
                _uiState.value = _uiState.value.copy(
                    gameState = GameState.GameOver,
                    board = finalBoard,
                    isBoardLocked = true,
                    isGameOverAnimating = false,
                    message = updatedMessage
                )
                sendEffect(UiEffect.DrawFound)
            }
        } else {
            // Продолжение игры - просто применяем ход и меняем игрока
            val boardWithSwitchedPlayer = newBoard.copy(currentPlayer = newBoard.currentPlayer.opponent)
            updatedGameState = GameState.Playing(boardWithSwitchedPlayer)
            updatedMessage = "Ход: ${boardWithSwitchedPlayer.currentPlayer.displayName}"

            _uiState.value = currentState.copy(
                gameState = updatedGameState,
                board = boardWithSwitchedPlayer,
                isBoardLocked = false, // Поле не заблокировано в обычном ходе
                message = updatedMessage
            )
            sendEffect(UiEffect.ClickEmptyCell)
        }
    }

    /**
     * Применяем ход на доску с глубоким клонированием
     */
    private fun applyMove(board: GameBoard, index: Int, isWinningCells: List<Int>? = null): GameBoard {
        val newCells = board.cells.mapIndexed { i, cellState ->
            if (i == index) {
                board.currentPlayer.toCellState()
            } else {
                cellState
            }
        }

        val newBoard = board.copy(
            cells = newCells,
            isWinningCells = isWinningCells,
            winner = if (isWinningCells != null) board.currentPlayer else board.winner,
            currentPlayer = board.currentPlayer, // Игрок меняется только после проверки победы/ничьей
            scoreX = board.scoreX, // Сохраняем счет
            scoreO = board.scoreO, // Сохраняем счет
            drawCount = board.drawCount // Сохраняем счет ничьих
        )

        return newBoard
    }

    /**
     * Обработка новой игры (возврат в IDLE с очисткой счета)
     */
    private fun handleNewGame() {
        val currentState = _uiState.value

        _uiState.value = currentState.copy(
            gameState = GameState.Idle,
            board = GameBoard.INITIAL,
            isBoardLocked = false,
            isGameOverAnimating = false,
            message = "Добро пожаловать в крестики-нолики!"
        )
        sendEffect(UiEffect.NewGame)
    }

    /**
     * Обработка новой партии (продолжение с сохранением счета)
     */
    private fun handleNewRound() {
        val currentState = _uiState.value
        val board = currentState.board

        // Создаем новую доску с сохранением счета
        val newBoard = GameBoard(
            cells = List(9) { CellState.EMPTY },
            currentPlayer = when (board.winner) {
                Player.X -> Player.O  // Очередной ход после X — это O
                Player.O -> Player.X  // Очередной ход после O — это X
                else -> Player.X  // При ничьей или начальной игре, начинаем с X
            },
            scoreX = board.scoreX,
            scoreO = board.scoreO,
            drawCount = board.drawCount,
            winner = null,
            isWinningCells = null
        )

        val newState = currentState.copy(
            gameState = GameState.Playing(newBoard),
            board = newBoard,
            isBoardLocked = false, // Явно устанавливаем false при новой партии
            isGameOverAnimating = false,
            message = "Ход: ${newBoard.currentPlayer.displayName}"
        )

        _uiState.value = newState
        sendEffect(UiEffect.NewRound)
    }

    /**
     * Обработка продолжения игры (перезапуск текущей партии)
     */
    private fun handleContinueGame() {
        val currentState = _uiState.value
        val board = currentState.board

        viewModelScope.launch {
            delay(200)
            val nextPlayer = board.winner?.opponent ?: Player.X
            _uiState.value = currentState.copy(
                gameState = GameState.Playing(
                    GameBoard(
                        cells = List(9) { CellState.EMPTY },
                        currentPlayer = nextPlayer,
                        scoreX = board.scoreX,
                        scoreO = board.scoreO,
                        winner = null,
                        isWinningCells = null
                    )
                ),
                isBoardLocked = false,
                message = "Ход: $nextPlayer"
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
     * Загрузка сохраненного состояния из SavedStateHandle
     */
    private fun loadSavedState(savedStateHandle: SavedStateHandle) {
        try {
            val boardString = savedStateHandle.get<String>(KEY_BOARD_STRING)
            if (boardString != null) {
                val restoredBoard = restoreGameBoardFromString(boardString)
                if (restoredBoard != null) {
                    _uiState.value = UiState(
                        gameState = GameState.Playing(restoredBoard),
                        board = restoredBoard,
                        isBoardLocked = false,
                        isGameOverAnimating = false,
                        message = "Добро пожаловать в крестики-нолики!"
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore errors during state restoration
            _uiState.value = UiState()
        }
    }

    /**
     * Сохранение текущего состояния в SavedStateHandle
     */
    private fun saveState() {
        val currentState = _uiState.value
        val boardString = formatGameBoardToString(currentState.board)
        val message = currentState.message

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
        // Канал будет автоматически закрыт при завершении coroutineScope
    }
}
