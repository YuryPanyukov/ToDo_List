package code_SyS.todo_list.data

import android.app.Application
import code_SyS.todo_list.data.local.TodoDatabase

class TodoApp : Application() {

    val repository: TaskRepository by lazy {
        TaskRepository(TodoDatabase.get(this).taskDao(), this)
    }
}
