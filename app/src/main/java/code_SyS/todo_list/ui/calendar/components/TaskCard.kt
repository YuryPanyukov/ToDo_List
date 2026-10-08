package code_SyS.todo_list.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import code_SyS.todo_list.domain.Task
import code_SyS.todo_list.ui.theme.customShapes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onMove: (LocalDate) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var moveDialogOpen by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.customShapes.taskCard)
            .background(colors.surfaceVariant)
            .clickable(onClick = onEdit)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = task.isDone,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.primary,
                checkmarkColor = colors.onPrimary,
            ),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontWeight = if (task.isDone) androidx.compose.ui.text.font.FontWeight.Normal
                else androidx.compose.ui.text.font.FontWeight.SemiBold,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                color = if (task.isDone) colors.onSurfaceVariant else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val details = buildList {
                task.time?.let { add(it.format(DateTimeFormatter.ofPattern("HH:mm"))) }
                if (task.description.isNotBlank()) add(task.description)
            }
            if (details.isNotEmpty()) {
                Text(
                    text = details.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.alpha(0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (task.priority != code_SyS.todo_list.domain.Priority.LOW) {
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(task.priority.color),
            )
        }
        IconButton(onClick = { menuOpen = true }) {
            Icon(
                imageVector = Icons.Filled.DragIndicator,
                contentDescription = "Меню дела",
                tint = colors.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text("Редактировать") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                onClick = { menuOpen = false; onEdit() },
            )
            DropdownMenuItem(
                text = { Text("Перенести на другой день") },
                leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null) },
                onClick = { menuOpen = false; moveDialogOpen = true },
            )
            DropdownMenuItem(
                text = { Text("Удалить") },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = { menuOpen = false; onDelete() },
            )
        }
    }

    if (moveDialogOpen) {
        AppDatePickerDialog(
            initialDate = task.date,
            onConfirm = { newDate ->
                moveDialogOpen = false
                onMove(newDate)
            },
            onDismiss = { moveDialogOpen = false },
        )
    }
}
