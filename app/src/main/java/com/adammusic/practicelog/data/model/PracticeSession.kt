package com.adammusic.practicelog.data.model

import java.time.LocalDateTime

data class PracticeSession(
    val id: Int = 0,
    val practiceId: Int,
    val date: LocalDateTime = LocalDateTime.now(),
    val duration: Int,
    val startingBpm: Int,
    val achievedBpm: Int,
    val notes: String? = null
)