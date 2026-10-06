package code_SyS.todo_list.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import code_SyS.todo_list.data.TodoApp

class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: androidx.glance.GlanceId) {
        TODO("implement widget in a later commit")
    }

    companion object {
        const val MAX_ITEMS = 8
    }
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}
