package code_SyS.todo_list.ui.calendar

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import code_SyS.todo_list.ui.calendar.components.DayPanel
import code_SyS.todo_list.ui.calendar.components.MonthGrid
import code_SyS.todo_list.ui.calendar.components.TaskEditorSheet
import code_SyS.todo_list.ui.calendar.components.WeekStrip
import code_SyS.todo_list.ui.calendar.components.WeekdayHeader
import code_SyS.todo_list.ui.theme.customShapes
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val state by viewModel.uiState.collectAsState()
    val editor by viewModel.editor.collectAsState()
    val uiEvent by viewModel.uiEvents.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

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
        floatingActionButton = {
            if (state is CalendarState.Success) {
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
                            onDateClick = { viewModel.onEvent(CalendarEvent.OnDateClick(it)) },
                        )
                    } else {
                        WeekStrip(
                            weekStart = success.selectedDate.minusDays(
                                (success.selectedDate.dayOfWeek.value + 6).mod(7).toLong(),
                            ),
                            selectedDate = success.selectedDate,
                            tasksByDate = success.tasksByDate,
                            onDateClick = { viewModel.onEvent(CalendarEvent.OnDateClick(it)) },
                        )
                    }
                }

                Text(
                    text = success.selectedDate.format(daySubtitleFormatter),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )

                DayPanel(
                    date = success.selectedDate,
                    activeTasks = success.selectedActive,
                    doneTasks = success.selectedDone,
                    hideDone = success.hideDone,
                    onHideDoneChanged = { viewModel.onEvent(CalendarEvent.OnHideDoneChanged(it)) },
                    onTaskToggle = { viewModel.onEvent(CalendarEvent.OnTaskToggle(it)) },
                    onTaskEdit = { viewModel.onEvent(CalendarEvent.OnTaskEdit(it)) },
                    onTaskMove = { viewModel.onEvent(CalendarEvent.OnTaskMove(it.id, it.date.plusDays(1))) },
                    onTaskDelete = { viewModel.onEvent(CalendarEvent.OnTaskDelete(it)) },
                    onAddTask = { viewModel.onEvent(CalendarEvent.OnAddTaskClick) },
                )
            }
        }
    }

    // Редактор дела
    editor?.let { editorState ->
        TaskEditorSheet(
            editorState = editorState,
            onSave = { title, description, date, timeMillis, priorityOrdinal ->
                viewModel.saveTask(title, description, date, timeMillis, priorityOrdinal)
            },
            onDismiss = { viewModel.onEvent(CalendarEvent.OnDismissEditor) },
        )
    }
}

private val daySubtitleFormatter: java.time.format.DateTimeFormatter =
    java.time.format.DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.forLanguageTag("ru"))
