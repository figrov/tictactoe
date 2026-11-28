# Архитектура проекта (Single Source of Truth)

## 1. Стек
- Kotlin 2.0.21, Jetpack Compose (Material 3), Clean Architecture + MVI (`StateFlow` + `Channel`).
- Тесты: JUnit 5, MockK, Turbine, `StandardTestDispatcher`, Compose UI Test.

## 2. Структура пакетов (`com.example.game`)
- `domain/` — чистая игровая логика и State Machine
- `presentation/` — ViewModel, UiState, UiIntent, UiEffect
- `ui/` — Compose-экраны и компоненты

## 3. Реестр ключевых классов

### Domain слой (чистая игровая логика)
- `Player.kt` — перечисление игроков X/O.
- `GameState.kt` — sealed interface состояний игры (Idle, Playing, GameOver).
- `CellState.kt` — состояния клетки (EMPTY, X, O).
- `GameBoard.kt` — управление игровым полем:
  - `isValidMove(index: Int): Boolean` — проверка валидности хода
  - `checkWin(): Pair<Boolean, List<Int>?>` — проверка победы (возвращает Pair<есть_ли_победитель, список_выигравших_клеток>)
  - `checkDraw(): Boolean` — проверка ничьи (все клетки заполнены, победителя нет)
  - `getWinningCombinations(): List<List<Int>>` — получение всех выигрышных комбинаций (горизонтали, вертикали, диагонали)

### Presentation слой (MVI-архитектура)
- `TicTacToeViewModel.kt` — ViewModel с UiState, UiIntent, UiEffect (исправлены критические баги: порядок проверки победы, обновление board, корректное сохранение состояния).
- `UiState.kt` — sealed класс состояния UI.
- `UiIntent.kt` — sealed класс намерений пользователя.
- `UiEffect.kt` — sealed класс эффектов (звук, вибрация, подтверждение).

### UI слой (Jetpack Compose)
- `TicTacToeScreen.kt` — главный экран с тремя анимированными состояниями (Idle/Playing/GameOver).
  - Экран приветствия (Idle) с кнопкой "Начать игру" (testTag: screen_idle, btn_start).
  - Игровой экран (Playing) с полем, счетом и индикатором хода (testTag: screen_playing, grid, indicator_turn, score_x, score_o, score_draw, board_cell_00..22).
  - Экран завершения (GameOver) с результатом и кнопками (testTag: screen_game_over, btn_continue, btn_new_game).
- `Cell.kt` — анимированная клетка с пульсацией (testTag: board_cell_00..22).
- `XIcon.kt` / `OIcon.kt` — иконки X и O.
- `WinLine.kt` — анимированная линия победы (testTag: win_line).
- `StatusMessage.kt` — сообщение о ходе (testTag: indicator_turn).
- `ScoreDisplay.kt` — счетчик X/O/Draw (testTag: scores, score_x, score_o, score_draw).
- `ActionButtons.kt` — контейнер кнопок действий (testTag: action_buttons, btn_start, btn_continue, btn_new_game).
- `ActionButton.kt` — кнопка с анимацией нажатия.

### Ключевые исправления в TicTacToeViewModel:
- **handleCellClicked()**: Исправлен порядок проверки победы (сначала ставим символ, потом проверяем) и обновление board во всех ветках
- **handleNewRound()/handleNewGame()**: Удалены лишние delay(200) для мгновенного обновления
- **init**: Удален setupRecomposition() для избежания UncaughtExceptionsBeforeTest
- **onIntent()**: Добавлен saveState() в конце для корректного сохранения состояния

### Entry point
- `MainActivity.kt` — точка входа, навигация на TicTacToeScreen.

## 4. Реестр семантических тегов (`Modifier.testTag`)

**Экранные контейнеры:**
- `board` — игровое поле.
- `status` — сообщение о ходе.
- `scores` — отображение счета.
- `action_buttons` — контейнер кнопок действий.

**Клетки игрового поля:**
- `board_cell_00`, `board_cell_01`, `board_cell_02`
- `board_cell_10`, `board_cell_11`, `board_cell_12`
- `board_cell_20`, `board_cell_21`, `board_cell_22`

**Кнопки:**
- `btn_start` — кнопка "Начать игру" (Idle).
- `btn_continue` — кнопка "Продолжить" (GameOver).
- `btn_new_game` — кнопка "Новая игра" (Playing/GameOver).
