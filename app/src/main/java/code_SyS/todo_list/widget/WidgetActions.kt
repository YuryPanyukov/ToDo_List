package code_SyS.todo_list.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import code_SyS.todo_list.data.TodoApp
import java.time.LocalDate

/** Ключ с id дела для [ToggleTaskAction]. */
val TaskIdKey: ActionParameters.Key<Long> = ActionParameters.Key("taskId")

/** Ключ со сдвигом даты (в днях) для [ShiftDateAction]. */
val DateDeltaKey: ActionParameters.Key<Long> = ActionParameters.Key("dateDelta")

/**
 * Отметка дела выполненным прямо из виджета. Репозиторий после мутации сам
 * вызывает [TodoWidget.updateAll], поэтому здесь дополнительное обновление не нужно.
 */
class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val taskId = parameters[TaskIdKey] ?: return
        (context.applicationContext as TodoApp).repository.toggleDone(taskId)
    }
}

/** Переключение выбранного дня конкретного экземпляра виджета (вчера/завтра). */
class ShiftDateAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val delta = parameters[DateDeltaKey] ?: return
        shiftWidgetDate(context, glanceId, delta)
        TodoWidget().updateAll(context)
    }
}

/** Сброс выбранного дня виджета на сегодня. */
class ResetDateAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        updateWidgetState(context, glanceId) {
            this[SelectedDateKey] = LocalDate.now().toString()
        }
        TodoWidget().updateAll(context)
    }
}

/** Переключение режима «Скрыть выполненные» для конкретного экземпляра виджета. */
class ToggleHideDoneAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        updateWidgetState(context, glanceId) {
            this[HideDoneKey] = !(this[HideDoneKey] ?: false)
        }
        TodoWidget().updateAll(context)
    }
}
