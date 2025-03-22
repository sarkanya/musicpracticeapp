package com.adammusic.practicelog.ui.screens.active

import com.adammusic.practicelog.utils.MetronomeUtils.calculateBeatInterval
import kotlin.test.Test
import kotlin.test.assertEquals

class MetronomeCalculationsTest {
    @Test
    fun `test calculateBeatInterval for quarter notes`() {
        // 60 BPM = 1 klikk/mp = 1000ms
        assertEquals(1000L, calculateBeatInterval(60, 4))

        // 120 BPM = 2 klikk/mp = 500ms
        assertEquals(500L, calculateBeatInterval(120, 4))

        // 30 BPM = 0.5 klikk/mp = 2000ms
        assertEquals(2000L, calculateBeatInterval(30, 4))
    }

    @Test
    fun `test calculateBeatInterval for eighth notes`() {
        // 60 BPM (4/4) = 120 BPM (8/8) = 500ms
        assertEquals(500L, calculateBeatInterval(60, 8))

        // 120 BPM (4/4) = 240 BPM (8/8) = 250ms
        assertEquals(250L, calculateBeatInterval(120, 8))
    }
}