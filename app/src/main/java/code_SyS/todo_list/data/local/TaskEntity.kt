package code_SyS.todo_list.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["dateMillis"]),
        Index(value = ["isDone", "dateMillis"]),
        Index(value = ["sortOrder", "dateMillis"]),
    ],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(defaultValue = "") val description: String = "",
    val dateMillis: Long, // Начало дня (локальная зона) — для группировки по датам
    val timeMillis: Long? = null, // Время напоминания (опционально)
    val isDone: Boolean = false,
    val priority: Int = 0, // 0: Low, 1: Medium, 2: High
    val sortOrder: Float = 0f, // Ручная сортировка
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
