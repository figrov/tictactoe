# Протокол командной работы (Team Harness: Семёныч, Петрович, Кузьмич)

Мы разрабатываем Android-игру (Kotlin, Jetpack Compose, MVI) силами трех специализированных агентов. Единый источник правды о проекте хранится в папке `docs/`.

## 1. Три файла общей памяти (Shared State)
Каждый агент перед началом работы обязан прочитать эти файлы, а после завершения — обновить свою зону ответственности:
1. `docs/GDD.md` — продуктовый реестр фич и правил игры (обновляет **Семёныч**).
2. `docs/ARCHITECTURE.md` — карта пакетов, ключевых классов, MVI-состояний и `testTag` (обновляет **Петрович**).
3. `docs/STATE.md` — текущий пульс разработки: активная фича, этап и счетчик итераций (обновляют **все по очереди**).

## 2. Конвейер одной фичи (Handoff Loop)
1. **Этап 1 — Аналитика (`game-analyst` / Семёныч):**
   - Читает `GDD.md`, `ARCHITECTURE.md`, `STATE.md`.
   - Создает `docs/SPEC_<feature>.md`, обновляет `GDD.md`, перезаписывает `docs/reports/semyonich.md`.
   - Ставит в `STATE.md` статус: `READY_FOR_DEV` (следующий: **Петрович**).
2. **Этап 2 — Разработка (`android-dev` / Петрович):**
   - Читает `STATE.md`, `ARCHITECTURE.md`, `SPEC_<feature>.md` (и `docs/qa/PROTOCOL_<feature>.md`, если это возврат с QA).
   - Пишет код в `app/src/main/`, проверяет компиляцию, обновляет карту классов и `testTag` в `ARCHITECTURE.md`, перезаписывает `docs/reports/petrovich.md`.
   - Ставит в `STATE.md` статус: `READY_FOR_QA` (следующий: **Кузьмич**).
3. **Этап 3 — Тестирование (`qa-engineer` / Кузьмич):**
   - Читает `STATE.md`, `SPEC_<feature>.md`, `ARCHITECTURE.md`.
   - Пишет Unit- и Compose UI-тесты, запускает `testDebugUnitTest`, оформляет `docs/qa/PROTOCOL_<feature>.md`, перезаписывает `docs/reports/kuzmich.md`.
   - Если 0 `Blocker`/`Critical` багов -> ставит в `STATE.md` статус `APPROVED`.
   - Если найдены дефекты -> увеличивает `QA_ATTEMPT` в `STATE.md` и ставит статус `REJECTED` (возврат **Петровичу**).

## 3. Защита от бесконечного цикла (Circuit Breaker)
- Максимум **3 круга исправлений** (`QA_ATTEMPT <= 3`) на одну фичу. При превышении — статус `ESCALATED_TO_USER`.
