package dev.taha.habits.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.taha.habits.HabitApp
import dev.taha.habits.data.Habit
import dev.taha.habits.data.HabitRepository
import dev.taha.habits.data.HabitSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UiState(
    val loading: Boolean = true,
    val habits: List<HabitSummary> = emptyList(),
) {
    val doneToday get() = habits.count { it.doneToday }
}

class HabitViewModel(
    private val repository: HabitRepository,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    val state: StateFlow<UiState> = repository.summaries(today())
        .map { UiState(loading = false, habits = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun add(name: String, emoji: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.add(name, emoji.ifBlank { "✅" }, today()) }
    }

    fun edit(habit: Habit, name: String, emoji: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.rename(habit, name, emoji.ifBlank { habit.emoji }) }
    }

    fun delete(habit: Habit) {
        viewModelScope.launch { repository.delete(habit.id) }
    }

    fun toggle(summary: HabitSummary, day: LocalDate = today()) {
        val done = summary.lastWeek.firstOrNull { it.first == day }?.second ?: summary.doneToday
        viewModelScope.launch { repository.setDone(summary.habit.id, day, !done) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HabitApp
                HabitViewModel(app.repository)
            }
        }
    }
}
