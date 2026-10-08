# Список дел 📝

Android-приложение для планирования задач с интерактивным календарём и виджетом:
просматривайте дела в режиме месяца или недели, добавляйте задачи с приоритетами
и временем, отмечайте выполнение прямо с рабочего стола — всё в тёплой палитре
Muted Rose / Sage / Warm Sand.

## Возможности

### Первый запуск

- **Нативный splash** — через `androidx.core:core-splashscreen`: системный экран
  запуска с тем же логотипом, что у стартового экрана
  (`drawable/ic_splash_logo.xml`: круг Muted Rose + галочка, в тёмной теме —
  палитра DarkColors), показывается ещё до первого кадра Compose — Android 12+
  и backport до API 24.
- **Стартовый экран** — логотип с spring-анимацией, тап пропускает ожидание,
  дальше автопереход на календарь.
- **Онбординг** — пять страниц о календаре, делах, приоритетах, выполненных
  и виджете; показывается один раз, вернуться можно из настроек.
- **Настройки** — экран из шапки календаря: повтор знакомства и версия приложения.

### Календарь и дела

- **Режимы просмотра** — переключение между сеткой месяца и компактной полосой
  недели; навигация стрелками по месяцам.
- **Экран дня** — тап по дню в сетке месяца или полосе недели открывает
  отдельный экран дня (`day_detail/{date}`) с активными и выполненными делами;
  выполненные скрываются переключателем в шапке. Число дня «перелетает» из
  календаря в шапку экрана (Shared Element Transition).
- **Приоритеты** — три уровня (Низкий / Средний / Высокий) с цветовой кодировкой:
  Sage, Sand, Rose; индикатор приоритета отображается в карточке дела и в виджете.
- **Время** — каждое дело может быть привязано ко времени (`LocalTime`);
  время хранится в UTC-миллисекундах и конвертируется в локальную зону.
- **Сортировка** — долгим нажатием по делу на экране дня его можно
  перетащить на новое место; порядок сохраняется в поле `sortOrder`
  и подхватывается календарём и виджетом.
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
- **Типографика** — `Playfair Display` (заголовки) + `Nunito Sans` (текст),
  статические инстансы в `res/font` генерируются `tools/generate_fonts.py`.
- **Dynamic Color** — Material You палитра на Android 12+, на старых версиях —
  фирменная гамма Muted Rose.
- **Анимации** — Shared Element Transition «день → экран дня», каскадное
  появление списка дел, пружины для FAB и модалок (`ui/theme/Motion.kt`).

## Стек технологий

| Компонент | Технология |
|---|---|
| Язык | Kotlin 2.2.10 (JVM target 11) |
| UI | Jetpack Compose + Material 3 (Compose BOM 2026.02.01) |
| Навигация | Navigation Compose (маршруты `calendar`, `settings`, `day_detail/{date}`) |
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
│   ├── TodoApp.kt             # Application + глобальный repository + prefs
│   ├── AppPrefs.kt            # SharedPreferences: флаг онбординга
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
│   ├── WidgetActions.kt       # действия виджета (toggle, shift date, reset date, toggle hide)
│   └── WidgetPrefs.kt         # состояние виджета (GlanceStateDefinition)
└── ui/
    ├── AppRoot.kt                 # стадии START → ONBOARDING? → MAIN (Crossfade)
    ├── start/StartScreen.kt       # стартовый экран (splash)
    ├── onboarding/OnboardingScreen.kt  # онбординг: пять страниц
    ├── navigation/AppNavHost.kt   # граф маршрутов + общий редактор дел
    ├── calendar/
    │   ├── CalendarScreen.kt      # главный экран: календарь + сводка дня
    │   ├── CalendarViewModel.kt   # состояние, события, сохранение задач
    │   ├── CalendarState.kt       # UI-состояние и режимы (MONTH/WEEK)
    │   └── components/
    │       ├── CalendarGrid.kt    # MonthGrid + WeekStrip + WeekdayHeader
    │       ├── TaskCard.kt        # карточка задачи с приоритетом
    │       └── TaskEditorSheet.kt # редактор дела (ModalBottomSheet)
    ├── daydetail/DayDetailScreen.kt # полный экран дня (day_detail/{date})
    ├── settings/SettingsScreen.kt # настройки: повтор онбординга, версия
    └── theme/
        ├── Color.kt               # палитра Muted Rose / Sage / Warm Sand
        ├── Type.kt                # типографика
        ├── Motion.kt              # пружины приложения (AppMotion)
        └── Theme.kt               # Material3 theme + customShapes
```

### Ключевая логика

`TaskRepository` работает с Room-базой: `getTasksForDateBlocking()` делает
блокирующую выборку (для виджета), `observeForDate()` / `observeBetween()`
возвращают `Flow`.
`CalendarViewModel` собирает UI-состояние в `StateFlow<CalendarState>`,
обрабатывает события (`CalendarEvent`) и управляет редактором (`StateFlow<TaskEditorState?>`).

Виджет читает состояние через `GlanceStateDefinition` (выбирает дату, режим
«скрыть выполненные» для конкретного экземпляра виджета) и выполняет действия
через `ActionCallback`: `ToggleTaskAction`, `ShiftDateAction`, `ResetDateAction`,
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

- **Дизайн** — реальные шрифты (`Playfair Display` / `Nunito Sans`), Dynamic Color
  (Material You), анимация «галочки» в виджете.
- **Архитектура** — multi-module, DI (Koin/Hilt), UseCases + интерфейс репозитория.
- **Экраны** — режим DAY, «Синхронизация» в меню.
- **Календарь** — подзадачи, напоминания по времени дела.
- **Виджет** — размер 4×4, обновление через WorkManager.
- **Ретеншен** — утренний бриф, вечерний рефлекс, теги-эмоции,
  виджет «Свободный слот».
- **Релиз** — CI/CD, Play Console, скриншоты, политика конфиденциальности.
