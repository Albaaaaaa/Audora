package com.example.audora.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.navigation.NavController
import com.example.audora.LocalPlayerConnection
import com.example.audora.crossmix.TransitionEditorSettings
import com.example.audora.crossmix.transitionEditorKey
import com.example.audora.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlin.math.abs
import kotlin.math.sin

private val Green = Color(0xFF8BEA63)
private val Panel = Color(0xFF151515)
private val Muted = Color(0xFF9C9C9C)

@Composable
fun TransitionEditorScreen(navController: NavController) {
    val connection = LocalPlayerConnection.current
    val context = LocalContext.current
    val player = connection?.player
    val queueState by (connection?.queueSnapshot?.collectAsState() ?: remember { mutableStateOf(null) })
    val current = player?.currentMediaItem
    val next = remember(queueState, player?.currentMediaItemIndex, player?.mediaItemCount) {
        player?.let { p ->
            val idx = if (p.repeatMode == PlayerRepeatOne) p.currentMediaItemIndex else p.nextMediaItemIndex
            if (idx != C.INDEX_UNSET && idx in 0 until p.mediaItemCount) p.getMediaItemAt(idx) else null
        }
    }
    val prefKey = remember(current?.mediaId, next?.mediaId) {
        transitionEditorKey(current?.mediaId.orEmpty(), next?.mediaId.orEmpty())
    }
    var bars by remember(prefKey) { mutableStateOf("8 bars") }
    var volume by remember(prefKey) { mutableStateOf("Smooth crossfade") }
    var eq by remember(prefKey) { mutableStateOf("Center bass swap") }
    var effects by remember(prefKey) { mutableStateOf("None") }
    var preset by remember(prefKey) { mutableStateOf("Custom") }
    var loaded by remember(prefKey) { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var playing by remember { mutableStateOf(false) }
    var drag by remember { mutableFloatStateOf(0f) }
    var outAnalysis by remember(current?.mediaId) { mutableStateOf<com.example.audora.crossmix.TrackAnalysis?>(null) }
    var inAnalysis by remember(next?.mediaId) { mutableStateOf<com.example.audora.crossmix.TrackAnalysis?>(null) }

    LaunchedEffect(current?.mediaId, next?.mediaId) {
        outAnalysis = null
        inAnalysis = null
        if (current != null && next != null && connection != null) {
            val result = withContext(Dispatchers.IO) {
                connection.service.analyzeTransitionTrack(current, wholeTrack = false) to
                    connection.service.analyzeTransitionTrack(next, wholeTrack = false)
            }
            outAnalysis = result.first
            inAnalysis = result.second
        }
    }

    LaunchedEffect(prefKey) {
        val saved = TransitionEditorSettings.decode(context.dataStore.data.first()[prefKey])
        if (saved != null) {
            bars = "${saved.bars} bars"; volume = saved.volume; eq = saved.eq; effects = saved.effect; preset = saved.preset
        }
        loaded = true
    }
    LaunchedEffect(playing) {
        if (playing) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(12_000, easing = LinearEasing))
            playing = false
        }
    }

    Column(
        Modifier.fillMaxSize().background(Color.Black)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Cancel", color = Color.White, fontSize = 15.sp, modifier = Modifier.clickable { navController.popBackStack() })
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Edit Transition", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("BETA", color = Green, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.5.sp)
            }
            Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                modifier = Modifier.clip(CircleShape).background(Green).clickable {
                    scope.launch {
                        context.dataStore.edit {
                            it[prefKey] = TransitionEditorSettings(
                                bars = bars.substringBefore(' ').toIntOrNull() ?: 8,
                                volume = volume, eq = eq, effect = effects, preset = preset,
                            ).encode()
                        }
                        navController.popBackStack()
                    }
                }.padding(horizontal = 16.dp, vertical = 9.dp))
        }

        TrackSummary("OUTGOING", current, player?.duration ?: C.TIME_UNSET, outAnalysis?.bpm, outAnalysis?.key, Green)
        Spacer(Modifier.height(14.dp))

        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TRANSITION PREVIEW", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text("DRAG TO ALIGN", color = Muted.copy(alpha = .75f), fontSize = 9.sp)
            }
            Spacer(Modifier.height(12.dp))
            WaveformRow(Color(0xFFFFC857), progress.value, drag, Modifier.fillMaxWidth().height(64.dp)
                .pointerInput(Unit) { detectHorizontalDragGestures { _, amount -> drag = (drag + amount / 180f).coerceIn(-1f, 1f) } })
            Box(Modifier.fillMaxWidth().height(38.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("VOL", color = Color(0xFFFFC857), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("EQ", color = Color(0xFFB59BFF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(if (playing) "■" else "▶", color = Green, fontSize = 17.sp,
                        modifier = Modifier.clip(CircleShape).clickable { playing = !playing }.padding(6.dp))
                }
            }
            WaveformRow(Color(0xFF9E86FF), progress.value, -drag, Modifier.fillMaxWidth().height(64.dp)
                .pointerInput(Unit) { detectHorizontalDragGestures { _, amount -> drag = (drag - amount / 180f).coerceIn(-1f, 1f) } })
            Spacer(Modifier.height(12.dp))
            EditorDropdown(bars, listOf("2 bars", "4 bars", "8 bars", "16 bars"), { bars = it }, Modifier.align(Alignment.CenterHorizontally))
        }

        Spacer(Modifier.height(14.dp))
        TrackSummary("INCOMING", next, C.TIME_UNSET, inAnalysis?.bpm, inAnalysis?.key, Color(0xFFB59BFF))
        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EditorDropdown("VOL · $volume", listOf("Smooth crossfade", "Overlap", "Cut"), { volume = it; preset = "Custom" }, Modifier.weight(1f))
            EditorDropdown("EQ · $eq", listOf("None", "Center bass swap", "End bass swap", "3 band fade"), { eq = it; preset = "Custom" }, Modifier.weight(1f))
            EditorDropdown("FX · $effects", listOf("None", "High-pass filter", "Low-pass filter"), { effects = it; preset = "Custom" }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(18.dp))
        Text("QUICK PRESETS", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Custom", "Auto", "Fade", "Rise", "Blend").forEach { choice ->
                Text(choice, color = if (preset == choice) Color.Black else Color.White,
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f).clip(CircleShape)
                        .background(if (preset == choice) Green else Panel).clickable {
                            preset = choice
                            when (choice) {
                                "Fade" -> { volume = "Smooth crossfade"; eq = "None"; effects = "None" }
                                "Rise" -> { volume = "Overlap"; eq = "3 band fade"; effects = "High-pass filter" }
                                "Blend" -> { volume = "Smooth crossfade"; eq = "Center bass swap"; effects = "Low-pass filter" }
                            }
                        }.padding(vertical = 11.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        if (current == null || next == null) {
            Spacer(Modifier.height(18.dp))
            Text("Put two songs in the player queue to edit a transition for that pair.", color = Muted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(28.dp))
    }
}

private const val PlayerRepeatOne = 1

@Composable
private fun TrackSummary(label: String, item: MediaItem?, duration: Long, bpm: Float?, key: String?, tint: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = .18f)), contentAlignment = Alignment.Center) {
            Text("♫", color = tint, fontSize = 24.sp)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
            Text(item?.mediaMetadata?.title?.toString() ?: "No track selected", color = Color.White, fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item?.mediaMetadata?.artist?.toString() ?: "Play or queue a song first", color = Muted, fontSize = 12.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Badge(bpm?.let { "${it.toInt()} BPM" } ?: "BPM —", tint)
                Badge(key ?: "Key —", Color(0xFFB59BFF))
            }
            Text(formatTrackDuration(duration), color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@Composable
private fun Badge(text: String, color: Color) {
    Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(CircleShape).background(color.copy(alpha = .13f)).padding(horizontal = 7.dp, vertical = 4.dp))
}

private fun formatTrackDuration(ms: Long): String = if (ms <= 0 || ms == C.TIME_UNSET) "—" else "%d:%02d".format(ms / 60_000, (ms / 1_000) % 60)

@Composable
private fun EditorDropdown(value: String, options: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Text(value, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clip(CircleShape).background(Panel).clickable { expanded = true }.padding(horizontal = 11.dp, vertical = 10.dp))
        DropdownMenu(expanded, onDismissRequest = { expanded = false }, properties = PopupProperties(focusable = true), modifier = Modifier.background(Panel)) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option, color = Color.White, fontSize = 13.sp) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}

@Composable
private fun WaveformRow(color: Color, progress: Float, shift: Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val mid = size.height / 2
        val count = (size.width / 5.dp.toPx()).toInt().coerceAtLeast(1)
        val overlapStart = size.width * (.39f + shift * .08f)
        val overlapEnd = size.width * (.65f + shift * .08f)
        drawRoundRect(color.copy(alpha = .09f), topLeft = Offset(overlapStart, 1f), size = androidx.compose.ui.geometry.Size(overlapEnd - overlapStart, size.height - 2f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f))
        for (i in 0..count) {
            val x = i * size.width / count
            val beat = i % 4 == 0
            drawLine(Color.White.copy(alpha = if (beat) .12f else .045f), Offset(x, 0f), Offset(x, size.height), strokeWidth = if (beat) 1.2f else .6f)
            val envelope = (.18f + .82f * abs(sin((i * 12.9898f + shift * 4f) * 2.1f))) * (if (i < count * .18f) .55f else 1f)
            val h = size.height * .44f * envelope
            drawLine(color.copy(alpha = .42f + .45f * envelope), Offset(x, mid - h), Offset(x, mid + h), strokeWidth = 2.4f)
        }
        val px = size.width * progress.coerceIn(0f, 1f)
        drawLine(Green, Offset(px, 0f), Offset(px, size.height), strokeWidth = 2.dp.toPx())
        val path = Path().apply {
            moveTo(0f, mid)
            for (i in 0..60) {
                val t = i / 60f
                val curve = when {
                    color.red > .8f -> (1f - t) * (1f - t)
                    else -> t * t
                }
                lineTo(size.width * t, mid + (if (color.red > .8f) -1 else 1) * curve * size.height * .22f)
            }
        }
        drawPath(path, Color.White.copy(alpha = .62f), style = Stroke(width = 1.4.dp.toPx()))
    }
}
