package com.adammusic.practicelog.utils

object MetronomeUtils {
    fun calculateBeatInterval(bpm: Int, noteValue: Int): Long {
        val quarterNoteMs = 60000L / bpm

        return when (noteValue) {
            4 -> quarterNoteMs
            8 -> quarterNoteMs / 2
            2 -> quarterNoteMs * 2
            16 -> quarterNoteMs / 4
            else -> quarterNoteMs
        }
    }
}