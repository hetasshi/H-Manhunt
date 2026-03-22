<div align="center">

<img src=".github/assets/hmanhunt.png" alt="H-Manhunt Banner" width="700">

<img src=".github/assets/separator.svg" alt="" width="500">

# H-Manhunt 🏹

> Модифицированный и прокачанный плагин режима Manhunt для самых современных версий Minecraft.

<img src=".github/assets/separator.svg" alt="" width="500">

[![GitHub Repo](https://img.shields.io/badge/github-H--Manhunt-blue?logo=github)](https://github.com/hetasshi/H-Manhunt)
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-blue?logo=data:image/svg%2Bxml;base64,PHN2ZyByb2xlPSJpbWciIHZpZXdCb3g9IjAgMCAyNCAyNCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cGF0aCBmaWxsPSJ3aGl0ZSIgZD0iTTExLjk0NCAwQTEyIDEyIDAgMCAwIDAgMTJhMTIgMTIgMCAwIDAgMTIgMTIgMTIgMTIgMCAwIDAgMTItMTJBMTIgMTIgMCAwIDAgMTIgMGExMiAxMiAwIDAgMC0uMDU2IDB6bTQuOTYyIDcuMjI0Yy4xLS4wMDIuMzIxLjAyMy40NjUuMTRhLjUwNi41MDYgMCAwIDEgLjE3MS4zMjVjLjAxNi4wOTMuMDM2LjMwNi4wMi40NzItLjE4IDEuODk4LS45NjIgNi41MDItMS4zNiA4LjYyNy0uMTY4LjktLjQ5OSAxLjIwMS0uODIgMS4yMy0uNjk2LjA2NS0xLjIyNS0uNDYtMS45LS45MDItMS4wNTYtLjY5My0xLjY1My0xLjEyNC0yLjY3OC0xLjgtMS4xODUtLjc4LS40MTctMS4yMS4yNTgtMS45MS4xNzctLjE4NCAzLjI0Ny0yLjk3NyAzLjMwNy0zLjIzLjAwNy0uMDMyLjAxNC0uMTUtLjA1Ni0uMjEycy0uMTc0LS4wNDEtLjI0OS0uMDI0Yy0uMTA2LjAyNC0xLjc5MyAxLjE0LTUuMDYxIDMuMzQ1LS40OC4zMy0uOTEzLjQ5LTEuMzAyLjQ4LS40MjgtLjAwOC0xLjI1Mi0uMjQxLTEuODY1LS40NC0uNzUyLS4yNDUtMS4zNDktLjM3NC0xLjI5Ny0uNzg5LjAyNy0uMjE2LjMyNS0uNDM3Ljg5My0uNjYzIDMuNDk4LTEuNTI0IDUuODMtMi41MjkgNi45OTgtMy4wMTQgMy4zMzItMS4zODYgNC4wMjUtMS42MjcgNC40NzYtMS42MzV6Ii8+PC9zdmc+)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=data:image/svg%2Bxml;base64,PHN2ZyByb2xlPSJpbWciIHZpZXdCb3g9IjAgMCAyNCAyNCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cGF0aCBmaWxsPSJ3aGl0ZSIgZD0iTTguODUxIDE4LjU2cy0uOTE3LjUzNC42NTMuNzE0YzEuOTAyLjIxOCAyLjg3NC4xODcgNC45NjktLjIxMSAwIDAgLjU1Mi4zNDYgMS4zMjEuNjQ2LTQuNjk5IDIuMDEzLTEwLjYzMy0uMTE4LTYuOTQzLTEuMTQ5TTguMjc2IDE1LjkzM3MtMS4wMjguNzYxLjU0Mi45MjRjMi4wMzIuMjA5IDMuNjM2LjIyNyA2LjQxMy0uMzA4IDAgMCAuMzg0LjM4OS45ODcuNjAyLTUuNjc5IDEuNjYxLTEyLjAwNy4xMy03Ljk0Mi0xLjIxOE0xMy4xMTYgMTEuNDc1YzEuMTU4IDEuMzMzLS4zMDQgMi41MzMtLjMwNCAyLjUzM3MyLjkzOS0xLjUxOCAxLjU4OS0zLjQxOGMtMS4yNjEtMS43NzItMi4yMjgtMi42NTIgMy4wMDctNS42ODggMCAwLTguMjE2IDIuMDUxLTQuMjkyIDYuNTczTTE5LjMzIDIwLjUwNHMuNjc5LjU1OS0uNzQ3Ljk5MWMtMi43MTIuODIyLTExLjI4OCAxLjA2OS0xMy42NjkuMDMzLS44NTYtLjM3My43NS0uODkgMS4yNTQtLjk5OC41MjctLjExNC44MjgtLjA5My44MjgtLjA5My0uOTUzLS42NzEtNi4xNTYgMS4zMTctMi42NDMgMS44ODcgOS41OCAxLjU1MyAxNy40NjItLjcgMTQuOTc3LTEuODJNOS4yOTIgMTMuMjFzLTQuMzYyIDEuMDM2LTEuNTQ0IDEuNDEyYzEuMTg5LjE1OSAzLjU2MS4xMjMgNS43Ny0uMDYyIDEuODA2LS4xNTIgMy42MTgtLjQ3NyAzLjYxOC0uNDc3cy0uNjM3LjI3Mi0xLjA5OC41ODdjLTQuNDI5IDEuMTY1LTEyLjk4Ni42MjMtMTAuNTIyLS41NjggMi4wODItMS4wMDYgMy43NzYtLjg5MiAzLjc3Ni0uODkyTTE3LjExNiAxNy41ODRjNC41MDMtMi4zNCAyLjQyMS00LjU4OS45NjgtNC4yODUtLjM1NS4wNzQtLjUxNS4xMzgtLjUxNS4xMzhzLjEzMi0uMjA3LjM4NS0uMjk3YzIuODc1LTEuMDExIDUuMDg2IDIuOTgxLS45MjggNC41NjIgMCAwIC4wNy0uMDYyLjA5LS4xMThNMTQuNDAxIDBzMi40OTQgMi40OTQtMi4zNjUgNi4zM2MtMy44OTYgMy4wNzctLjg4OSA0LjgzMiAwIDYuODM2LTIuMjc0LTIuMDUzLTMuOTQzLTMuODU4LTIuODI0LTUuNTM5IDEuNjQ0LTIuNDY5IDYuMTk3LTMuNjY1IDUuMTg5LTcuNjI3TTkuNzM0IDIzLjkyNGM0LjMyMi4yNzcgMTAuOTU5LS4xNTMgMTEuMTE2LTIuMTk4IDAgMC0uMzAyLjc3NS0zLjU3MiAxLjM5MS0zLjY4OC42OTQtOC4yMzkuNjEzLTEwLjkzNy4xNjggMCAwIC41NTMuNDU3IDMuMzkzLjYzOSIvPjwvc3ZnPg==)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

</div>

---

## Что такое Manhunt?

> [!TIP]
> **Manhunt** (Охота на игрока) — это популярный игровой режим в Minecraft, доведенный до идеала знаменитым ютубером **Dream** в его серии роликов *"Minecraft Speedrunner VS Hunter"*.
>
> **Суть режима:**
> *   **Спидраннер:** Один (или несколько) игрок должен как можно быстрее убить Дракона Края. Его задача — выжить и завершить игру.
> *   **Охотники:** Группа игроков, чья задача — помешать спидраннеру и убить его до того, как падет Дракон. Охотники имеют бесконечные жизни и **Компас**, который всегда указывает на местоположение спидраннера.

> [!NOTE]
> **H-Manhunt** — это модифицированный плагин, который позволяет воссоздать легендарный режим и поиграть вместе с друзьями на серверах Paper (1.21+). Он не только реализует классическую охоту, но и добавляет уникальные механики, способные слегка изменить и освежить привычный игровой процесс.

---

## 📦 Совместимость

| | Статус |
|---|---|
| Серверное ядро | **Paper 1.21+** |
| Проверено на | **Paper 1.21.11 (build 99)** |
| Java | **21** |

| Ядро | Поддержка |
|---|---|
| **Paper** | Основная платформа |
| **Форки Paper** (Purpur, Pufferfish) | Обычно совместимы, без гарантии |
| **Spigot / Bukkit** | Не поддерживаются |

---

## 📋 Основные возможности

*   **⚡ Быстрое управление:** Управление ролями охотников и спидраннеров через интуитивно понятные команды `/manhunt`.
*   **🎯 Продвинутый трекинг:** Компас поддерживает режимы отслеживания ближайшей цели или конкретного выбранного игрока.
*   **🌌 Межпространственная охота:** Поддержка `trackPortals` — компас не сбивается при переходе цели в Незер или Энд, указывая на последний портал.
*   **⏸ Система пауз:** Возможность поставить матч на паузу через голосование — полезно при технических шоколадках или необходимости перерыва.
*   **🧙 Способности (Casual Mode):** Уникальные навыки для охотников, такие как **Warp Shadows** — быстрый варп в сторону цели для поддержания динамики игры.

---

## 💬 Команды

| Команда | Описание |
|---|---|
| `/manhunt add <role> <player> ...` | Добавить игроков в роль |
| `/manhunt add <role> @a` | Добавить всех онлайн-игроков |
| `/manhunt remove <player> ...` | Удалить игроков из матча |
| `/manhunt remove @a` | Удалить всех из матча |
| `/manhunt start` | Запустить матч |
| `/manhunt reset` | Сбросить матч |
| `/manhunt pause` | Поставить на паузу |
| `/manhunt unpause` | Снять паузу |
| `/manhunt list` | Список участников и ролей |
| `/manhunt rules <rule> [value]` | Посмотреть/изменить правило |
| `/manhunt help` | Справка |

---

## ⚙️ Конфигурация (`config.yml`)

| Ключ | Тип | По умолчанию | Описание |
|---|---|---|---|
| `timeSetDayOnStart` | boolean | `true` | Установить день при старте матча |
| `weatherClearOnStart` | boolean | `true` | Очистить погоду при старте матча |
| `headStartDuration` | int | `0` | Фора спидраннерам (секунды) |
| `speedrunnersLives` | int | `1` | Количество жизней спидраннеров |
| `spectatorAfterDeath` | boolean | `true` | Переводить погибших в spectator |
| `teleport` | boolean | `true` | Телепорт участников в одну точку перед стартом |
| `trackPortals` | boolean | `true` | Отслеживать цель между измерениями |
| `friendlyFire` | boolean | `true` | Урон по союзникам |
| `compassMenu` | boolean | `false` | Выбор цели через GUI-меню |
| `trackNearestMode` | boolean | `true` | Режим трекинга ближайшего спидраннера |
| `clearInventories` | boolean | `true` | Очищать инвентари при старте |
| `takeAwayOps` | boolean | `true` | Временно снимать OP во время матча |
| `usePermissions` | boolean | `false` | Проверять права на команды |
| `useBossBarRadar` | boolean | `false` | BossBar-радар союзников |
| `enablePauses` | boolean | `true` | Разрешить паузу/возобновление |
| `matchWorlds.enabled` | boolean | `false` | Включить автоматическое создание отдельного матч-мира |
| `matchWorlds.autoGenerate.worldPrefix` | string | `manhunt_match_` | Префикс имени для новых миров |
| `matchWorlds.autoGenerate.keepLatestWorlds` | int | `2` | Сколько последних автосозданных миров хранить |
| `matchWorlds.autoGenerate.maxAttempts` | int | `4` | Сколько сидов пробовать при подборе мира |
| `matchWorlds.autoGenerate.minScoreToAccept` | int | `90` | Порог качества мира для мгновенного принятия |
| `matchWorlds.autoGenerate.acceptBestCandidateIfThresholdMissed` | boolean | `true` | Брать лучший мир, если идеальный не найден |
| `matchWorlds.autoGenerate.fixedSeeds` | list | `[]` | Список ручных сидов вместо случайных |
| `matchWorlds.autoGenerate.startDistanceFromAnchor` | int | `140` | На каком расстоянии от ключевой структуры ставить старт |
| `casual` | boolean | `true` | Способности охотников (Shift + ПКМ по компасу) |
| `warpShadowsCooldown` | int | `300` | Кулдаун Warp Shadows (секунды) |
| `warpShadowsMaxDistance` | int | `200` | Дистанция до цели после варпа |
| `warpShadowsBufferZone` | int | `40` | Порог, ближе которого варп не срабатывает |

### 🌍 Автоматическая генерация матч-мира

`H-Manhunt` теперь можно перевести в режим полной автоматизации старта матча. Тогда `/manhunt start` не использует текущий мир сервера, а сам:

*   создаёт новый overworld через Paper `WorldCreator`;
*   пробует несколько сидов;
*   оценивает каждый мир по эвристике около спавна;
*   выбирает лучший вариант и переносит туда игроков.
*   ставит старт не прямо в структуру, а на дистанции от неё.

Оценка мира строится вокруг того, что обычно ускоряет и оживляет ранний Manhunt:

*   хороший стартовый биом;
*   близкая деревня;
*   разрушенный портал;
*   корабль;
*   наличие дерева;
*   выразительный рельеф.

Это уже ближе к твоей идее “турбо-манханта”, чем просто multiworld-плагин: мир подбирается под темп забега, а не просто создаётся случайно.

Важно: это полностью собственная логика `H-Manhunt` без зависимости от сторонних multiworld-плагинов.

Дополнительно:

*   стартовая точка подбирается относительно лучшей найденной структуры;
*   спидраннер получает короткую directional-подсказку о том, где находится ближайшая полезная структура.

### 🌀 Как работает Warp Shadows?

**Warp Shadows (Варп тени)** — способность охотника, которая позволяет совершить направленный телепорт в сторону спидраннера. Она нужна для того, чтобы сократить дистанцию, когда цель слишком далеко убежала и погоня теряет смысл.

Охотник не появляется вплотную к цели — он приземляется на расстоянии `warpShadowsMaxDistance` блоков и должен продолжить преследование сам. Если охотник уже достаточно близко (ближе чем `maxDistance + bufferZone`), варп не сработает — способность не даёт нечестного преимущества в ближнем бою.

Активация: **Shift + ПКМ** по компасу охотника → меню способностей → Варп тени.

Если спидраннер ушёл в другое измерение (Незер или Энд), а `trackPortals: true` — варп работает аналогично: способность использует последние сохранённые координаты цели в мире охотника и телепортирует к точке последнего портала. Это позволяет не терять след даже при межпространственных переходах.

<div align="center">

<img src=".github/assets/last.jpeg" alt="Warp Shadows — визуальное объяснение механики телепорта" width="700">

> *Схема работы способности Warp Shadows*

</div>

Если спидраннер спустился в шахту, а охотник стоит на поверхности — вектор телепорта пойдёт по диагонали вниз, прямо через камень. Расчётная точка варпа окажется внутри породы, но алгоритм `findSafeLocation` автоматически просканирует вверх и вытолкнет охотника на ближайшую позицию с двумя блоками воздуха — на поверхность или в саму шахту, если там найдётся свободное место.

<div align="center">

<img src=".github/assets/les2.jpeg" alt="Warp Shadows — кейс когда цель в шахте под землёй" width="700">

> *Варп когда спидраннер в шахте: вектор уходит под землю, охотник приземляется на поверхности*

</div>

---

## 🔐 Права доступа

> Работают только при `usePermissions: true`

| Permission | Описание |
|---|---|
| `manhunt.manhunt` | Доступ ко всем командам |
| `manhunt.add` | Добавление игроков |
| `manhunt.remove` | Удаление игроков |
| `manhunt.start` | Запуск матча |
| `manhunt.reset` | Сброс матча |
| `manhunt.pause` | Пауза без голосования |
| `manhunt.unpause` | Снятие паузы без голосования |
| `manhunt.list` | Просмотр состава |
| `manhunt.rules` | Просмотр/изменение правил |
| `manhunt.help` | Справка |

---

## 🤝 Credits

Оригинальный проект: [Matistan / MinecraftManhunt](https://github.com/Matistan/MinecraftManhunt)
