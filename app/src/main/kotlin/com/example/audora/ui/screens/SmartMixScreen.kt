package com.example.audora.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.audora.LocalPlayerConnection
import com.example.audora.crossmix.SmartMixArc
import com.example.audora.crossmix.SmartMixCandidate
import com.example.audora.crossmix.SmartMixPlanner
import com.example.audora.ui.player.models.QueueItemData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val SmartGreen = Color(0xFF8BEA63)
private val SmartPanel = Color(0xFF151515)
private val SmartMuted = Color(0xFF9B9B9B)

@Composable
fun SmartMixScreen(navController: NavController) {
    val connection = LocalPlayerConnection.current
    if (connection == null) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("Player isn't ready yet", color = Color.White)
        }
        return
    }
    val snapshot by connection.queueSnapshot.collectAsState()
    val shuffle by connection.shuffleModeEnabled.collectAsState()
    var arc by remember { mutableStateOf(SmartMixArc.Peak) }
    var working by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("Choose an energy flow for the next songs in your queue.") }
    var result by remember { mutableStateOf<List<SmartMixCandidate>>(emptyList()) }
    var analyzeJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    val upcoming = remember(snapshot) {
        snapshot.items.drop((snapshot.currentIndex + 1).coerceAtLeast(0))
    }
    val fixedNext = remember(upcoming) { upcoming.takeWhile { it.isPlayNext } }
    val mixable = remember(upcoming) { upcoming.drop(fixedNext.size).take(12) }

    Column(Modifier.fillMaxSize().background(Color.Black)
        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
        .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹  Back", color = Color.White, fontSize = 15.sp, modifier = Modifier.clickable { navController.popBackStack() })
            Text("SMART MIX", color = SmartMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp,
                modifier = Modifier.weight(1f).padding(end = 50.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        Text("Make the queue\nflow like a set.", color = Color.White, fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
        Text("Audora matches energy, tempo and musical key while keeping your current song untouched.",
            color = SmartMuted, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 10.dp, bottom = 22.dp))

        EnergyCurve(arc, Modifier.fillMaxWidth().height(115.dp).clip(RoundedCornerShape(20.dp)).background(SmartPanel).padding(12.dp))
        Spacer(Modifier.height(18.dp))
        Text("PICK THE ENERGY ARC", color = SmartMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(10.dp))
        SmartMixArc.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                row.forEach { option ->
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                        .background(if (arc == option) SmartGreen.copy(alpha = .15f) else SmartPanel)
                        .clickable(enabled = !working) { arc = option }
                        .padding(horizontal = 14.dp, vertical = 13.dp)) {
                        Text(option.title, color = if (arc == option) SmartGreen else Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(option.subtitle, color = SmartMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(5.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("UP NEXT", color = SmartMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Text("${upcoming.size} songs", color = SmartMuted, fontSize = 11.sp)
        }
        Spacer(Modifier.height(10.dp))
        if (upcoming.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SmartPanel).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Your queue is waiting for its first tracks", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Add at least two songs, then Smart Mix can arrange the energy for you.", color = SmartMuted, fontSize = 13.sp, lineHeight = 18.sp)
                Text("Find music →", color = SmartGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate(Screens.Search.route) }.padding(top = 2.dp))
            }
        } else {
            val shown = if (result.isNotEmpty()) result.map { it.uid } else mixable.map { it.uid }
            val displayItems = (fixedNext + shown.mapNotNull { uid -> upcoming.firstOrNull { it.uid == uid } })
            displayItems.take(8).forEachIndexed { index, item ->
                SmartTrackRow(index + 1, item, result.firstOrNull { it.uid == item.uid }?.energy)
                Spacer(Modifier.height(8.dp))
            }
            if (fixedNext.isNotEmpty()) {
                Text("Play Next songs stay at the front of the queue.", color = SmartMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        if (shuffle) {
            Text("Turn off Shuffle to arrange the queue in a predictable order.", color = Color(0xFFFFC857), fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp))
        }
        Text(status, color = SmartMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 14.dp, bottom = 12.dp))
        Row(Modifier.fillMaxWidth().height(54.dp).clip(CircleShape)
            .background(if (working || mixable.size < 2 || shuffle) Color(0xFF3A3A3A) else SmartGreen)
            .clickable(enabled = working || (mixable.size >= 2 && !shuffle)) {
                if (working) {
                    analyzeJob?.cancel()
                } else analyzeJob = scope.launch {
                    working = true
                    result = emptyList()
                    progress = 0
                    total = mixable.size
                    status = "Analyzing tracks…"
                    try {
                        val player = connection.player
                        val initialCurrentId = player.currentMediaItem?.mediaId
                        val mapped = mixable.mapNotNull { queueItem ->
                            (0 until player.mediaItemCount).firstOrNull { player.getMediaItemAt(it).mediaId == queueItem.mediaId }
                                ?.let { queueItem to player.getMediaItemAt(it) }
                        }
                        val candidates = ArrayList<SmartMixCandidate>(mapped.size)
                        mapped.forEachIndexed { index, (item, mediaItem) ->
                            status = "Reading ${index + 1} of ${mapped.size}: ${item.title.ifBlank { "track" }}"
                            val analysis = withContext(Dispatchers.IO) {
                                connection.service.analyzeTransitionTrack(mediaItem, wholeTrack = false)
                            }
                            candidates += SmartMixCandidate(item.uid, item.title, item.artist, index, analysis)
                            progress = index + 1
                        }
                        if (connection.player.currentMediaItem?.mediaId != initialCurrentId) {
                            status = "The current song changed while analyzing; build the mix again."
                        } else if (candidates.size < 2) {
                            status = "Couldn't find enough playable queue tracks."
                        } else {
                            val ordered = SmartMixPlanner.order(candidates, arc)
                            val initialAnchorUid = fixedNext.lastOrNull()?.uid ?: snapshot.items.getOrNull(snapshot.currentIndex)?.uid
                            if (initialAnchorUid == null) {
                                status = "Start a song first, then build a mix for what’s next."
                            } else {
                                var anchorUid: Int = initialAnchorUid
                                ordered.forEach { candidate ->
                                    connection.moveQueueItem(candidate.uid, anchorUid)
                                    anchorUid = candidate.uid
                                }
                                result = ordered
                                val analyzed = ordered.count { it.analysis != null }
                                status = "Smart Mix ready · ${ordered.size} songs arranged · $analyzed analyzed"
                            }
                        }
                    } catch (cancelled: CancellationException) {
                        status = "Mix cancelled."
                        throw cancelled
                    } catch (error: Exception) {
                        status = "Couldn't finish the mix: ${error.message ?: "try again"}"
                    } finally {
                        working = false
                    }
                }
            }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (working) {
                CircularProgressIndicator(progress = { if (total > 0) progress.toFloat() / total else 0f }, color = SmartGreen,
                    trackColor = Color.White.copy(alpha = .18f), strokeWidth = 2.dp, modifier = Modifier.size(21.dp))
                Text("  Stop", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                Text(if (result.isNotEmpty()) "Remix this order" else "Build Smart Mix", color = if (mixable.size >= 2 && !shuffle) Color.Black else Color.White,
                    fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SmartTrackRow(number: Int, item: QueueItemData, energy: Float?) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(SmartPanel).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(number.toString().padStart(2, '0'), color = SmartMuted, fontSize = 11.sp, modifier = Modifier.width(27.dp))
        AsyncImage(model = item.artworkUri, contentDescription = null, modifier = Modifier.size(42.dp).clip(RoundedCornerShape(9.dp)))
        Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
            Text(item.title.ifBlank { "Unknown title" }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.artist.ifBlank { "Unknown artist" }, color = SmartMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(energy?.let { "${(it * 100).toInt()}%" } ?: "—", color = energy?.let { SmartGreen } ?: SmartMuted,
            fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EnergyCurve(arc: SmartMixArc, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val points = 36
        val path = Path()
        for (i in 0..points) {
            val x = i.toFloat() / points
            val e = SmartMixPlanner.targetEnergy(arc, i, points + 1)
            val y = size.height * (1f - e)
            if (i == 0) path.moveTo(x * size.width, y) else path.lineTo(x * size.width, y)
        }
        for (i in 1..3) {
            val y = size.height * i / 4
            drawLine(Color.White.copy(alpha = .07f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        drawPath(path, SmartGreen, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        val p = when (arc) {
            SmartMixArc.Chill -> listOf(.35f, .25f, .18f, .12f)
            SmartMixArc.Balanced -> listOf(.52f, .52f, .52f, .52f)
            SmartMixArc.Build -> listOf(.15f, .35f, .57f, .82f)
            SmartMixArc.Peak -> listOf(.18f, .56f, .83f, .5f)
        }
        p.forEachIndexed { index, energy ->
            val x = size.width * index / (p.size - 1)
            drawCircle(SmartGreen, radius = 4.dp.toPx(), center = Offset(x, size.height * (1f - energy)))
        }
    }
}
