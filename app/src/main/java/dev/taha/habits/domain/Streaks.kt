package dev.taha.habits.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Streak and progress calculations. Dates are passed in as [LocalDate]s so this file has
 * no Android dependencies and can be unit tested on the JVM.
 */
object Streaks {

    /**
     * Number of consecutive days, ending today, on which the habit was done.
     * If today isn't checked yet the streak still counts from yesterday –
     * the day isn't over, so the streak shouldn't look broken in the morning.
     */
    fun current(doneDates: Set<LocalDate>, today: LocalDate): Int {
        var day = if (today in doneDates) today else today.minusDays(1)
        var count = 0
        while (day in doneDates) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    fun longest(doneDates: Set<LocalDate>): Int {
        if (doneDates.isEmpty()) return 0
        val sorted = doneDates.sorted()
        var best = 1
        var run = 1
        for (i in 1 until sorted.size) {
            run = if (sorted[i - 1].plusDays(1) == sorted[i]) run + 1 else 1
            best = maxOf(best, run)
        }
        return best
    }

    /**
     * Share of the last [days] days (including today) on which the habit was done.
     * Days before the habit was created don't count against it.
     */
    fun completionRate(doneDates: Set<LocalDate>, today: LocalDate, createdOn: LocalDate, days: Int = 30): Float {
        val start = maxOf(today.minusDays((days - 1).toLong()), createdOn)
        if (start.isAfter(today)) return 0f
        // ChronoUnit instead of LocalDate.datesUntil(), which needs API 34 on Android
        val total = ChronoUnit.DAYS.between(start, today).toInt() + 1
        val done = doneDates.count { !it.isBefore(start) && !it.isAfter(today) }
        return done.toFloat() / total
    }

    /** The last [count] days ending with today, oldest first – used for the dot row in the UI. */
    fun lastDays(today: LocalDate, count: Int = 7): List<LocalDate> =
        (count - 1 downTo 0).map { today.minusDays(it.toLong()) }
}
