package code_SyS.todo_list.ui.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import code_SyS.todo_list.domain.Task
import code_SyS.todo_list.ui.theme.customShapes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayTitleFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.forLanguageTag("ru"))

/**
 * Постоянная нижняя панель со списком дел выбранного дня.
 */
@Composable
fun DayPanel(
    date: LocalDate,
    activeTasks: List<Task>,
    doneTasks: List<Task>,
    hideDone: Boolean,
    onHideDoneChanged: (Boolean) -> Unit,
    onTaskToggle: (Long) -> Unit,
    onTaskEdit: (Task) -> Unit,
    onTaskMove: (Task) -> Unit,
    onTaskDelete: (Long) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.customShapes.bottomSheet,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = date.format(dayTitleFormatter),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Скрыть",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Switch(checked = hideDone, onCheckedChange = onHideDoneChanged)
                    }
                }
            }
            item {
                TextButton(onClick = onAddTask, modifier = Modifier.padding(start = 4.dp)) {
                    Text("+ Новое дело")
                }
            }
            if (activeTasks.isEmpty() && doneTasks.isEmpty()) {
                item {
                    Text(
                        text = "Свободный день 🌿",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            items(activeTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggle = { onTaskToggle(task.id) },
                    onEdit = { onTaskEdit(task) },
                    onMove = { onTaskMove(task) },
                    onDelete = { onTaskDelete(task.id) },
                )
            }
            if (doneTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Выполненные (${doneTasks.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
                    )
                }
                items(doneTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { onTaskToggle(task.id) },
                        onEdit = { onTaskEdit(task) },
                        onMove = { onTaskMove(task) },
                        onDelete = { onTaskDelete(task.id) },
                    )
                }
            }
        }
    }
}
