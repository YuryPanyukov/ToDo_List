# 📱 Архитектура и дизайн приложения «Список дел» (Kotlin/Android)

Отличный проект! Фокус на **виджете с полной интерактивностью** — это сложная, но очень ценная фишка (Large部分的 Android-виджеты ограничены `RemoteViews`, но с Android 12+ и `Glance` можно сделать настоящую магию).

Ниже — комплексное решение: от архитектуры до дизайн-системы и кодовых заготовок.

---

## 1. 🏗 Технологический стек (Modern Android Development)

| Слой | Технология | Почему |
| :--- | :--- | :--- |
| **UI** | **Jetpack Compose** + **Material 3** | Декларативный UI, легкая анимация, нативная поддержка Glance. |
| **Виджеты** | **Jetpack Glance (Compose для RemoteViews)** | Единственный способ сделать *интерактивный* виджет (клики, ввод текста, чекбоксы) на современном Android. |
| **Архитектура** | **MVI (Unidirectional Data Flow)** + **Clean Architecture** | Предсказуемое состояние, легкое тестирование, разделение UI и логики. |
| **DI** | **Koin** или **Hilt** | Koin легче и чисто на Kotlin, Hilt — стандарт 엔терпрайз. |
| **БД** | **Room** + **Kotlin Flow** | Реактивные данные, миграции, поддержка Paging. |
| **Асинхронность** | **Kotlin Coroutines** + **Flow** | Структурированная конкурентность. |
| **Навигация** | **Navigation Compose** | Type-safe routes, бэк-стек. |
| **Календарь** | **Compose Calendar View** (кастомный или библиотека `compose-calendar`) | Готовые решения часто тяжелые; лучше написать свой ленивый `LazyColumn`/`LazyGrid` для месяца/недели. |
| **Дизайн-система** | **Material 3 Theming** (Dynamic Color) + **Custom Theme** | Поддержка Material You, кастомные палитры "под женскую аудиторию". |

---

## 2. 🎨 Дизайн-система: «Легкость, Воздух, Тепло»

### Концепция: **«Мягкий планировщик» (Soft Planner)**
Отказ от агрессивных розовых стереотипов. Век на **пастельные натуральные тона**, **типографику с характером**, **micro-interactions** и **белое пространство**.

<details>
<summary>🎨 <strong>Детальная палитра и Типографика (нажми, чтобы развернуть)</strong></summary>

### Цветовая схема (Material 3 Dynamic Color Ready)
*Базируется на `ColorScheme` M3. Поддерживает Light/Dark и системные обои (Monet).*

| Роль | Light (Hex) | Dark (Hex) | Назначение |
| :--- | :--- | :--- | :--- |
| **Primary** | `#C86B85` (Muted Rose) | `#E8A8C0` | Основной акцент (FAB, активные чекбоксы, заголовки дней). |
| **Secondary** | `#A8C5BA` (Sage Green) | `#8AB3A0` | Вторичные действия (кнопка "Перенести", теги). |
| **Tertiary** | `#F4D39E` (Warm Sand) | `#D4B88A` | Акценты на карточках дел, чипы приоритета. |
| **Surface** | `#FFFEF7` (Cream White) | `#1E1D1B` | Фон экранов, карточек. **Не чистый белый/черный!** |
| **Surface Variant** | `#F5F0EB` | `#2C2B28` | Фон виджетов, полей ввода, разделители. |
| **Outline** | `#D8CFC7` | `#4A4945` | Границы карточек, неактивные иконки. |
| **On Primary** | `#FFFFFF` | `#3D1A2A` | Текст на Primary. |
| **Error** | `#C94A4A` | `#F2B8B5` | Удаление, ошибки валидации. |

> **Совет:** Используй `MaterialTheme.colorScheme` везде. Для виджетов (Glance) цвета нужно дублировать в `GlanceTheme`, так как там нет доступа к системной теме динамически.

### Типографика (Google Fonts / Variable Fonts)
*Используй `FontFamily` с весами 400, 500, 600, 700.*

| Стиль | Шрифт | Размер / Weight | Letter Spacing | Использование |
| :--- | :--- | :--- | :--- | :--- |
| **Display Large** | **Playfair Display** (Serif) | 57sp / 400 | -0.25sp | Заголовок месяца в календаре ("ОКТЯБРЬ"). Элегантность. |
| **Headline Medium** | **Nunito Sans** (Rounded) | 28sp / 600 | 0sp | Заголовки экранов ("Мои дела"), дни недели. |
| **Title Large** | **Nunito Sans** | 22sp / 600 | 0sp | Название дела в списке. |
| **Body Large** | **Nunito Sans** | 16sp / 400 | +0.15sp | Описание дела, время. |
| **Label Large** | **Nunito Sans** | 14sp / 600 | +0.1sp | Кнопки, чипы, заголовки виджетов. |

### Скругления и Тени (Shape System)
*Главный инструмент «женственности» и мягкости — это **нестандартные скругления**.*

| Компонент | Corner Family | Size | Elevation / Shadow |
| :--- | :--- | :--- | :--- |
| **Карточка дела (Task Card)** | `RoundedCorner(24.dp)` | Extra Large | `Level 1` (0.5dp blur) + цветная тень `PrimaryContainer@10%` |
| **Кнопка FAB / Виджет** | `RoundedCorner(28.dp)` | Full | `Level 2` (1.5dp blur) |
| **Боттомшит (Add/Edit)** | `RoundedCorner(28.dp)` top only | Extra Large Top | `Level 3` |
| **Чипы (Приоритет/Тег)** | `RoundedCorner(100.dp)` | Full (Pill) | None (только фон) |
| **Ячейка календаря** | `RoundedCorner(16.dp)` | Large | None (состояние нажатия — Ripple) |

### Анимации (Motion)
*   **Shared Element Transition:** При клике на день — номер дня «перелетает» в заголовок детального экрана.
*   **Staggered List:** Появление дел в списке задержкой 50ms (ItemAnimation).
*   **Widget Update:** Анимация «галочки» при отметке выполнения (Lottie или `AnimatedVectorDrawable` в Glance).
*   **Spring Physics:** `spring(dampingRatio = Spring.DampingRatioMediumBouncy)` для модалок и FAB.

</details>

---

## 3. 🗄 Модель Данных (Room Entity)

```kotlin
// domain/model/Task.kt
@Entity(tableName = "tasks", indices = [
    Index(value = ["dateMillis"]), // Для быстрой выборки по дню
    Index(value = ["isDone", "dateMillis"]), // Для виджетов: невыполненные за день
    Index(value = ["sortOrder", "dateMillis"]) // Ручная сортировка
])
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(defaultValue = "") val description: String = "",
    val dateMillis: Long, // Начало дня (UTC) для группировки
    val timeMillis: Long? = null, // Время напоминания (опционально)
    val isDone: Boolean = false,
    val priority: Int = 0, // 0: Low, 1: Medium, 2: High
    val sortOrder: Float = 0f, // Для drag-and-drop сортировки
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val parentTaskId: Long? = null // Для подзадач (будущее)
)

// UI Model (Mapper в Repository/ViewModel)
data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val date: LocalDate,
    val time: LocalTime?,
    val isDone: Boolean,
    val priority: Priority,
    val sortOrder: Float
) {
    companion object {
        fun fromEntity(e: TaskEntity): Task = Task(
            id = e.id, title = e.title, description = e.description,
            date = Instant.ofEpochMilli(e.dateMillis).atZone(ZoneId.systemDefault()).toLocalDate(),
            time = e.timeMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime() },
            isDone = e.isDone, priority = Priority.fromInt(e.priority),
            sortOrder = e.sortOrder
        )
    }
}

enum class Priority(val color: Color, val label: String) {
    LOW(Color(0xFFA8C5BA), "Низкий"),      // Sage
    MEDIUM(Color(0xFFF4D39E), "Средний"),  // Sand
    HIGH(Color(0xFFC86B85), "Высокий")     // Rose
}
```

---

## 4. 🧠 Архитектура: MVI + Clean (Feature-First)

### Структура модулей (Multi-module рекомендуется для виджета)
```text
📦 app (compose, navigation, DI)
📦 core:common (utils, extensions, mappers)
📦 core:data (Room DB, DataSources, RepositoryImpl)
📦 core:domain (Entities, Repository Interfaces, UseCases)
📦 feature:calendar (UI: Month/Week/Day screens, ViewModels)
📦 feature:task_detail (BottomSheet/Dialog: Add/Edit/View)
📦 feature:widget (Glance AppWidget, WidgetViewModel/Receiver)
```

### State & Events (MVI Pattern)
```kotlin
// feature/calendar/CalendarState.kt
sealed interface CalendarState {
    data class Success(
        val currentMonth: YearMonth,
        val selectedDate: LocalDate,
        val tasksByDate: Map<LocalDate, List<Task>>, // Ключ - LocalDate
        val viewMode: ViewMode = ViewMode.MONTH
    ) : CalendarState
    
    data class Loading(val previousState: Success? = null) : CalendarState
    data class Error(val message: String, val previousState: Success? = null) : CalendarState
}

enum class ViewMode { MONTH, WEEK, DAY }

sealed interface CalendarEvent {
    data class OnDateClick(val date: LocalDate) : CalendarEvent
    data class OnTaskClick(val task: Task) : CalendarEvent
    data class OnTaskToggle(val taskId: Long, val isDone: Boolean) : CalendarEvent
    data class OnTaskMove(val taskId: Long, val newDate: LocalDate) : CalendarEvent
    object OnAddTaskClick : CalendarEvent
    object OnViewModeChanged : CalendarEvent
    data class OnHideDoneChanged(val hide: Boolean) : CalendarEvent
    // Swipe-to-refresh, etc.
}
```

### ViewModel (Пример)
```kotlin
// feature/calendar/CalendarViewModel.kt
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getTasksForMonth: GetTasksForMonthUseCase, // Flow<Map<LocalDate, List<Task>>>
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val moveTaskUseCase: MoveTaskUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalendarState>(CalendarState.Loading())
    val uiState: StateFlow<CalendarState> = _uiState

    private var currentMonth = YearMonth.now()
    private var selectedDate = LocalDate.now()
    private var viewMode = ViewMode.MONTH
    private var hideDone = false

    init { observeTasks() }

    private fun observeTasks() {
        viewModelScope.launch {
            getTasksForMonth(currentMonth)
                .map { tasksMap ->
                    // Фильтрация "Скрыть выполненные" происходит здесь или в UI
                    val filtered = if (hideDone) tasksMap.mapValues { it.value.filterNot { it.isDone } } else tasksMap
                    CalendarState.Success(currentMonth, selectedDate, filtered, viewMode)
                }
                .onEach { _uiState.value = it }
                .catch { _uiState.value = CalendarState.Error(it.message) }
                .launchIn(viewModelScope)
        }
    }

    fun onEvent(event: CalendarEvent) = when(event) {
        is CalendarEvent.OnDateClick -> {
            selectedDate = event.date
            // Обновляем стейт без перезагрузки БД
            updateSelectedDate()
        }
        is CalendarEvent.OnTaskToggle -> toggleTaskUseCase(event.taskId, event.isDone)
        is CalendarEvent.OnTaskMove -> moveTaskUseCase(event.taskId, event.newDate)
        is CalendarEvent.OnAddTaskClick -> navigateToAddTask(selectedDate)
        is CalendarEvent.OnViewModeChanged -> { viewMode = viewMode.next(); updateViewMode() }
        is CalendarEvent.OnHideDoneChanged -> { hideDone = event.hide; observeTasks() } // Перезапрос или маппинг
    }
    
    private fun updateSelectedDate() {
        val current = _uiState.value as? CalendarState.Success ?: return
        _uiState.value = current.copy(selectedDate = selectedDate)
    }
}
```

---

## 5. ⚡ Главная фишка: Интерактивный Виджет (Jetpack Glance)

**Это самый сложный пункт.** Обычные `RemoteViews` не поддерживают `RecyclerView` с кликами по чекбоксам внутри. **Glance** решает это через `Action`-ы и `Session`-ы.

### Концепция виджета
1.  **Размеры:** 4x2 (список на день), 4x4 (неделя + список), Resizable.
2.  **Состояние:** `GlanceStateDefinition` (Preferences) хранит `selectedDateWidget` и `hideDoneWidget`.
3.  **Данные:** `RxAppWidgetManager` / `WorkManager` обновляет виджет каждые 15-30 мин + при изменении БД (через `ContentProvider` или Broadcast от Room).

### Структура Glance
```kotlin
// feature/widget/TodoWidget.kt
@OptIn(ExperimentalGlanceApi::class)
class TodoWidget : GlanceAppWidget() {
    override fun provideGlance(context: Context, glanceId: GlanceId) {
        val repository = (context.applicationContext as MyApp).taskRepository // Или через Koin context
        val prefs = GlancePreferences(context, glanceId)
        
        // 1. Читаем настройки виджета
        val selectedDate = prefs.getString(KEY_SELECTED_DATE)?.let { LocalDate.parse(it) } ?: LocalDate.now()
        val hideDone = prefs.getBoolean(KEY_HIDE_DONE, false)
        
        // 2. Синхронно тянем задачи (Room позволяет suspend, но в Glance нужен блокирующий вызов или кэш)
        // Лучше: Room -> Flow -> Cache в Memory (Singleton) -> Glance читает из кэша мгновенно.
        val tasks = TaskCache.getTasksForDate(selectedDate, hideDone) 
        
        // 3. Рендерим
        provideContent {
            Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header: Месяц + Стрелки навигации
                WidgetHeader(date = selectedDate, glanceId = glanceId)
                
                // List: LazyColumn в Glance (поддерживается в новых версиях)
                if (tasks.isEmpty()) {
                    Text(text = "Свободный день 🌿", style = TextStyle(color = Color.Gray, fontSize = 16.sp))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tasks.forEach { task ->
                            TaskWidgetItem(task = task, glanceId = glanceId)
                        }
                    }
                }
                
                // Footer: Кнопка "Добавить" (открывает Activity)
                ActionButton(text = "+ Новое дело", onClick = StartActivityAction(Intent(context, MainActivity::class.java)))
            }
        }
    }
    
    companion object {
        fun updateAllWidgets(context: Context) {
            // Триггерится из Repository после Insert/Update/Delete
            GlanceAppWidget.update<TodoWidget>(context)
        }
    }
}
```

### Интерактивный элемент дела (TaskWidgetItem)
```kotlin
@Composable
fun TaskWidgetItem(task: Task, glanceId: GlanceId) {
    val isDone = task.isDone
    val textColor = if (isDone) Color.Gray else Color.Black
    
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(12.dp).background(
            color = if (isDone) Color.LightGray else Color.White,
            shape = RoundedShape(16.dp)
        ).pointerInput( // Обработка свайпов сложна в Glance, лучше кнопки
            // Long press -> Context Menu? Glance поддерживает onLongClick
        )
    ) {
        // Чекбокс - ГЛАВНЫЙ ЭКШН
        Checkbox(
            checked = isDone,
            onClick = ToggleTaskAction(task.id, glanceId), // Custom Action
            colors = CheckboxColors(checkedColor = MaterialTheme.colors.primary)
        )
        
        // Текст дела
        Text(
            text = task.title,
            modifier = GlanceModifier.weight(1f).padding(start = 12.dp),
            style = TextStyle(
                color = textColor, 
                fontSize = 14.sp, 
                fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                lineHeight = 20.sp,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
            ),
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        
        // Иконка приоритета
        if (task.priority != Priority.LOW) {
            Box(modifier = GlanceModifier.size(8.dp).background(task.priority.color, CircleShape))
        }
        
        // Кнопка "Перенести" (Открывает BottomSheet в Activity или диалог в виджете? Только Activity)
        Image(
            provider = ImageProvider(R.drawable.ic_move),
            contentDescription = "Перенести",
            modifier = GlanceModifier
                .size(24.dp)
                .padding(start = 8.dp)
                .fillMaxHeight()
                .clickable(StartActivityAction(Intent(...).putExtra("MOVE_TASK_ID", task.id)))
        )
    }
}

// Кастомный Action для чекбокса (выполняется в фоне, обновляет БД -> триггерит обновление виджета)
class ToggleTaskAction(private val taskId: Long, private val glanceId: GlanceId) : Action() {
    override fun onInvoke(context: Context, glanceId: GlanceId, params: ActionParameters) {
        // 1. Обновляем БД (CoroutineScope нужен, используем WorkManager или CoroutineScope в Application)
        CoroutineScope(Dispatchers.IO).launch {
            val repo = (context.applicationContext as MyApp).taskRepository
            val task = repo.getTask(taskId).first()
            repo.updateTask(task.copy(isDone = !task.isDone))
            
            // 2. Обновляем виджет
            TodoWidget.updateAllWidgets(context)
        }
    }
}
```

> **Важно про Glance:**
> *   Нет `LazyColumn` как в Compose (пока экспериментально). Используй `Column` + `forEach` (ограничение RemoteViews ~ 30-50 элементов).
> *   Состояние (выбранная дата, скрыть выполненные) храни в `GlancePreferences` (ProtoDataStore).
> *   Навигация по дням (вчера/завтра) — это `Action`, который меняет дату в `Preferences` и вызывает `TodoWidget.update(context)`.

---

## 6. 📅 Экраны и Навигация

### Граф навигации
```kotlin
// AppNavHost.kt
@Composable
fun AppNavHost(navController: NavHostController, startDestination: String = "calendar") {
    NavHost(navController, startDestination) {
        composable("calendar") { CalendarScreen() }
        
        // Bottom Sheet для добавления/редактирования
        // Используем ModalBottomSheetLayout на уровне CalendarScreen или отдельный DialogDestination
        // Лучше: ModalBottomSheet внутри CalendarScreen для контекста даты.
        
        // Детальный экран дня (при клике на день в месяце) - можно сделать BottomSheet или отдельный Screen
        composable(
            route = "day_detail/{date}",
            arguments = listOf(navArgument("date") { type = LocalDateTypeConverter })
        ) { backStackEntry ->
            val date = backStackEntry.getDate()
            DayDetailScreen(selectedDate = date)
        }
    }
}
```

### Главный экран (CalendarScreen)
*   **TopAppBar:** Название месяца (Playfair Display), Переключатель Месяц/Неделя/День (Segmented Button), Меню (Настройки, Синхронизация).
*   **Body:** `CalendarView` (LazyVerticalGrid для месяца / LazyRow для недели).
*   **Bottom Sheet (Persistent/Modal):** При клике на день -> раскрывается панель снизу со списком дел за этот день.
    *   Внутри: Список дел (LazyColumn), разделенный на "Активные" и "Выполненные" (свернутая секция).
    *   Чекбокс, Текст, Время, Приоритет (цветной кружок), Меню (3 точки -> Редактировать, Перенести, Удалить).
*   **FAB (Bottom Left):** `ExtendedFloatingActionButton` с иконкой `+`, текст "Новое дело". Анимация появления при скролле.

---

## 7. 🛠 Ключевые реализационные детали

### Drag & Drop сортировка дел (в BottomSheet/Дне)
Используй `ItemTouchHelper` с `LazyColumn` (через `androidx.compose.foundation:compose-foundation` -> `ReorderableLazyColumn` или библиотеку `compose-reorderable`).
Обновляй `sortOrder` в БД транзакционно.

### Перенос дела на другой день
1.  Долгое нажатие на дело -> "Перенести".
2.  Открывается **календарь-пикер** (Material Date Picker или кастомный Compose Dialog).
3.  Выбор даты -> `MoveTaskUseCase` обновляет `dateMillis` и сбрасывает `sortOrder`.

### Синхронизация Виджета <-> Приложение
```kotlin
// В RepositoryImpl
override suspend fun updateTask(task: TaskEntity) {
    dao.update(task)
    // Уведомляем виджет через Broadcast или WorkManager
    context.sendBroadcast(Intent(ACTION_TASKS_CHANGED).putExtra("date", task.dateMillis))
}

// WidgetReceiver или GlanceAppWidget.update() слушает этот Broadcast
```

---

## 8. 🚀 План запуска (Roadmap)

| Спринт | Фокус | Ключевые задачи |
| :--- | :--- | :--- |
| **1. Foundation** | Setup & Data | Модули, Koin/Hilt, Room, Theme (Colors/Fonts), CI/CD. |
| **2. Core Calendar** | Month View | `LazyVerticalGrid` календарь, навигация мес/нед/день, выбор даты. |
| **3. Task CRUD** | BottomSheet + DB | Добавление/Редактирование/Удаление, Приоритеты, Время, Ввод текста. |
| **4. Day View & Logic** | Interactions | Список дел за день, Чекбоксы, Скрыть выполненные, Drag&Drop сортировка, Перенос даты. |
| **5. Widget MVP** | Glance Basics | Статический виджет 4x2: Заголовок даты + 3 дела + Кнопка "Открыть". |
| **6. Widget Pro** | Interactivity | `Action` для чекбокса, Навигация дней (вчера/завтра), Настройка "Скрыть выполненные" в виджете. |
| **7. Polish & Design** | Delight | Анимации (Shared Element, Staggered), Empty States, Onboarding, Dynamic Color, Адаптивные иконки. |
| **8. Release** | Store | Настройка Play Console, Скриншоты (Фича виджет №1), Политика конфиденциальности. |

---

## 9. 💡 Уникальные фишки для ретеншена (Идеи)

1.  **«Утренний бриф» в виджете:** Показывает 3 главных дела на сегодня с иконкой погоды/настроения.
2.  **«Вечерний рефлекс»:** Push в 21:00: «Ты сделал 5 из 7 дел. Неплохо! 🌿 Перенести остальное на завтра?» (Кнопка действия в пуше -> переносит все невыполненные на завтра).
3.  **Теги-эмоции:** Вместо скучных тегов — эмодзи-настроение: 🌱 Рост, 💧 Отдых, 🔥 Важно, ❤️ Для себя. Отображаются маленькими иконками в виджете.
4.  **Виджет «Свободный слот»:** Если на день 0 дел — виджет показывает большу кнопку «Добавить заботу о себе» с пресетами (Прогулка, Вода, Медитация).

---

## 10. 📦 Стартовый код: Theme.kt (Копипаст для начала)

```kotlin
// ui/theme/Theme.kt
@Immutable
data class CustomShapes(
    val taskCard: CornerBasedShape = RoundedCornerShape(24.dp),
    val widgetCard: CornerBasedShape = RoundedCornerShape(20.dp),
    val bottomSheet: CornerBasedShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val chip: CornerBasedShape = RoundedCornerShape(100.dp),
    val fab: CornerBasedShape = RoundedCornerShape(28.dp)
) : ShapeAppearance

val LightCustomColors = lightColorScheme(
    primary = Color(0xFFC86B85),
    secondary = Color(0xFFA8C5BA),
    tertiary = Color(0xFFF4D39E),
    surface = Color(0xFFFFFEF7),
    surfaceVariant = Color(0xFFF5F0EB),
    background = Color(0xFFFFFEF7),
    onSurface = Color(0xFF2D2A27),
    outline = Color(0xFFD8CFC7),
    error = Color(0xFFC94A4A)
)

val DarkCustomColors = darkColorScheme(
    primary = Color(0xFFE8A8C0),
    secondary = Color(0xFF8AB3A0),
    tertiary = Color(0xFFD4B88A),
    surface = Color(0xFF1E1D1B),
    surfaceVariant = Color(0xFF2C2B28),
    background = Color(0xFF1E1D1B),
    onSurface = Color(0xFFEDEAE7),
    outline = Color(0xFF4A4945),
    error = Color(0xFFF2B8B5)
)

// Типографика
val CustomTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.playfair_display_regular, FontWeight.Normal)),
        fontSize = 57.sp, fontWeight = FontWeight.Normal, letterSpacing = -0.25.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily(Font(R.font.nunito_sans_regular, FontWeight.Normal)),
        fontSize = 28.sp, fontWeight = FontWeight.SemiBold
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.nunito_sans_regular, FontWeight.Normal)),
        fontSize = 22.sp, fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.nunito_sans_regular, FontWeight.Normal)),
        fontSize = 16.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.15.sp, lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.nunito_sans_regular, FontWeight.Normal)),
        fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp
    )
)

@Composable
fun TodoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkCustomColors else LightCustomColors
    val shapes = CustomShapes()
    
    // Dynamic Color (Material You) support for Android 12+
    val dynamicColors = if (darkTheme) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current)
    val finalColors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicColors else colors

    MaterialTheme(
        colorScheme = finalColors,
        typography = CustomTypography,
        shapes = shapes,
        content = content
    )
}

// Extension для удобного доступа к кастомным формам
val MaterialTheme.customShapes: CustomShapes
    @Composable get() = LocalShapeAppearance.current as CustomShapes
```

---

### 🎯 С чего начать прямо сейчас?
1.  Создай **Multi-module проект** в Android Studio (New Project -> "Empty Activity" -> настрой `settings.gradle.kts` для модулей).
2.  Подключи **KSP** (Kotlin Symbol Processing) вместо KAPT — быстрее сборка для Room/Glance/Koin.
3.  Добавь шрифты (`Playfair Display`, `Nunito Sans`) в `res/font` как `FontResource`.
4.  Реализуй **Room Entity + Dao + Database** + **Fake Repository** для превью Compose.
5.  Нарисуй **CalendarScreen** с `LazyVerticalGrid` (7 колонок) и фейковыми данными.
6.  Сделай **Theme.kt** (код выше) и примени к `MaterialTheme`.
7.  **Первый виджет:** Создай `GlanceAppWidget` с просто текстом "Привет, виджет!" и обнови его из `MainActivity` кнопкой.

Нужна помощь с конкретным куском кода (например, `LazyVerticalGrid` для календаря или настройка `Glance` в `build.gradle`)? Пиши, разберем детально! 🚀