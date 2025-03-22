package com.adammusic.practicelog.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "practices",
    indices = [Index("categoryId")],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PracticeEntity(@PrimaryKey(autoGenerate = true) val id: Int,
                          val name: String,
                          val description: String,
                          val categoryId: Int)