package code_SyS.todo_list.domain

import code_SyS.todo_list.data.local.TaskEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val date: LocalDate,
    val time: LocalTime?,
    val isDone: Boolean,
    val priority: Priority,
    val sortOrder: Float,
) {
    companion object {
        private val zone: ZoneId = ZoneId.systemDefault()

        fun fromEntity(e: TaskEntity): Task = Task(
            id = e.id,
            title = e.title,
            description = e.description,
            date = Instant.ofEpochMilli(e.dateMillis).atZone(zone).toLocalDate(),
            time = e.timeMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() },
            isDone = e.isDone,
            priority = Priority.fromInt(e.priority),
            sortOrder = e.sortOrder,
        )

        fun startOfDayMillis(date: LocalDate): Long =
            date.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
