package code_SyS.todo_list.widget

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import java.time.LocalDate

// Состояние виджета хранится в PreferencesGlanceStateDefinition:
// у каждого экземпляра виджета (GlanceId) своя дата и свой режим «скрыть выполненные».

/** Выбранный в виджете день (ISO-8601). */
val SelectedDateKey = stringPreferencesKey("selected_date")

/** Режим «скрыть выполненные» для конкретного виджета. */
val HideDoneKey = booleanPreferencesKey("hide_done")

private fun parseDate(raw: String?): LocalDate? =
    raw?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

/** Читает дату конкретного экземпляра виджета; по умолчанию — сегодня. */
suspend fun widgetSelectedDate(context: Context, glanceId: GlanceId): LocalDate =
    parseDate(
        getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)[SelectedDateKey],
    ) ?: LocalDate.now()

/** Читает режим «скрыть выполненные» конкретного экземпляра виджета. */
suspend fun widgetHideDone(context: Context, glanceId: GlanceId): Boolean =
    getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)[HideDoneKey] ?: false

/** Атомарно обновляет состояние конкретного экземпляра виджета. */
suspend fun updateWidgetState(
    context: Context,
    glanceId: GlanceId,
    transform: MutablePreferences.() -> Unit,
) {
    updateAppWidgetState(context, glanceId) { prefs -> prefs.transform() }
}

/** Сдвигает выбранную дату экземпляра на [deltaDays] дней. */
suspend fun shiftWidgetDate(context: Context, glanceId: GlanceId, deltaDays: Long) {
    updateWidgetState(context, glanceId) {
        val current = parseDate(this[SelectedDateKey]) ?: LocalDate.now()
        this[SelectedDateKey] = current.plusDays(deltaDays).toString()
    }
}
