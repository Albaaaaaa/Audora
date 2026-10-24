// SleepTimer.kt

package com.example.audora.playback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class SleepTimer(
    private val scope: CoroutineScope,
    var player: Player,
    private val onTimerFinished: (fadeOut: Boolean) -> Unit,
    private val onStopAmbience: () -> Unit,
) : Player.Listener {
    private var sleepTimerJob: Job? = null
    var triggerTime by mutableLongStateOf(-1L)
        private set
    var pauseWhenSongEnd by mutableStateOf(false)
        private set
    var fadeOut by mutableStateOf(true)
        private set
    /** Keeps a gentle rain bed playing once the queue naturally reaches its end. */
    var rainAfterQueueEnds by mutableStateOf(false)
        private set
    val isActive: Boolean
        get() = triggerTime != -1L || pauseWhenSongEnd || rainAfterQueueEnds

    fun start(minute: Int, fadeOut: Boolean = true, rainAfterQueueEnds: Boolean = false) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        onStopAmbience()
        this.fadeOut = fadeOut
        this.rainAfterQueueEnds = rainAfterQueueEnds
        if (minute == -1) {
            pauseWhenSongEnd = true
            triggerTime = -1L
        } else {
            pauseWhenSongEnd = false
            triggerTime = System.currentTimeMillis() + minute.minutes.inWholeMilliseconds
            sleepTimerJob =
                scope.launch {
                    delay(minute.minutes)
                    triggerTime = -1L
                    onTimerFinished(fadeOut)
                }
        }
    }

    fun notifySongTransition() {
        if (pauseWhenSongEnd) {
            pauseWhenSongEnd = false
            onTimerFinished(fadeOut)
        }
    }

    fun clear() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        pauseWhenSongEnd = false
        triggerTime = -1L
        rainAfterQueueEnds = false
        onStopAmbience()
    }

    override fun onMediaItemTransition(
        mediaItem: MediaItem?,
        reason: Int,
    ) {
        if (pauseWhenSongEnd) {
            pauseWhenSongEnd = false
            onTimerFinished(fadeOut)
        }
    }

    override fun onPlaybackStateChanged(
        @Player.State playbackState: Int,
    ) {
        if (playbackState == Player.STATE_ENDED && pauseWhenSongEnd) {
            pauseWhenSongEnd = false
            onTimerFinished(fadeOut)
        }
    }
}
