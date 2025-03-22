package com.adammusic.practicelog.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val days: String,
    val hourOfDay: Int,
    val minute: Int,
    val isEnabled: Boolean = true
) 