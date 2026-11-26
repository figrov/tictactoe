# Архитектура проекта (Single Source of Truth)

## 1. Стек
- Kotlin 2.0.21, Jetpack Compose (Material 3), Clean Architecture + MVI (`StateFlow` + `Channel`).
- Тесты: JUnit 5, MockK, Turbine, `StandardTestDispatcher`, Compose UI Test.

## 2. Структура пакетов (`com.example.game`)
- `domain/` — чистая игровая логика и State Machine
- `presentation/` — ViewModel, UiState, UiIntent, UiEffect
- `ui/` — Compose-экраны и компоненты

## 3. Реестр ключевых классов
- `MainActivity.kt` — точка входа.

## 4. Реестр семантических тегов (`Modifier.testTag`)
- Пока не добавлены.
