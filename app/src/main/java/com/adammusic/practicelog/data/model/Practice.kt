package com.adammusic.practicelog.data.model

data class Practice(
    val id: Int = 0,
    val name: String,
    val description: String,
    val category: Category,
    val sessions: List<PracticeSession> = emptyList()
)