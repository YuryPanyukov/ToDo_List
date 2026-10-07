package code_SyS.todo_list.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.glance.appwidget.updateAll
import code_SyS.todo_list.data.local.TaskDao
import code_SyS.todo_list.data.local.TaskEntity
import code_SyS.todo_list.domain.Task
import code_SyS.todo_list.widget.TodoWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/** Единая точка доступа к задачам. После каждой мутации уведомляет виджеты,
 * чтобы список на рабочем столе всегда отображал актуальные данные.
 */
class TaskRepository(
    private val dao: TaskDao,
    private val context: Context,
) {

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val widgetUpdateHandler = Handler(Looper.getMainLooper())

    private fun scheduleWidgetUpdate() {
        // Обновления могут идти с любого потока, но Handler привязан к main looper.
        widgetUpdateHandler.removeCallbacksAndMessages(null)
        widgetUpdateHandler.postDelayed(
            Runnable {
                // updateAll — suspend-функция, поэтому вызываем её из корутины.
                runBlocking {
                    runCatching { TodoWidget().updateAll(context) }
                }
            },
            WIDGET_UPDATE_DEBOUNCE_MS,
        )
    }

    fun observeForDate(date: LocalDate): Flow<List<Task>> =
        dao.observeForDate(Task.startOfDayMillis(date)).map { list -> list.map(Task::fromEntity) }

    fun observeBetween(from: LocalDate, to: LocalDate): Flow<Map<LocalDate, List<Task>>> =
        dao.observeBetween(Task.startOfDayMillis(from), Task.startOfDayMillis(to))
            .map { entities -> entities.map(Task::fromEntity).groupBy(Task::date) }

    suspend fun getTask(id: Long): Task? = dao.getById(id)?.let(Task::fromEntity)

    /** Блокирующая выборка для виджета (вызывается из provideGlance на Dispatchers.IO). */
    suspend fun getTasksForDateBlocking(date: LocalDate): List<Task> =
        observeForDate(date).first()

    companion object {
        /** Минимальный интервал между вызовами виджета при частых мутациях. */
        private const val WIDGET_UPDATE_DEBOUNCE_MS = 350L
    }

    suspend fun addTask(title: String, description: String, date: LocalDate, timeMillis: Long?, priority: Int) {
        val dateMillis = Task.startOfDayMillis(date)
        val nextOrder = (dao.maxSortOrder(dateMillis) ?: 0f) + 1f
        dao.insert(
            TaskEntity(
                title = title.trim(),
                description = description.trim(),
                dateMillis = dateMillis,
                timeMillis = timeMillis,
                priority = priority,
                sortOrder = nextOrder,
            ),
        )
        notifyWidgets()
    }

    suspend fun updateTask(task: Task, timeMillis: Long?) {
        val entity = dao.getById(task.id) ?: return
        val dateMillis = Task.startOfDayMillis(task.date)
        val nextOrder = if (entity.dateMillis == dateMillis) {
            entity.sortOrder
        } else {
            (dao.maxSortOrder(dateMillis) ?: 0f) + 1f
        }
        dao.update(
            entity.copy(
                title = task.title.trim(),
                description = task.description.trim(),
                dateMillis = dateMillis,
                timeMillis = timeMillis,
                priority = task.priority.ordinal,
                sortOrder = nextOrder,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        notifyWidgets()
    }

    suspend fun toggleDone(id: Long) {
        val entity = dao.getById(id) ?: return
        dao.setDone(id, !entity.isDone, System.currentTimeMillis())
        notifyWidgets()
    }

    suspend fun moveTask(id: Long, newDate: LocalDate) {
        val entity = dao.getById(id) ?: return
        val dateMillis = Task.startOfDayMillis(newDate)
        if (entity.dateMillis == dateMillis) return
        dao.update(
            entity.copy(
                dateMillis = dateMillis,
                sortOrder = (dao.maxSortOrder(dateMillis) ?: 0f) + 1f,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        notifyWidgets()
    }

    suspend fun deleteTask(id: Long) {
        val entity = dao.getById(id) ?: return
        dao.delete(entity)
        notifyWidgets()
    }

    private fun notifyWidgets() {
        // Обновление виджета не должно ронять приложение, если рендер не удался
        // (например, виджет удалён с рабочего стола или хост недоступен).
        // При частых мутациях обновление coalesce-ится через scheduleWidgetUpdate().
        scheduleWidgetUpdate()
    }
}
