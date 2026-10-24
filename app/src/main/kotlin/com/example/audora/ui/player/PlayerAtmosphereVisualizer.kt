package com.example.audora.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.audora.LocalArtworkColor
import com.example.audora.LocalPlayerConnection
import com.example.audora.constants.PlayerAtmosphereVisualizerKey
import com.example.audora.utils.rememberPreference
import kotlinx.coroutines.flow.collectLatest

/** A quiet, spectrum-driven accent that sits behind the expanded player's controls. */
@Composable
internal fun PlayerAtmosphereVisualizer(
    isPlaying: Boolean,
    progressProvider: () -> Float,
    modifier: Modifier = Modifier,
) {
    val connection = LocalPlayerConnection.current
    val (enabled) = rememberPreference(PlayerAtmosphereVisualizerKey, defaultValue = true)
    val accent = LocalArtworkColor.current
    val levels = remember { FloatArray(32) }
    val frame = remember { mutableLongStateOf(0L) }

    LaunchedEffect(enabled, isPlaying, connection) {
        if (!enabled || !isPlaying || connection == null) {
            levels.fill(0f)
            frame.longValue++
            return@LaunchedEffect
        }
        snapshotFlow { progressProvider() > 0.52f }.collectLatest { visible ->
            if (!visible) return@collectLatest
            while (true) {
                val now = androidx.compose.runtime.withFrameNanos { it }
                val spectrum = connection.service.spectrumOf(connection.player)
                if (spectrum == null || !spectrum.read(connection.player.currentPosition, levels)) {
                    levels.fill(0f)
                }
                frame.longValue = now
            }
        }
    }

    if (!enabled) return
    Canvas(modifier = modifier.fillMaxSize()) {
        frame.longValue // invalidate only while the actual spectrum is being sampled
        val count = 24
        val gap = size.width * 0.007f
        val barWidth = (size.width * 0.58f - gap * (count - 1)) / count
        val totalWidth = count * barWidth + (count - 1) * gap
        val left = (size.width - totalWidth) / 2f
        val baseline = size.height * 0.88f
        val maxHeight = size.height * 0.095f
        val brush = Brush.verticalGradient(
            colors = listOf(accent.copy(alpha = 0.02f), accent.copy(alpha = 0.42f)),
            startY = baseline - maxHeight,
            endY = baseline,
        )
        for (i in 0 until count) {
            val sourceIndex = i * (levels.lastIndex) / (count - 1)
            val level = levels[sourceIndex].coerceIn(0f, 1f)
            val idle = 0.035f + 0.018f * kotlin.math.sin(i * 0.7f)
            val height = maxHeight * maxOf(idle, level * 0.8f)
            val x = left + i * (barWidth + gap) + barWidth / 2f
            drawLine(
                brush = brush,
                start = Offset(x, baseline - height),
                end = Offset(x, baseline),
                strokeWidth = barWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}
