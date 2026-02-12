# H-Manhunt

Модифицированный плагин режима Manhunt для Paper.

Репозиторий проекта:  
`https://github.com/hetasshi/H-Manhunt`

## Что это
**H-Manhunt** — PvP/PvE-режим, где:
- **Охотники** преследуют спидраннеров с помощью компаса.
- **Спидраннеры** должны убить дракона Края до того, как их остановят.

## Совместимость
- Серверное ядро: **Paper 1.21+**
- Проверено на: **Paper 1.21.11 (build 99)**
- Java: **21**

Поддерживаемые типы ядер:
- **Paper**: поддерживается (основная целевая платформа).
- **Форки Paper** (например, Purpur/Pufferfish): обычно совместимы, но без гарантии.
- **Spigot/Bukkit**: официально не поддерживаются и совместимость не гарантируется.

## Основные возможности
- Роли охотников и спидраннеров с управлением через `/manhunt`.
- Трекинг через компас: ближайшая цель или конкретный спидраннер.
- Поддержка `trackPortals` (последние известные координаты по измерениям).
- Система паузы/возобновления матча через голосование.
- Казуальные способности охотников (`casual`), включая **Warp Shadows** (варп в сторону цели с кулдауном).

## Команды
- `/manhunt add <role> <player> <player> ...` — добавить игроков в роль.
- `/manhunt add <role> @a` — добавить всех онлайн-игроков в роль.
- `/manhunt remove <player> <player> ...` — удалить игроков из матча.
- `/manhunt remove @a` — удалить всех игроков из матча.
- `/manhunt start` — запустить матч.
- `/manhunt reset` — сбросить матч.
- `/manhunt pause` — поставить матч на паузу (если включено).
- `/manhunt unpause` — снять паузу (если включено).
- `/manhunt list` — показать список участников и ролей.
- `/manhunt rules <rule> [value]` — посмотреть/изменить значение правила.
- `/manhunt help` — справка по командам.

## Конфигурация (`config.yml`)
| Ключ | Тип | По умолчанию | Описание |
|---|---|---|---|
| `timeSetDayOnStart` | boolean | `true` | Установить день при старте матча. |
| `weatherClearOnStart` | boolean | `true` | Очистить погоду при старте матча. |
| `headStartDuration` | int | `0` | Фора спидраннерам в секундах. |
| `speedrunnersLives` | int | `1` | Количество жизней спидраннеров. |
| `spectatorAfterDeath` | boolean | `true` | Переводить погибших спидраннеров в spectator. |
| `teleport` | boolean | `true` | Телепортировать участников в одну точку перед стартом. |
| `trackPortals` | boolean | `true` | Отслеживать цель по последним координатам между измерениями. |
| `friendlyFire` | boolean | `true` | Разрешить урон по союзникам. |
| `compassMenu` | boolean | `false` | Выбор цели через GUI-меню. |
| `trackNearestMode` | boolean | `true` | Режим трекинга ближайшего спидраннера. |
| `clearInventories` | boolean | `true` | Очищать инвентари при старте. |
| `takeAwayOps` | boolean | `true` | Временно снимать OP во время матча. |
| `usePermissions` | boolean | `false` | Проверять права на команды. |
| `useBossBarRadar` | boolean | `false` | Включить BossBar-радар союзников. |
| `enablePauses` | boolean | `true` | Разрешить `/manhunt pause` и `/manhunt unpause`. |
| `casual` | boolean | `true` | Включить способности охотников (Shift + ПКМ по компасу). |
| `warpShadowsCooldown` | int | `300` | Кулдаун Warp Shadows в секундах. |
| `warpShadowsMaxDistance` | int | `200` | Дистанция, которая остается до цели после варпа. |
| `warpShadowsBufferZone` | int | `40` | Если охотник ближе этого порога к цели, варп не срабатывает. |

## Права доступа
Если `usePermissions: true`, используются следующие права:

| Permission | Описание |
|---|---|
| `manhunt.manhunt` | Доступ ко всем `/manhunt`-командам. |
| `manhunt.add` | Добавление игроков. |
| `manhunt.remove` | Удаление игроков. |
| `manhunt.start` | Запуск матча. |
| `manhunt.reset` | Сброс матча. |
| `manhunt.pause` | Пауза без голосования. |
| `manhunt.unpause` | Снятие паузы без голосования. |
| `manhunt.list` | Просмотр состава ролей. |
| `manhunt.rules` | Просмотр/изменение правил. |
| `manhunt.help` | Справка по командам. |

## Credits
- Оригинальный проект: Matistan / MinecraftManhunt  
  `https://github.com/Matistan/MinecraftManhunt`
