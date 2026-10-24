package com.example.audora.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.audora.ui.screens.Screens

@Composable
fun LyricsMomentsScreen(navController: NavController) {
    val context = LocalContext.current
    var moments by remember { mutableStateOf(LyricsMomentStore.all(context)) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF08090C)),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("YOUR COLLECTION", color = Color(0xFFB8A6FF), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Lyrics Moments", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
        }
        if (moments.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF15161B)).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Text("Your lyric collection starts here", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("Play a song, open its lyrics, then long-press a line to save a timestamped moment.", color = Color.White.copy(alpha = .62f), fontSize = 14.sp, lineHeight = 20.sp)
                    Text("Find a song →", color = Color(0xFFB8A6FF), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navController.navigate(Screens.Search.route) }.padding(top = 3.dp))
                }
            }
        }
        items(moments, key = { it.id }) { moment ->
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF332255), Color(0xFF112A35))))
                    .clickable { LyricsMomentShare.share(context, moment) }
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text("“${moment.text}”", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 5, overflow = TextOverflow.Ellipsis)
                Text(moment.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${moment.artist} · ${LyricsMomentShare.timestamp(moment.positionMs)}", color = Color.White.copy(alpha = .62f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("Remove", color = Color(0xFFE5B2C0), fontSize = 11.sp, modifier = Modifier.clickable {
                        LyricsMomentStore.delete(context, moment)
                        moments = LyricsMomentStore.all(context)
                    }.padding(start = 12.dp))
                }
                Text("TAP TO SHARE  ·  AUDORA ✦", color = Color(0xFFD0C2FF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
