package dev.taha.habits.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreaksTest {
    private val today = LocalDate.of(2026, 3, 10)
    private fun daysAgo(vararg n: Long) = n.map { today.minusDays(it) }.toSet()

    @Test
    fun `current streak counts back from today`() {
        assertEquals(3, Streaks.current(daysAgo(0, 1, 2, 5), today))
    }

    @Test
    fun `current streak is kept when today is not done yet`() {
        assertEquals(2, Streaks.current(daysAgo(1, 2), today))
    }

    @Test
    fun `current streak is zero after a missed day`() {
        assertEquals(0, Streaks.current(daysAgo(2, 3, 4), today))
        assertEquals(0, Streaks.current(emptySet(), today))
    }

    @Test
    fun `longest streak finds the best run`() {
        assertEquals(4, Streaks.longest(daysAgo(0, 1, 10, 11, 12, 13, 20)))
        assertEquals(1, Streaks.longest(daysAgo(5)))
        assertEquals(0, Streaks.longest(emptySet()))
    }

    @Test
    fun `longest streak handles month boundaries`() {
        val days = setOf(LocalDate.of(2026, 2, 27), LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 1))
        assertEquals(3, Streaks.longest(days))
    }

    @Test
    fun `completion rate ignores days before the habit existed`() {
        // created 4 days ago -> 5 days in the window, 4 done
        val rate = Streaks.completionRate(daysAgo(0, 1, 2, 3), today, createdOn = today.minusDays(4))
        assertEquals(0.8f, rate, 0.0001f)
    }

    @Test
    fun `completion rate over a full 30 day window`() {
        val done = (0L until 15L).map { today.minusDays(it * 2) }.toSet()
        assertEquals(0.5f, Streaks.completionRate(done, today, createdOn = today.minusDays(100)), 0.0001f)
    }

    @Test
    fun `last days are ordered oldest first and end today`() {
        val days = Streaks.lastDays(today, 7)
        assertEquals(7, days.size)
        assertEquals(today.minusDays(6), days.first())
        assertEquals(today, days.last())
    }
}
