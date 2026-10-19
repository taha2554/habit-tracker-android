# Habit Tracker (Android)

![CI](https://github.com/taha2554/habit-tracker-android/actions/workflows/ci.yml/badge.svg)

A small habit tracker for Android written in **Kotlin** with **Jetpack Compose**, **Room** and **ViewModel + StateFlow**. Check off habits every day and keep your streak going.

## Features

- Add, rename and delete habits, each with an emoji (long-press a card to edit)
- One tap marks today as done; the 7-day row lets you fix earlier days
- Current streak, best streak and 30-day completion rate
- Streak isn't shown as broken until the day is actually over
- Material 3, dynamic colours on Android 12+, dark mode, edge-to-edge
- Works fully offline – all data is stored locally in SQLite via Room

## Architecture

```
ui/          HabitScreen (Compose), HabitViewModel, theme
data/        Room entities (Habit, Check), HabitDao, AppDatabase, HabitRepository
domain/      Streaks – pure Kotlin streak / completion logic
HabitApp     tiny manual DI container (repository lives here)
```

- A check-in is just a row `(habitId, day)` with a composite primary key, so a day can't be checked twice and un-checking is a single delete.
- Dates are stored as `LocalDate.toEpochDay()` numbers – easy to index and compare, no time-zone surprises.
- The repository `combine`s the habits flow and the check-ins flow into ready-to-render `HabitSummary` objects, so the UI never does calculations.
- `Streaks` has no Android imports, so the logic is covered by plain JVM unit tests.

## Build & run

Requirements: Android Studio (Ladybug or newer) or JDK 17 + Android SDK 35.

```bash
./gradlew installDebug          # on a connected device / emulator
./gradlew testDebugUnitTest     # unit tests
```

CI builds the debug APK on every push and uploads it as an artifact.

## Possible next steps

- Daily reminder notification with WorkManager
- Habits that repeat only on some weekdays
- Home-screen widget (Glance)

## License

MIT
