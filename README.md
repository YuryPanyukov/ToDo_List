# Список дел 📝

Android-приложение для планирования задач с интерактивным календарём и виджетом:
просматривайте дела в режиме месяца или недели, добавляйте задачи с приоритетами
и временем, отмечайте выполнение прямо с рабочего стола — всё в тёплой палитре
Muted Rose / Sage / Warm Sand.

## Возможности

### Календарь и дела

- **Режимы просмотра** — переключение между сеткой месяца и компактной полосой
  недели; навигация стрелками по месяцам.
- **Детальная панель дня** — выбранный день раскрывается панелью с активными
  и выполненными делами; можно скрыть выполненные.
- **Приоритеты** — три уровня (Низкий / Средний / Высокий) с цветовой кодировкой:
  Sage, Sand, Rose; индикатор приоритета отображается в карточке дела и в виджете.
- **Время** — каждое дело может быть привязано ко времени (`LocalTime`);
  время хранится в UTC-миллисекундах и конвертируется в локальную зону.
- **Сортировка** — порядок дел определяется полем `sortOrder` (UI-перетаскивание
  в планах).
- **Редактор дела** — `ModalBottomSheet` с полями заголовок, описание, дата,
  время и приоритет; сохранение и отмена.
- **Управление делами** — отметка выполнения одним тапом, перенос на следующий
  день, удаление, добавление нового дела через FAB.

### Виджет

- **Интерактивный Glance-виджет 4×2** — показывает дела выбранного дня, позволяет
  отмечать выполнение прямо с домашнего экрана и листать дни стрелками.
- **Счётчик невыполненных** — отображает «Осталось: N» или «Всё выполнено 🎉».
- **Свободные дни** — пустые дни показывают «Свободный день 🌿».
- **Скрытие выполненных** — переключатель прямо в виджете.
- **Добавление дела** — кнопка «＋ Дело» открывает приложение.
- **Максимум 6 задач** на виджете — остальные скрываются.

### Персонализация

- **Палитра** — тёплая дизайн-система (Muted Rose / Sage / Warm Sand);
  приоритеты и акцентные элементы выдержаны в единой гамме.
- **Типографика** — системные `Serif` / `SansSerif` (в планах: `Playfair Display`
  + `Nunito Sans`).

## Стек технологий

| Компонент | Технология |
|---|---|
| Язык | Kotlin 11 |
| UI | Jetpack Compose + Material 3 (Compose BOM) |
| Навигация | один экран (`CalendarScreen`), в планах — Navigation Compose |
| ViewModel | Lifecycle ViewModel + Compose |
| Хранилище | Room (KSP) + `java.time` (desugaring) |
| Виджет | Jetpack Glance (AppWidget) |
| Асинхронность | Kotlin Coroutines |
| Минимальная версия Android | 7.0 (API 24) |
| Целевая версия Android | API 37 |
| `applicationId` | `code_SyS.todo_list` |
| `versionName` | `"1.0"` |

## Архитектура

MVVM с разделением на слои (все имена пакетов — как в исходниках):

```
app/src/main/java/code_SyS/todo_list/
├── MainActivity.kt            # точка входа
├── data/                      # модели и хранилища
│   ├── TodoApp.kt             # Application + глобальный repository
│   ├── TaskRepository.kt      # CRUD-операции над задачами
│   └── local/
│       ├── TodoDatabase.kt    # Room база данных
│       ├── TaskDao.kt         # DAO: выборка по дате, insert, update, delete
│       └── TaskEntity.kt      # Room-сущность (id, title, description, dateMillis, timeMillis, isDone, priority, sortOrder)
├── domain/                    # чистая логика, без Android
│   ├── Task.kt                # доменная модель + конвертация из Entity
│   └── Priority.kt            # приоритеты с цветом и меткой
├── widget/                    # Glance-виджет
│   ├── TodoWidget.kt          # GlanceAppWidget + UI-контент
│   ├── WidgetActions.kt       # действия виджета (toggle, shift date, toggle hide)
│   └── WidgetPrefs.kt         # состояние виджета (GlanceStateDefinition)
└── ui/
    ├── calendar/
    │   ├── CalendarScreen.kt      # главный экран: календарь + панель дня
    │   ├── CalendarViewModel.kt   # состояние, события, сохранение задач
    │   ├── CalendarState.kt       # UI-состояние и режимы (MONTH/WEEK)
    │   └── components/
    │       ├── CalendarGrid.kt    # MonthGrid + WeekStrip + WeekdayHeader
    │       ├── DayPanel.kt        # панель дня: активные/выполненные, переключатель
    │       ├── TaskCard.kt        # карточка задачи с приоритетом
    │       └── TaskEditorSheet.kt # редактор дела (ModalBottomSheet)
    └── theme/
        ├── Color.kt               # палитра Muted Rose / Sage / Warm Sand
        ├── Type.kt                # типографика
        └── Theme.kt               # Material3 theme + customShapes
```

### Ключевая логика

`TaskRepository` работает с Room-базой: `getTasksForDateBlocking()` делает
блокирующий выборку (для виджета), `getTasksForDate()` возвращает `Flow`.
`CalendarViewModel` собирает UI-состояние в `StateFlow<CalendarState>`,
обрабатывает события (`CalendarEvent`) и управляет редактором (`StateFlow<TaskEditor?>`).

Виджет читает состояние через `GlanceStateDefinition` (выбирает дату, режим
«скрыть выполненные» для конкретного экземпляра виджета) и выполняет действия
через `GlanceActionCallback`: `ToggleTaskAction`, `ShiftDateAction`,
`ToggleHideDoneAction`.

### Хранение данных

- `TaskEntity` хранит дату и время в миллисекундах UTC; конвертация в
  `LocalDate` / `LocalTime` происходит при загрузке через `Instant` и
  системную зону.
- `sortOrder` — `Float`, предназначен для будущей Drag & Drop сортировки.
- Desugaring `java.time` включён (`coreLibraryDesugaring`) для корректной
  работы `LocalDate`/`Instant`/`ZoneId` на minSdk 24.

## Планы развития

Актуальный бэклог — в [TODO.md](TODO.md).

Краткий обзор незакрытых направлений:

- **Дизайн** — реальные шрифты, Dynamic Color (Material You), анимации,
  адаптивные иконки.
- **Архитектура** — multi-module, DI (Koin/Hilt), UseCases.
- **Экраны** — Navigation Compose, экран настроек, режим DAY, FAB с анимацией.
- **Календарь** — Drag & Drop, перенос на произвольную дату, подзадачи,
  напоминания.
- **Виджет** — размер 4×4, обновление через WorkManager, автотесты.
- **Ретеншен** — утренний бриф, вечерний рефлекс, теги-эмоции.
- **Релиз** — CI/CD, Play Console, скриншоты.
