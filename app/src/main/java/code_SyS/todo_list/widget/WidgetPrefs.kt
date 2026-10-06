package code_SyS.todo_list.widget

import android.content.Context
import java.time.LocalDate

/** Простое хранилище состояния виджета (выбранная дата, скрыть выполненные). */
object WidgetPrefs {
    private const val FILE = "todo_widget_prefs"
    private const val KEY_DATE = "selected_date"
    private const val KEY_HIDE_DONE = "hide_done"

    fun selectedDate(context: Context): LocalDate =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(KEY_DATE, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()

    fun setSelectedDate(context: Context, date: LocalDate) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_DATE, date.toString()).apply()
    }

    fun hideDone(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_HIDE_DONE, false)

    fun setHideDone(context: Context, hide: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_HIDE_DONE, hide).apply()
    }
}
