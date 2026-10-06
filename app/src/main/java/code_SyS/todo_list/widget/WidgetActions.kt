package code_SyS.todo_list.widget

import android.content.Context
import code_SyS.todo_list.data.TodoApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Здесь в точке каталога Glance 1.1.1 не дана поддержка Action / updateAll.
 * Ниже заглушки для шорткатов виджета; их реализация ожидается при подключении Glance новее 1.2.0.
 */
fun toggleTask(context: Context, taskId: Long) {
    CoroutineScope(Dispatchers.IO).launch {
        (context.applicationContext as TodoApp).repository.toggleDone(taskId)
        // update widget:
        // TodoWidget.updateAll(context)
    }
}

fun shiftDate(context: Context, delta: Long) {
    val newDate = WidgetPrefs.selectedDate(context).plusDays(delta)
    WidgetPrefs.setSelectedDate(context, newDate)
}

fun toggleHideDone(context: Context) {
    WidgetPrefs.setHideDone(context, !WidgetPrefs.hideDone(context))
}

fun resetDate(context: Context) {
    WidgetPrefs.setSelectedDate(context, java.time.LocalDate.now())
}

fun openApp(context: Context) {
    // open application via intent — stub pending
}
