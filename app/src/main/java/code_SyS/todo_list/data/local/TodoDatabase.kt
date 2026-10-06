package code_SyS.todo_list.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class], version = 1, exportSchema = true)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var instance: TodoDatabase? = null

        fun get(context: Context): TodoDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo.db",
                ).build().also { instance = it }
            }
    }
}
