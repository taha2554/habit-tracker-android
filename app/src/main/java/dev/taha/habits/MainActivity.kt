package dev.taha.habits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.taha.habits.ui.HabitScreen
import dev.taha.habits.ui.HabitViewModel
import dev.taha.habits.ui.theme.HabitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTheme {
                HabitScreen(viewModel(factory = HabitViewModel.Factory))
            }
        }
    }
}
