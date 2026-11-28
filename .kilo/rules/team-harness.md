# Протокол командной работы (Team Harness v2.1: Тимлид, Семёныч, Петрович, Бондарев, Кузьмич)

Мы разрабатываем Android-игру (Kotlin, Jetpack Compose, MVI) силами четырех специализированных агентов под управлением Тимлида-оркестратора (`delivarable-man`). Единый источник правды о проекте хранится в папке `docs/`.

## 1. Три файла общей памяти (Shared State)
1. `docs/GDD.md` — продуктовый реестр фич и правил игры (обновляет **Семёныч**).
2. `docs/ARCHITECTURE.md` — карта пакетов, ключевых классов, MVI-состояний и `testTag` (обновляет **Петрович**).
3. `docs/STATE.md` — текущий пульс разработки: активная фича, этап и счетчик итераций (обновляют **все по очереди**).

## 2. Правило мгновенной доставки отчётов в Telegram (через Git)
Каждый участник после завершения своего шага перезаписывает свой файл отчёта в `docs/reports/` и **немедленно отправляет его в текущую ветку Git** (чтобы сработал CI `telegram-notify.yml`), **строго ограничивая коммит только папками отчётов и протоколов**:
- Команда для агентов с доступом к `bash` (**Петрович**, **Бондарев**, **Кузьмич** на промежуточном этапе):
  `git add docs/reports/ docs/qa/ && git commit -m "docs(report): update agent report" && git push origin HEAD`
- Для агентов без доступа к `bash` (**Тимлид `delivarable-man`** и **Семёныч**): после записи своего отчёта Тимлид вызывает **Бондарева (`build-controller`)** с короткой сервисной задачей: *«Отправь в Git только обновлённые отчёты из `docs/reports/` и `docs/qa/` (ничего лишнего не добавлять)»*.

## 3. Конвейер одной фичи (Handoff Loop)
1. **Старт — Тимлид (`delivarable-man`):**
   - Записывает вводный отчёт в `docs/reports/timlead.md`, просит **Бондарева** запушить его в Git (`docs/reports/`), затем передаёт задачу **Семёнычу**.
2. **Этап 1 — Аналитика (`game-analyst` / Семёныч):**
   - Читает `GDD.md`, `ARCHITECTURE.md`, `STATE.md`.
   - Создает `docs/SPEC_<feature>.md` (с четкими `AC-*` и едиными `testTag`), обновляет `GDD.md`, перезаписывает `docs/reports/semyonich.md`.
   - Ставит в `STATE.md` статус: `READY_FOR_DEV` (следующий: **Петрович**).
   - Тимлид просит **Бондарева** запушить `docs/reports/semyonich.md` в Git.
3. **Этап 2 — Разработка (`android-dev` / Петрович):**
   - Читает `STATE.md`, `ARCHITECTURE.md`, `SPEC_<feature>.md` (и разбор от Бондарева или протокол Кузьмича при возврате).
   - Пишет код в `app/src/main/`, гарантируя контрастную сетку 3×3, кликабельность всех клеток, отсутствие перекрытия кликов слоем `WinLine`, русскую локализацию UI и обновление `board` и `gameState` в `ViewModel` + `SavedStateHandle`.
   - Проверяет компиляцию `./gradlew compileDebugKotlin --console=plain -q`, обновляет `ARCHITECTURE.md`, перезаписывает `docs/reports/petrovich.md`.
   - Ставит в `STATE.md` статус: `READY_FOR_BUILD_CHECK` (следующий: **Бондарев**).
   - Сам пушит в Git **только свой отчёт**: `git add docs/reports/petrovich.md && git commit -m "docs(report): Petrovich status update" && git push origin HEAD`.
4. **Этап 3 — Технический контроль и Сборка (`build-controller` / Бондарев):**
   - Читает `STATE.md`, `SPEC_<feature>.md`, `ARCHITECTURE.md` и проверяет ключевые UI-файлы (`TicTacToeScreen.kt`, `Cell.kt`, `WinLine.kt`, `ScoreDisplay.kt`, `TicTacToeViewModel.kt`).
   - Запускает полную проверку сборки и компиляции всех тестов + сборку APK:
     `./gradlew compileDebugKotlin compileDebugUnitTestKotlin compileDebugAndroidTestKotlin assembleDebug --console=plain`
   - Если сборка упала или в UI есть дефекты -> пишет разбор в `docs/reports/bondarev.md`, ставит статус `BUILD_FAILED` (возврат **Петровичу**) и пушит `docs/reports/bondarev.md` в Git.
   - Если сборка APK и компиляция всех тестов 100% успешны -> пишет `docs/reports/bondarev.md`, ставит статус `READY_FOR_QA` (следующий: **Кузьмич**) и пушит `docs/reports/bondarev.md` в Git.
5. **Этап 4 — Тестирование и Автопилот-релиз (`qa-engineer` / Кузьмич):**
   - Читает `STATE.md`, `SPEC_<feature>.md`, `ARCHITECTURE.md`, `docs/reports/bondarev.md`.
   - Пишет и запускает тесты: `./gradlew testDebugUnitTest compileDebugAndroidTestKotlin --console=plain`.
   - **Если найден баг в `src/main/`:** оформляет `docs/qa/PROTOCOL_<feature>.md`, перезаписывает `docs/reports/kuzmich.md`, увеличивает `QA_ATTEMPT` и ставит `REJECTED` (возврат **Петровичу**), затем пушит в Git **только отчёт и протокол** (`git add docs/reports/ docs/qa/ && git commit -m "docs(qa): Kuzmich bug report" && git push origin HEAD`).
   - **Если 100% тестов зелёные и 0 багов (Автопилот-релиз):**
     1. Оформляет `docs/qa/PROTOCOL_<feature>.md`, перезаписывает `docs/reports/kuzmich.md`, ставит в `STATE.md` статус `APPROVED`.
     2. Если текущая ветка НЕ `main`, выполняет полный релизный пуш:
        `git add . && git commit -m "feat(game): complete <feature> with 100% passing tests and APK" && git push -u origin HEAD`
     3. Если установлен `gh`, создаёт PR командой `gh pr create --base main --fill`.
6. **Финал — Тимлид (`delivarable-man`):**
   - Записывает итоговую сводку в `docs/reports/timlead.md` и просит **Бондарева** запушить `docs/reports/timlead.md` в Git.

## 4. Защита от бесконечного цикла (Circuit Breaker)
- Максимум **3 круга исправлений** (`QA_ATTEMPT <= 3`) после QA -> далее `ESCALATED_TO_USER`.