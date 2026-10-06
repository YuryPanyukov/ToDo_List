package code_SyS.todo_list.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import code_SyS.todo_list.domain.Task
import code_SyS.todo_list.ui.theme.customShapes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private val weekdayLabels: List<String> = DayOfWeek.entries.map {
    it.getDisplayName(JavaTextStyle.SHORT, Locale("ru")).replace(".", "")
}

@Composable
fun WeekdayHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        weekdayLabels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun MonthGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val firstDayOfMonth = currentMonth.atDay(1)
    val leadingEmpty = firstDayOfMonth.dayOfWeek.value % 7 // Пн = 0 ... Вс = 6
    val daysInMonth = currentMonth.lengthOfMonth()
    val cells = List(leadingEmpty) { null } + (1..daysInMonth).map { currentMonth.atDay(it) }
    val rows = cells.chunked(7)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        if (date != null) {
                            DayCell(
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == today,
                                taskCount = tasksByDate[date]?.size ?: 0,
                                hasUndone = tasksByDate[date]?.any { !it.isDone } ?: false,
                                onClick = { onDateClick(date) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                repeat(7 - week.size) { Box(modifier = Modifier.weight(1f).aspectRatio(1f)) }
            }
        }
    }
}

@Composable
fun WeekStrip(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val week = (0..6L).map { weekStart.plusDays(it) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            week.forEach { date ->
                Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                    DayCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        taskCount = tasksByDate[date]?.size ?: 0,
                        hasUndone = tasksByDate[date]?.any { !it.isDone } ?: false,
                        onClick = { onDateClick(date) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    taskCount: Int,
    hasUndone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val container = when {
        isSelected -> colors.primary
        isToday -> colors.primaryContainer
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    val content = when {
        isSelected -> colors.onPrimary
        else -> colors.onSurface
    }
    Column(
        modifier = modifier
            .padding(2.dp)
            .clip(MaterialTheme.customShapes.calendarCell)
            .background(container)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 15.sp,
            fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
            color = content,
        )
        if (taskCount > 0) {
            val dotColor = when {
                isSelected -> colors.onPrimary
                hasUndone -> colors.secondary
                else -> colors.outline
            }
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        } else {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(7.dp))
        }
    }
}
