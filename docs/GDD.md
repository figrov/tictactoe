# Game Design Document (GDD) — TicTacToe

## 1. Концепт и Core Loop
- **Жанр:** Казуальная настольная головоломка (Tabletop / Puzzle).
- **Платформа:** Android (minSdk 26, Portrait, Jetpack Compose).
- **Core Loop:** Выбор режима -> Партия на поле -> Фиксация победы/ничьей -> Мгновенный рестарт.

## 2. Реестр фич

| ID | Фича | Спецификация | Статус |
| :--- | :--- | :--- | :--- |
| `FEAT-01` | Базовое игровое поле 3x3 (PvP на одном экране) | `docs/SPEC_core_gameplay.md` | `[Planned]` |
| `FEAT-02` | ИИ-соперник (3 уровня сложности) | `docs/SPEC_bot_ai.md` | `[Planned]` |
