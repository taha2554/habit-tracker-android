package dev.taha.habits.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.taha.habits.data.Habit
import dev.taha.habits.data.HabitSummary
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(viewModel: HabitViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Habit?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Today")
                        if (state.habits.isNotEmpty()) {
                            Text(
                                "${state.doneToday} of ${state.habits.size} done",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { adding = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add habit")
            }
        },
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            state.habits.isEmpty() -> EmptyState(Modifier.padding(padding))
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding(), bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.habits, key = { it.habit.id }) { summary ->
                    HabitCard(
                        summary = summary,
                        onToggleToday = { viewModel.toggle(summary) },
                        onToggleDay = { day -> viewModel.toggle(summary, day) },
                        onLongPress = { editing = summary.habit },
                    )
                }
            }
        }
    }

    if (adding) {
        HabitDialog(
            title = "New habit",
            onDismiss = { adding = false },
            onSave = { name, emoji ->
                viewModel.add(name, emoji)
                adding = false
            },
        )
    }
    editing?.let { habit ->
        HabitDialog(
            title = "Edit habit",
            initialName = habit.name,
            initialEmoji = habit.emoji,
            onDismiss = { editing = null },
            onSave = { name, emoji ->
                viewModel.edit(habit, name, emoji)
                editing = null
            },
            onDelete = {
                viewModel.delete(habit)
                editing = null
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitCard(
    summary: HabitSummary,
    onToggleToday: () -> Unit,
    onToggleDay: (LocalDate) -> Unit,
    onLongPress: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = onToggleToday, onLongClick = onLongPress),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(summary.habit.emoji, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        summary.habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (summary.currentStreak > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                        )
                        Text(
                            " ${summary.currentStreak} day streak · best ${summary.longestStreak}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                FilledIconToggleButton(checked = summary.doneToday, onCheckedChange = { onToggleToday() }) {
                    Icon(Icons.Default.Check, contentDescription = if (summary.doneToday) "Mark as not done" else "Mark as done")
                }
            }

            Spacer(Modifier.padding(top = 12.dp))
            WeekRow(summary.lastWeek, onToggleDay)

            Spacer(Modifier.padding(top = 12.dp))
            LinearProgressIndicator(progress = { summary.completion30d }, modifier = Modifier.fillMaxWidth())
            Text(
                "${(summary.completion30d * 100).toInt()}% in the last 30 days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun WeekRow(days: List<Pair<LocalDate, Boolean>>, onToggle: (LocalDate) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEach { (day, done) ->
            val color by animateColorAsState(
                if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                label = "dot",
            )
            val label = day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onToggle(day) }
                        .semantics { contentDescription = "$day ${if (done) "done" else "not done"}" },
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🌱", style = MaterialTheme.typography.displayMedium)
        Text("No habits yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            "Tap + to add something small you want to do every day.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val EMOJIS = listOf("✅", "💧", "📚", "🏃", "🧘", "🥗", "😴", "✍️", "🎸", "🧹")

@Composable
private fun HabitDialog(
    title: String,
    initialName: String = "",
    initialEmoji: String = EMOJIS.first(),
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var emoji by rememberSaveable { mutableStateOf(initialEmoji) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 40) name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.padding(top = 12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    EMOJIS.take(5).forEach { EmojiChoice(it, it == emoji) { emoji = it } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    EMOJIS.drop(5).forEach { EmojiChoice(it, it == emoji) { emoji = it } }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, emoji) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@Composable
private fun EmojiChoice(emoji: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                CircleShape,
            ),
        contentPadding = PaddingValues(0.dp),
    ) { Text(emoji, style = MaterialTheme.typography.titleLarge) }
}
