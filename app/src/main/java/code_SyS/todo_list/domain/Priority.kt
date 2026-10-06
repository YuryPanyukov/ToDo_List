package code_SyS.todo_list.domain

import androidx.compose.ui.graphics.Color

enum class Priority(val color: Color, val label: String) {
    LOW(Color(0xFFA8C5BA), "Низкий"), // Sage
    MEDIUM(Color(0xFFF4D39E), "Средний"), // Sand
    HIGH(Color(0xFFC86B85), "Высокий"); // Rose

    companion object {
        fun fromInt(value: Int): Priority = entries.getOrElse(value) { LOW }
    }
}
