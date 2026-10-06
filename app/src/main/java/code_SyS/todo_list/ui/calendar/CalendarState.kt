package code_SyS.todo_list.ui.calendar

import code_SyS.todo_list.domain.Task
import java.time.LocalDate
import java.time.YearMonth

sealed interface CalendarState {
    val hideDone: Boolean

    data class Success(
        val currentMonth: YearMonth,
        val selectedDate: LocalDate,
        val tasksByDate: Map<LocalDate, List<Task>>,
        val viewMode: ViewMode = ViewMode.MONTH,
        override val hideDone: Boolean = false,
    ) : CalendarState {
        val selectedTasks: List<Task> get() = tasksByDate[selectedDate].orEmpty()
        val selectedActive: List<Task> get() = selectedTasks.filterNot { it.isDone }
        val selectedDone: List<Task> get() = selectedTasks.filter { it.isDone }
    }

    data class Loading(override val hideDone: Boolean = false) : CalendarState
    data class Error(val message: String, override val hideDone: Boolean = false) : CalendarState
}

enum class ViewMode { MONTH, WEEK;
    fun next(): ViewMode = if (this == MONTH) WEEK else MONTH
}

sealed interface CalendarEvent {
    data class OnDateClick(val date: LocalDate) : CalendarEvent
    data class OnMonthShift(val months: Long) : CalendarEvent
    data class OnTaskClick(val task: Task) : CalendarEvent
    data class OnTaskToggle(val taskId: Long) : CalendarEvent
    data class OnTaskMove(val taskId: Long, val newDate: LocalDate) : CalendarEvent
    data class OnTaskDelete(val taskId: Long) : CalendarEvent
    data class OnTaskEdit(val task: Task) : CalendarEvent
    data class OnHideDoneChanged(val hide: Boolean) : CalendarEvent
    object OnViewModeChanged : CalendarEvent
    object OnAddTaskClick : CalendarEvent
    object OnDismissEditor : CalendarEvent
}
