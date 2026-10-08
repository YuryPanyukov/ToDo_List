package code_SyS.todo_list.ui.calendar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import code_SyS.todo_list.domain.Priority
import code_SyS.todo_list.domain.Task
import code_SyS.todo_list.ui.calendar.TaskEditorState
import code_SyS.todo_list.ui.theme.AppMotion
import code_SyS.todo_list.ui.theme.customShapes
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFullFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru"))
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Длительность сопутствующего затемнения содержимого, мс — пружина задаётся [AppMotion]. */
private const val SHEET_CONTENT_FADE_MS = 200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(
    editorState: TaskEditorState,
    onSave: (title: String, description: String, date: LocalDate, timeMillis: Long?, priorityOrdinal: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val task = editorState.task
    var title by remember(editorState) { mutableStateOf(task?.title ?: "") }
    var description by remember(editorState) { mutableStateOf(task?.description ?: "") }
    var date by remember(editorState) { mutableStateOf(editorState.date) }
    var time by remember(editorState) { mutableStateOf(task?.time) }
    var priority by remember(editorState) { mutableStateOf(task?.priority ?: Priority.LOW) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Содержимое модалки «садится» на место с пружиной, пока лист поднимается — §2 спеки (Motion).
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }
    val contentScale by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0.94f,
        animationSpec = AppMotion.bouncy(),
        label = "sheetContentScale",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(SHEET_CONTENT_FADE_MS),
        label = "sheetContentAlpha",
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.customShapes.bottomSheet,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = contentScale
                    scaleY = contentScale
                    alpha = contentAlpha
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (task == null) "Новое дело" else "Редактировать дело",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Что нужно сделать?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Заметка (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
            )

            // Дата: ← / → для ±1 дня, календарь — выбор произвольной даты
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = { date = date.minusDays(1) }) { Text("←") }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = date.format(dateFullFormatter),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = "Выбрать дату в календаре",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                TextButton(onClick = { date = date.plusDays(1) }) { Text("→") }
            }

            // Время
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Время", style = MaterialTheme.typography.bodyLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text(text = time?.format(timeFormatter) ?: "Выбрать")
                    }
                    AnimatedVisibility(
                        visible = time != null,
                        enter = fadeIn(animationSpec = tween(SHEET_CONTENT_FADE_MS)) +
                            expandHorizontally(animationSpec = AppMotion.smooth()),
                        exit = fadeOut(animationSpec = tween(SHEET_CONTENT_FADE_MS)) +
                            shrinkHorizontally(animationSpec = AppMotion.smooth()),
                    ) {
                        TextButton(onClick = { time = null }) { Text("Очистить") }
                    }
                }
            }

            // Приоритет: чипы
            Text("Приоритет", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p.label) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(p.color),
                            )
                        },
                    )
                }
            }

            Button(
                onClick = {
                    val timeMillis = time?.let {
                        LocalTime.of(it.hour, it.minute).atDate(date)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    }
                    onSave(title, description, date, timeMillis, priority.ordinal)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.customShapes.fab,
                enabled = title.isNotBlank(),
            ) {
                Text(if (task == null) "Добавить" else "Сохранить")
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time?.hour ?: 9,
            initialMinute = time?.minute ?: 0,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Отмена") }
            },
            text = { TimePicker(state = timePickerState) },
        )
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDate = date,
            onConfirm = { picked ->
                date = picked
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}
