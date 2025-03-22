package com.adammusic.practicelog.data.model

data class Reminder(
    val id: Int = 0,
    val days: List<Int>, // 1-7 representing Monday-Sunday
    val hourOfDay: Int, // 0-23
    val minute: Int, // 0-59
    val isEnabled: Boolean = true
) 