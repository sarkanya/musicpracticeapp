package com.adammusic.practicelog.data.db

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return try {
            value?.let { LocalDateTime.parse(it, formatter) }
        } catch (e: DateTimeParseException) {
            null
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return try {
            date?.format(formatter)
        } catch (e: Exception) {
            null
        }
    }
}