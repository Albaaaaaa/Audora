package com.example.audora.playback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/** A small, offline rain bed generated in real time so Sleep Mix needs neither a download nor an asset. */
class RainAmbiencePlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private var track: AudioTrack? = null

    @Synchronized
    fun start() {
        if (job?.isActive == true) return
        val sampleRate = 22_050
        val minimum = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(sampleRate / 5)
        val newTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build(),
            )
            .setBufferSizeInBytes(minimum * 3)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        track = newTrack
        newTrack.play()
        job = scope.launch {
            val buffer = ShortArray(1_024)
            var softRain = 0f
            while (isActive) {
                buffer.indices.forEach { index ->
                    val white = Random.nextFloat() * 2f - 1f
                    softRain = softRain * 0.965f + white * 0.035f
                    // Rare, bright droplets over the soft filtered noise make this read as rain.
                    val droplet = if (Random.nextInt(950) < 5) white * 0.55f else 0f
                    buffer[index] = ((softRain * 0.16f + droplet) * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                newTrack.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
            }
        }
    }

    @Synchronized
    fun stop() {
        job?.cancel()
        job = null
        track?.runCatching { pause(); flush(); release() }
        track = null
    }
}
