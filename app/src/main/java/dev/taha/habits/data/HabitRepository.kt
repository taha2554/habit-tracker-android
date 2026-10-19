package dev.taha.habits.data

import dev.taha.habits.domain.Streaks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

/** Everything the list screen needs for one habit. */
data class HabitSummary(
    val habit: Habit,
    val doneToday: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val completion30d: Float,
    val lastWeek: List<Pair<LocalDate, Boolean>>,
)

class HabitRepository(private val dao: HabitDao) {

    /**
     * Combines habits and their check-ins into ready-to-display summaries.
     * Only the last year of checks is loaded; older history doesn't change the current
     * streak in practice and keeps the query small.
     */
    fun summaries(today: LocalDate): Flow<List<HabitSummary>> {
        val since = today.minusDays(365).toEpochDay()
        return combine(dao.observeHabits(), dao.observeChecksSince(since)) { habits, checks ->
            val byHabit = checks.groupBy({ it.habitId }, { LocalDate.ofEpochDay(it.day) })
            habits.map { habit ->
                val days = byHabit[habit.id].orEmpty().toSet()
                HabitSummary(
                    habit = habit,
                    doneToday = today in days,
                    currentStreak = Streaks.current(days, today),
                    longestStreak = Streaks.longest(days),
                    completion30d = Streaks.completionRate(days, today, LocalDate.ofEpochDay(habit.createdOn)),
                    lastWeek = Streaks.lastDays(today).map { it to (it in days) },
                )
            }
        }
    }

    suspend fun add(name: String, emoji: String, today: LocalDate): Long =
        dao.insert(Habit(name = name.trim(), emoji = emoji, createdOn = today.toEpochDay()))

    suspend fun rename(habit: Habit, name: String, emoji: String) =
        dao.update(habit.copy(name = name.trim(), emoji = emoji))

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun setDone(habitId: Long, day: LocalDate, done: Boolean) {
        if (done) dao.check(Check(habitId, day.toEpochDay())) else dao.uncheck(habitId, day.toEpochDay())
    }
}
