package dev.taha.habits

import android.app.Application
import dev.taha.habits.data.AppDatabase
import dev.taha.habits.data.HabitRepository

/** Manual dependency container – the app is small enough that a DI framework would be overkill. */
class HabitApp : Application() {
    val repository: HabitRepository by lazy {
        HabitRepository(AppDatabase.create(this).habitDao())
    }
}
