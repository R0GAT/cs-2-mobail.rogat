package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * CS2 Audio Synthesizer: Generates authentic procedural tactical audio for
 * weapon fire (AK-47, AWP, M4A4, Deagle), knife swipes, headshot dink chime,
 * bomb beeps, defusal sound, and victory alerts without external audio file dependencies.
 */
object CS2SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 22050
    var soundEnabled = true
    var masterVolume = 0.85f

    fun playAk47() {
        if (!soundEnabled) return
        scope.launch {
            // Sharp crack + heavy low end burst + mechanical metallic decay
            val durationMs = 180
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val random = Random(System.nanoTime())

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = (1.0f - i.toFloat() / numSamples).coerceIn(0f, 1f)
                val noise = (random.nextFloat() * 2f - 1f) * (1f - (i.toFloat() / (numSamples * 0.4f)).coerceIn(0f, 1f))
                val bass = sin(2.0 * Math.PI * (120.0 - t * 200.0) * t).toFloat()
                val metal = sin(2.0 * Math.PI * 480.0 * t).toFloat() * 0.3f
                val sampleVal = ((noise * 0.7f + bass * 0.6f + metal * 0.2f) * env * 32000f * masterVolume).toInt()
                buffer[i] = sampleVal.coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playAwp() {
        if (!soundEnabled) return
        scope.launch {
            // Massive explosive sniper cannon punch with prolonged cavernous rumble
            val durationMs = 380
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val random = Random(System.nanoTime())

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = (1.0f - (i.toFloat() / numSamples)).coerceIn(0f, 1f)
                val punchEnv = (1.0f - (i.toFloat() / (numSamples * 0.15f)).coerceIn(0f, 1f))
                val noise = (random.nextFloat() * 2f - 1f) * punchEnv
                val subBass = sin(2.0 * Math.PI * (80.0 - t * 60.0) * t).toFloat()
                val boom = sin(2.0 * Math.PI * (150.0 / (1.0 + t * 12.0)) * t).toFloat()
                val sampleVal = ((noise * 0.8f + subBass * 0.8f + boom * 0.5f) * env * 32700f * masterVolume).toInt()
                buffer[i] = sampleVal.coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playM4a4() {
        if (!soundEnabled) return
        scope.launch {
            // Tighter, silenced/suppressed or smooth rifle shot
            val durationMs = 140
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val random = Random(System.nanoTime())

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = (1.0f - i.toFloat() / numSamples).coerceIn(0f, 1f)
                val noise = (random.nextFloat() * 2f - 1f) * 0.6f
                val midSnap = sin(2.0 * Math.PI * (220.0 - t * 400.0) * t).toFloat()
                val sampleVal = ((noise * 0.6f + midSnap * 0.5f) * env * 28000f * masterVolume).toInt()
                buffer[i] = sampleVal.coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playDeagle() {
        if (!soundEnabled) return
        scope.launch {
            // Distinct crisp heavy hand-cannon crack
            val durationMs = 220
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val random = Random(System.nanoTime())

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = (1.0f - i.toFloat() / numSamples).coerceIn(0f, 1f)
                val noise = (random.nextFloat() * 2f - 1f) * 0.7f
                val crack = sin(2.0 * Math.PI * 180.0 * t).toFloat()
                val sampleVal = ((noise * 0.6f + crack * 0.7f) * env * 32000f * masterVolume).toInt()
                buffer[i] = sampleVal.coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playKnifeSlash() {
        if (!soundEnabled) return
        scope.launch {
            // Swift metallic air whoosh
            val durationMs = 120
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = sin(Math.PI * i / numSamples).toFloat()
                val whoosh = sin(2.0 * Math.PI * (600.0 + t * 400.0) * t).toFloat()
                val blade = sin(2.0 * Math.PI * 1800.0 * t).toFloat() * 0.3f
                val sampleVal = ((whoosh * 0.7f + blade) * env * 24000f * masterVolume).toInt()
                buffer[i] = sampleVal.coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playHeadshotDink() {
        if (!soundEnabled) return
        scope.launch {
            // Iconic CS helmet hit "DINK" metallic ping
            val durationMs = 250
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = (1.0f - i.toFloat() / numSamples).coerceIn(0f, 1f)
                val tone1 = sin(2.0 * Math.PI * 1960.0 * t).toFloat()
                val tone2 = sin(2.0 * Math.PI * 2940.0 * t).toFloat() * 0.6f
                val tone3 = sin(2.0 * Math.PI * 980.0 * t).toFloat() * 0.4f
                val sampleVal = ((tone1 + tone2 + tone3) * env * 28000f * masterVolume).toInt()
                buffer[i] = sampleVal.coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playBombBeep() {
        if (!soundEnabled) return
        scope.launch {
            // High frequency C4 beep
            val durationMs = 60
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = (1.0f - i.toFloat() / numSamples)
                val beep = sin(2.0 * Math.PI * 1500.0 * t).toFloat()
                val sampleVal = (beep * env * 26000f * masterVolume).toInt()
                buffer[i] = sampleVal.coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playDefuseClick() {
        if (!soundEnabled) return
        scope.launch {
            val durationMs = 80
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            val random = Random(System.nanoTime())
            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val click = sin(2.0 * Math.PI * 900.0 * t).toFloat() + (random.nextFloat() - 0.5f) * 0.5f
                val env = (1f - i.toFloat() / numSamples)
                buffer[i] = (click * env * 22000f * masterVolume).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playCaseTick() {
        if (!soundEnabled) return
        scope.launch {
            val durationMs = 25
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val tick = sin(2.0 * Math.PI * 1800.0 * t).toFloat()
                val env = (1f - i.toFloat() / numSamples)
                buffer[i] = (tick * env * 24000f * masterVolume).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    fun playCaseGoldFanfare() {
        if (!soundEnabled) return
        scope.launch {
            // Golden rare special item chime
            val durationMs = 600
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toFloat() / SAMPLE_RATE
                val freq = if (t < 0.2f) 1046.5 else if (t < 0.4f) 1318.5 else 1568.0
                val chord = sin(2.0 * Math.PI * freq * t).toFloat() + sin(2.0 * Math.PI * (freq * 1.5) * t).toFloat() * 0.4f
                val env = (1f - i.toFloat() / numSamples)
                buffer[i] = (chord * env * 26000f * masterVolume).toInt().coerceIn(-32767, 32767).toShort()
            }
            playPcm(buffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // release after played
            scope.launch {
                kotlinx.coroutines.delay((buffer.size * 1000L / SAMPLE_RATE) + 100L)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // Ignore audio initialization errors gracefully
        }
    }
}
