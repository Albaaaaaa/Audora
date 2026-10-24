package com.example.audora.ui.screens.recap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.audora.LocalDatabase
import com.example.audora.R
import com.example.audora.ui.screens.Screens
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SoundCapsuleScreen(navController: NavController) {
    val database = LocalDatabase.current
    val context = LocalContext.current
    var capsule by remember { mutableStateOf<SoundCapsuleData?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var loadAttempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(database, context, loadAttempt) {
        loading = true
        loadError = false
        try {
            capsule = SoundCapsuleRepository.load(database, context)
        } catch (_: Exception) {
            loadError = true
        } finally {
            loading = false
        }
    }
    val data = capsule
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF08090C)),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("YOUR WEEK IN SOUND", color = Color(0xFFB8A6FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Sound Capsule", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.4).sp)
            Text(
                data?.let { "${it.from.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))} — ${it.to.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))}" }
                    ?: if (loading) "Gathering this week's listening…" else "Your listening, at a glance",
                color = Color.White.copy(alpha = .55f), fontSize = 13.sp,
            )
        }
        if (data == null) {
            item {
                CapsuleCard {
                    when {
                        loading -> {
                            CircularProgressIndicator(color = Color(0xFFB8A6FF), strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
                            Text("Gathering your listening", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Your weekly recap will be ready in a moment.", color = Color.White.copy(alpha = .62f), fontSize = 14.sp)
                        }
                        loadError -> {
                            Text("Couldn't load your Capsule", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text("Your listening history is safe. Try loading the recap again.", color = Color.White.copy(alpha = .62f), fontSize = 14.sp)
                            CapsuleAction("Try again") { loadAttempt++ }
                        }
                        else -> {
                            Text("No listening data yet", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text("Play a few songs in Audora and your weekly highlights will show up here.", color = Color.White.copy(alpha = .62f), fontSize = 14.sp)
                            CapsuleAction("Find music") { navController.navigate(Screens.Search.route) }
                        }
                    }
                }
            }
        } else if (data.plays == 0) {
            item {
                CapsuleCard {
                    Text("Your next week starts with one song.", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Play a few songs in Audora and your Capsule will collect your highlights here.", color = Color.White.copy(alpha = .62f), fontSize = 14.sp)
                    CapsuleAction("Find music") { navController.navigate(Screens.Search.route) }
                }
            }
        } else {
            item {
                CapsuleCard(brush = Brush.linearGradient(listOf(Color(0xFF5B42A6), Color(0xFF173E50)))) {
                    Text("YOUR AURA", color = Color.White.copy(alpha = .72f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
                    Text(data.aura, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-.8).sp)
                    Text(data.mood, color = Color.White.copy(alpha = .8f), fontSize = 14.sp)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CapsuleMetric("LISTENED", "${data.hours}h ${data.minutes}m", Modifier.weight(1f))
                    CapsuleMetric("SONGS", data.uniqueSongs.toString(), Modifier.weight(1f))
                    CapsuleMetric("ARTISTS", data.uniqueArtists.toString(), Modifier.weight(1f))
                }
            }
            item {
                CapsuleCard {
                    Text("Your listening mood", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(data.mood, color = Color(0xFFCABEFF), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    val hour = data.peakHour
                    Text(hour?.let { "You listened most around ${it.toString().padStart(2, '0')}:00" } ?: "Keep listening to discover your rhythm", color = Color.White.copy(alpha = .58f), fontSize = 13.sp)
                    if (data.topGenres.isNotEmpty()) {
                        Text("TOP GENRE", color = Color.White.copy(alpha = .46f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
                        Text(data.topGenres.joinToString("  ·  "), color = Color(0xFFCABEFF), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (data.topTracks.isNotEmpty()) {
                item { Text("On repeat", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp)) }
                itemsIndexed(data.topTracks, key = { index, track -> "${track.title}-$index" }) { index, track ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF15161B)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${index + 1}", color = Color(0xFFB8A6FF), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                        AsyncImage(track.artwork, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(Color.DarkGray))
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(track.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(track.artist, color = Color.White.copy(alpha = .55f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("${track.minutes}m", color = Color.White.copy(alpha = .5f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CapsuleAction(label: String, onClick: () -> Unit) {
    Text(
        label,
        color = Color(0xFF111018),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 5.dp).clip(CircleShape)
            .background(Color(0xFFB8A6FF)).clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
    )
}

@Composable
private fun CapsuleCard(brush: Brush = Brush.linearGradient(listOf(Color(0xFF15151C), Color(0xFF101216))), content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(brush).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
private fun CapsuleMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Color(0xFF15161B)).padding(14.dp)) {
        Text(label, color = Color.White.copy(alpha = .5f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
        Spacer(Modifier.height(5.dp))
        Text(value, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}
