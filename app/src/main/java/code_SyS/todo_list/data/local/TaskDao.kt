package code_SyS.todo_list.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query(
        "SELECT * FROM tasks WHERE dateMillis = :dateMillis " +
            "ORDER BY isDone ASC, sortOrder ASC, timeMillis IS NULL, timeMillis ASC, createdAt ASC",
    )
    fun observeForDate(dateMillis: Long): Flow<List<TaskEntity>>

    @Query(
        "SELECT * FROM tasks WHERE dateMillis BETWEEN :fromMillis AND :toMillis " +
            "ORDER BY dateMillis ASC, isDone ASC, sortOrder ASC",
    )
    fun observeBetween(fromMillis: Long, toMillis: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    @Query("SELECT MAX(sortOrder) FROM tasks WHERE dateMillis = :dateMillis")
    suspend fun maxSortOrder(dateMillis: Long): Float?

    @Query("UPDATE tasks SET isDone = :isDone, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setDone(id: Long, isDone: Boolean, updatedAt: Long)

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)
}
