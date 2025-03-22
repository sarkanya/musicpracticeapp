package com.adammusic.practicelog.audio


import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.adammusic.practicelog.R
import kotlinx.coroutines.delay

class MetronomeAudioHandler(
    context: Context
) {
    private var soundPool: SoundPool
    private var tickSound: Int
    private var tickHighSound: Int
    private var soundsLoaded = false
    private var loadCount = 0

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, _, _ ->
            loadCount++
            if (loadCount >= 2) {
                soundsLoaded = true
            }
        }

        tickSound = soundPool.load(context, R.raw.metronome_tick, 1)
        tickHighSound = soundPool.load(context, R.raw.metronome_tick_hi, 1)

        preloadSounds()
    }
    
    private fun preloadSounds() {
        // 0 hangerőn lejátszani a hangokat hogy cachelodjon, különben az első klikk kicsit késve megy
        soundPool.play(tickSound, 0f, 0f, 1, 0, 1f)
        soundPool.play(tickHighSound, 0f, 0f, 1, 0, 1f)
    }

    fun playTick(isAccented: Boolean = false) {
        if (!soundsLoaded) return
        
        val soundId = if (isAccented) tickHighSound else tickSound
        soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }
}

@Composable
fun rememberMetronomeAudioHandler(): MetronomeAudioHandler {
    val context = LocalContext.current
    val audioHandler = remember { MetronomeAudioHandler(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioHandler.release()
        }
    }

    return audioHandler
}