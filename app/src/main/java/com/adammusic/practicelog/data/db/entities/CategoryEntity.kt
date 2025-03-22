package com.adammusic.practicelog.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity (@PrimaryKey(autoGenerate = true) val id: Int, val name: String, val color: Long)