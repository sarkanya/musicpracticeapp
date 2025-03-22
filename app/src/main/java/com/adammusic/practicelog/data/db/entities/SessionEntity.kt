package com.adammusic.practicelog.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "sessions",
    indices = [Index("practiceId")],
    foreignKeys = [
        ForeignKey(
            entity = PracticeEntity::class,
            parentColumns = ["id"],
            childColumns = ["practiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val practiceId: Int,
    val date: LocalDateTime,
    val duration: Int,
    val startingBpm: Int,
    val achievedBpm: Int,
    val notes: String?
)