package code_SyS.todo_list.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code_SyS.todo_list.data.TaskRepository
import code_SyS.todo_list.domain.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val repository: TaskRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalendarState>(CalendarState.Loading())
    val uiState: StateFlow<CalendarState> = _uiState.asStateFlow()

    private var currentMonth = YearMonth.now()
    private var selectedDate = LocalDate.now()
    private var viewMode = ViewMode.MONTH
    private var hideDone = false

    // Редактор (bottom-sheet): null — закрыт; null-id — добавление
    private val _editor = MutableStateFlow<TaskEditorState?>(null)
    val editor: StateFlow<TaskEditorState?> = _editor.asStateFlow()

    private val _events = MutableStateFlow<CalendarUiEvent?>(null)
    val uiEvents: StateFlow<CalendarUiEvent?> = _events.asStateFlow()

    private val monthRange: Pair<LocalDate, LocalDate>
        get() {
            val from = currentMonth.minusMonths(1).atDay(1)
            val to = currentMonth.plusMonths(1).atEndOfMonth()
            return from to to
        }

    init {
        observeTasks()
    }

    private fun observeTasks() {
        val (from, to) = monthRange
        repository.observeBetween(from, to)
            .map { tasksByDate -> filterDone(tasksByDate) }
            .onEach { tasksByDate ->
                _uiState.value = CalendarState.Success(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    tasksByDate = tasksByDate,
                    viewMode = viewMode,
                    hideDone = hideDone,
                )
            }
            .catch { throwable ->
                _uiState.value = CalendarState.Error(throwable.message ?: "Ошибка загрузки", hideDone)
            }
            .launchIn(viewModelScope)
    }

    private fun filterDone(tasksByDate: Map<LocalDate, List<Task>>): Map<LocalDate, List<Task>> =
        if (hideDone) {
            tasksByDate.mapValues { (_, tasks) -> tasks.filterNot { it.isDone } }
                .filterValues { it.isNotEmpty() }
        } else {
            tasksByDate
        }

    fun onEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.OnDateClick -> {
                selectedDate = event.date
                if (event.date.month != currentMonth.month || event.date.year != currentMonth.year) {
                    currentMonth = YearMonth.from(event.date)
                    observeTasks()
                } else {
                    updateState()
                }
            }
            is CalendarEvent.OnMonthShift -> {
                currentMonth = currentMonth.plusMonths(event.months)
                selectedDate = selectedDate.let { currentMonth.atDay(1) }
                observeTasks()
            }
            is CalendarEvent.OnViewModeChanged -> {
                viewMode = viewMode.next()
                updateState()
            }
            is CalendarEvent.OnHideDoneChanged -> {
                hideDone = event.hide
                observeTasks()
            }
            is CalendarEvent.OnTaskToggle -> viewModelScope.launch { repository.toggleDone(event.taskId) }
            is CalendarEvent.OnTaskMove -> viewModelScope.launch {
                repository.moveTask(event.taskId, event.newDate)
            }
            is CalendarEvent.OnTaskDelete -> viewModelScope.launch { repository.deleteTask(event.taskId) }
            is CalendarEvent.OnAddTaskClick -> {
                _editor.value = TaskEditorState(date = selectedDate, task = null)
            }
            is CalendarEvent.OnTaskEdit -> {
                _editor.value = TaskEditorState(date = event.task.date, task = event.task)
            }
            is CalendarEvent.OnTaskClick -> { /* редактирование через OnTaskEdit */ }
            CalendarEvent.OnDismissEditor -> _editor.value = null
        }
    }

    fun saveTask(title: String, description: String, date: LocalDate, timeMillis: Long?, priorityOrdinal: Int) {
        if (title.isBlank()) {
            _events.value = CalendarUiEvent.ShowMessage("Введите название дела")
            return
        }
        viewModelScope.launch {
            val editing = _editor.value?.task
            if (editing != null) {
                repository.updateTask(
                    task = editing.copy(
                        title = title,
                        description = description,
                        date = date,
                        priority = code_SyS.todo_list.domain.Priority.fromInt(priorityOrdinal),
                    ),
                    timeMillis = timeMillis,
                )
            } else {
                repository.addTask(title, description, date, timeMillis, priorityOrdinal)
            }
            _editor.value = null
        }
    }

    fun consumeUiEvent() {
        _events.value = null
    }

    private fun updateState() {
        val current = _uiState.value as? CalendarState.Success ?: return
        _uiState.value = current.copy(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            viewMode = viewMode,
            hideDone = hideDone,
        )
    }
}

data class TaskEditorState(
    val date: LocalDate,
    val task: Task?, // null — добавление нового дела
)

sealed interface CalendarUiEvent {
    data class ShowMessage(val text: String) : CalendarUiEvent
}
