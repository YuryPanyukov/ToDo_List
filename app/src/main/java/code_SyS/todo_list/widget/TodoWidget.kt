package code_SyS.todo_list.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.CheckboxDefaults
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import code_SyS.todo_list.MainActivity
import code_SyS.todo_list.data.TodoApp
import code_SyS.todo_list.domain.Priority
import code_SyS.todo_list.domain.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Интерактивный виджет «Список дел»: показывает дела выбранного дня,
 * позволяет отмечать выполнение прямо с рабочего стола и листать дни.
 */
class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Состояние читается из GlanceStateDefinition для конкретного экземпляра виджета.
        val date = widgetSelectedDate(context, id)
        val hideDone = widgetHideDone(context, id)
        val repository = (context.applicationContext as TodoApp).repository
        // Блокирующая выборка (Room Flow.first()) — уводим с главного потока.
        val tasks = withContext(Dispatchers.IO) { repository.getTasksForDateBlocking(date) }

        provideContent {
            TodoWidgetContent(date = date, tasks = tasks, hideDone = hideDone)
        }
    }

    companion object {
        const val MAX_ITEMS = 6
    }
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}

private val headerFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.forLanguageTag("ru"))

@Composable
private fun TodoWidgetContent(date: LocalDate, tasks: List<Task>, hideDone: Boolean) {
    val visible = (if (hideDone) tasks.filterNot { it.isDone } else tasks).take(TodoWidget.MAX_ITEMS)
    val remaining = tasks.count { !it.isDone }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(WidgetColors.SurfaceVariant))
            .cornerRadius(24.dp)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Заголовок: стрелки навигации по дням + дата + счётчик
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavArrow("‹", -1L)
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = date.format(headerFormatter).replaceFirstChar { it.uppercase() },
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.OnSurface),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                )
                Text(
                    text = if (remaining == 0) "Всё выполнено" else "Осталось: $remaining",
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.OnSurfaceVariant),
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                )
            }
            NavArrow("›", 1L)
        }

        if (visible.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (tasks.isEmpty()) "Свободный день 🌿" else "Все дела выполнены 🎉",
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.OnSurfaceVariant),
                        fontSize = 14.sp,
                    ),
                )
            }
        } else {
            visible.forEach { task -> TaskRow(task) }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Футер: «скрыть выполненные» + добавление дела
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (hideDone) "Показать выполненные" else "Скрыть выполненные",
                modifier = GlanceModifier
                    .defaultWeight()
                    .clickable(actionRunCallback<ToggleHideDoneAction>()),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.Secondary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            Text(
                text = "＋ Дело",
                modifier = GlanceModifier.clickable(
                    actionStartActivity(Intent(LocalContext.current, MainActivity::class.java)),
                ),
                style = TextStyle(
                    color = ColorProvider(WidgetColors.Primary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun NavArrow(label: String, delta: Long) {
    Text(
        text = label,
        modifier = GlanceModifier
            .clickable(
                actionRunCallback<ShiftDateAction>(actionParametersOf(DateDeltaKey to delta)),
            )
            .padding(horizontal = 10.dp),
        style = TextStyle(
            color = ColorProvider(WidgetColors.Primary),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        ),
        maxLines = 1,
    )
}

@Composable
private fun TaskRow(task: Task) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(ColorProvider(WidgetColors.Surface))
            .cornerRadius(16.dp)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckBox(
            checked = task.isDone,
            onCheckedChange = actionRunCallback<ToggleTaskAction>(
                actionParametersOf(TaskIdKey to task.id),
            ),
            colors = CheckboxDefaults.colors(
                checkedColor = ColorProvider(WidgetColors.Primary),
                uncheckedColor = ColorProvider(WidgetColors.Outline),
            ),
        )
        Text(
            text = task.title,
            modifier = GlanceModifier.defaultWeight().padding(start = 8.dp),
            style = TextStyle(
                color = ColorProvider(
                    if (task.isDone) WidgetColors.OnSurfaceVariant else WidgetColors.OnSurface,
                ),
                fontSize = 14.sp,
                fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.Medium,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
            ),
            maxLines = 1,
        )
        if (task.priority != Priority.LOW) {
            Box(
                modifier = GlanceModifier
                    .size(8.dp)
                    .background(ColorProvider(task.priority.color))
                    .cornerRadius(4.dp),
            ) {}
        }
    }
}

private object WidgetColors {
    val Primary = Color(0xFFC86B85)
    val Secondary = Color(0xFF7FA695)
    val Outline = Color(0xFFD8CFC7)
    val OnSurface = Color(0xFF2D2A27)
    val OnSurfaceVariant = Color(0xFF6E655D)
    val Surface = Color(0xFFFFFEF7)
    val SurfaceVariant = Color(0xFFF5F0EB)
}
