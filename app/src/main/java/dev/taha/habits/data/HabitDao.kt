package dev.taha.habits.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY id")
    fun observeHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM checks WHERE day >= :fromDay")
    fun observeChecksSince(fromDay: Long): Flow<List<Check>>

    @Query("SELECT day FROM checks WHERE habitId = :habitId")
    suspend fun allDays(habitId: Long): List<Long>

    @Insert
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun check(check: Check)

    @Query("DELETE FROM checks WHERE habitId = :habitId AND day = :day")
    suspend fun uncheck(habitId: Long, day: Long)
}
