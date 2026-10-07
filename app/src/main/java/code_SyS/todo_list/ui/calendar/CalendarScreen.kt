package code_SyS.todo_list.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import code_SyS.todo_list.ui.calendar.components.MonthGrid
import code_SyS.todo_list.ui.calendar.components.WeekStrip
import code_SyS.todo_list.ui.calendar.components.WeekdayHeader
import code_SyS.todo_list.ui.navigation.daySharedKey
import code_SyS.todo_list.ui.theme.AppMotion
import code_SyS.todo_list.ui.theme.customShapes
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onOpenSettings: () -> Unit = {},
    onOpenDay: (LocalDate) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val state by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvents.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // FAB появляется с пружиной, а не мгновенно — §2 спеки (Motion).
    var fabVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { fabVisible = true }

    LaunchedEffect(uiEvent) {
        when (val e = uiEvent) {
            is CalendarUiEvent.ShowMessage -> {
                snackbarHostState.showSnackbar(e.text)
                viewModel.consumeUiEvent()
            }
            null -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // По спеке §6 FAB живёт в левом нижнем углу, а не в правом по умолчанию.
        floatingActionButtonPosition = FabPosition.Start,
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible && state is CalendarState.Success,
                enter = fadeIn(animationSpec = tween(FAB_FADE_MS)) +
                    scaleIn(animationSpec = AppMotion.bouncy(), initialScale = 0.6f),
                exit = fadeOut(animationSpec = tween(FAB_FADE_MS)) +
                    scaleOut(animationSpec = AppMotion.smooth(), targetScale = 0.6f),
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.onEvent(CalendarEvent.OnAddTaskClick) },
                    shape = MaterialTheme.customShapes.fab,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Новое дело", modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val success = state as? CalendarState.Success

            if (success == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val message = when (val s = state) {
                        is CalendarState.Error -> s.message
                        else -> "Загрузка…"
                    }
                    Text(message, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                // Клик по дню: сначала выбираем дату (чтобы общий элемент совпал), затем открываем день.
                val onDayClick: (LocalDate) -> Unit = { date ->
                    viewModel.onEvent(CalendarEvent.OnDateClick(date))
                    onOpenDay(date)
                }

                // Общий элемент перехода: выбранная ячейка календаря ↔ бейдж дня на экране дня.
                val dayCellModifier: @Composable (LocalDate) -> Modifier = { date ->
                    if (date == success.selectedDate) {
                        with(sharedTransitionScope) {
                            Modifier.sharedBounds(
                                rememberSharedContentState(daySharedKey(date)),
                                animatedVisibilityScope,
                                resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                            )
                        }
                    } else {
                        Modifier
                    }
                }

                // Верхняя панель: месяц, стрелки, переключатель вида
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = success.currentMonth.month
                                .getDisplayName(JavaTextStyle.FULL, Locale.forLanguageTag("ru"))
                                .replaceFirstChar { it.uppercase(Locale.forLanguageTag("ru")) },
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = success.currentMonth.year.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(CalendarEvent.OnMonthShift(-1)) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
                    }
                    IconButton(onClick = { viewModel.onEvent(CalendarEvent.OnMonthShift(1)) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Следующий месяц")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Настройки")
                    }
                }

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    SegmentedButton(
                        selected = success.viewMode == ViewMode.MONTH,
                        onClick = {
                            if (success.viewMode != ViewMode.MONTH) {
                                viewModel.onEvent(CalendarEvent.OnViewModeChanged)
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text("Месяц") }
                    SegmentedButton(
                        selected = success.viewMode == ViewMode.WEEK,
                        onClick = {
                            if (success.viewMode != ViewMode.WEEK) {
                                viewModel.onEvent(CalendarEvent.OnViewModeChanged)
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text("Неделя") }
                }

                // Сетка календаря
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    if (success.viewMode == ViewMode.MONTH) {
                        WeekdayHeader(modifier = Modifier.padding(bottom = 4.dp))
                        MonthGrid(
                            currentMonth = success.currentMonth,
                            selectedDate = success.selectedDate,
                            tasksByDate = success.tasksByDate,
                            onDateClick = onDayClick,
                            cellModifier = dayCellModifier,
                        )
                    } else {
                        WeekStrip(
                            weekStart = success.selectedDate.minusDays(
                                (success.selectedDate.dayOfWeek.value + 6).mod(7).toLong(),
                            ),
                            selectedDate = success.selectedDate,
                            tasksByDate = success.tasksByDate,
                            onDateClick = onDayClick,
                            cellModifier = dayCellModifier,
                        )
                    }
                }

                // Компактная сводка по выбранному дню: тап открывает экран дня.
                DaySummaryCard(
                    date = success.selectedDate,
                    activeTasks = success.selectedActive.size,
                    totalTasks = success.selectedActive.size + success.selectedDone.size,
                    onClick = { onOpenDay(success.selectedDate) },
                )
            }
        }
    }
}

/**
 * Строка-подсказка под сеткой: дата, сколько дел осталось и переход к полному экрану дня.
 */
@Composable
private fun DaySummaryCard(
    date: LocalDate,
    activeTasks: Int,
    totalTasks: Int,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.customShapes.taskCard)
            .clickable(onClick = onClick),
        shape = MaterialTheme.customShapes.taskCard,
        color = colors.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date.format(daySubtitleFormatter),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = when {
                        totalTasks == 0 -> "Дел нет"
                        activeTasks == 0 -> "Все дела закрыты"
                        else -> "Осталось: $activeTasks"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Открыть день",
                tint = colors.primary,
            )
        }
    }
}

/** Длительность сопутствующего FAB затемнения, мс — сама пружина задаётся [AppMotion]. */
private const val FAB_FADE_MS = 180

private val daySubtitleFormatter: java.time.format.DateTimeFormatter =
    java.time.format.DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.forLanguageTag("ru"))
