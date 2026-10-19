package dev.taha.habits.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "✅",
    /** Days since epoch (LocalDate.toEpochDay) – simpler to store and compare than strings. */
    val createdOn: Long,
    val archived: Boolean = false,
)

@Entity(
    tableName = "checks",
    primaryKeys = ["habitId", "day"],
    foreignKeys = [
        ForeignKey(entity = Habit::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("habitId")],
)
data class Check(
    val habitId: Long,
    val day: Long,
)
