package com.example.audora.ui.screens.recap

import android.content.Context
import androidx.compose.runtime.Immutable
import com.example.audora.db.MusicDatabase
import com.example.audora.db.entities.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Immutable
internal data class CapsuleTrack(val title: String, val artist: String, val artwork: String?, val minutes: Long)

@Immutable
internal data class SoundCapsuleData(
    val from: LocalDate,
    val to: LocalDate,
    val listeningMs: Long,
    val plays: Int,
    val uniqueSongs: Int,
    val uniqueArtists: Int,
    val peakHour: Int?,
    val topTracks: List<CapsuleTrack>,
    val topGenres: List<String>,
) {
    val hours: Int get() = (listeningMs / 3_600_000L).toInt()
    val minutes: Int get() = ((listeningMs / 60_000L) % 60).toInt()
    val aura: String get() = when {
        peakHour == null -> "Finding your sound"
        peakHour >= 22 || peakHour < 5 -> "Midnight Dreamer"
        uniqueArtists >= 12 && uniqueSongs >= 20 -> "Curious Explorer"
        peakHour in 6..11 -> "Daylight Listener"
        peakHour in 17..21 -> "Golden Hour Soul"
        else -> "Easygoing Listener"
    }
    val mood: String get() = when {
        peakHour == null -> "A new week, a fresh soundtrack"
        peakHour >= 22 || peakHour < 5 -> "Late-night calm"
        peakHour in 6..11 -> "Morning reset"
        peakHour in 17..21 -> "Evening unwind"
        else -> "In your own rhythm"
    }
}

internal object SoundCapsuleRepository {
    suspend fun load(database: MusicDatabase, context: Context, today: LocalDate = LocalDate.now()): SoundCapsuleData = withContext(Dispatchers.IO) {
        val end = today.plusDays(1)
        val start = today.minusDays(6)
        val events = database.recapEvents(start.toStoredMillis(), end.toStoredMillis())
        val ids = events.mapTo(LinkedHashSet()) { it.songId }.toList()
        val songs = ids.chunked(500).flatMap { database.getSongsByIds(it) }.associateBy { it.id }
        val minutesBySong = events.groupBy { it.songId }.mapValues { (_, rows) -> rows.sumOf { it.playTime.coerceAtLeast(0L) } / 60_000L }
        val hourCounts = IntArray(24)
        var listening = 0L
        events.forEach { event ->
            listening += event.playTime.coerceAtLeast(0L)
            val hour = storedMillisToDateTime(event.timestamp).hour
            hourCounts[hour]++
        }
        val top = minutesBySong.entries.sortedByDescending { it.value }.take(5).mapNotNull { (id, minutes) ->
            val song = songs[id] ?: return@mapNotNull null
            CapsuleTrack(
                title = song.song.title,
                artist = song.artists.joinToString(", ") { it.name }.ifBlank { song.song.albumName ?: "Unknown artist" },
                artwork = song.song.thumbnailUrl,
                minutes = minutes,
            )
        }
        data class ArtistTotals(val id: String, val name: String, var thumbnail: String?, var timeMs: Long = 0L, var plays: Int = 0)
        val artistTotals = linkedMapOf<String, ArtistTotals>()
        events.forEach { event ->
            val song: Song = songs[event.songId] ?: return@forEach
            song.artists.distinctBy { it.name.trim().lowercase() }.forEach artistLoop@ { artist ->
                val key = artist.name.trim().lowercase()
                if (key.isBlank()) return@artistLoop
                val total = artistTotals.getOrPut(key) { ArtistTotals(artist.id, artist.name.trim(), artist.thumbnailUrl) }
                if (total.thumbnail == null) total.thumbnail = artist.thumbnailUrl
                total.timeMs += event.playTime.coerceAtLeast(0L)
                total.plays++
            }
        }
        val recapArtists = artistTotals.values.sortedByDescending { it.timeMs }.take(30).map {
            RecapArtist(it.id, it.name, it.thumbnail, it.timeMs, it.plays, null)
        }
        val genres = RecapGenres.shares(context, recapArtists)?.take(3)?.map { it.name }.orEmpty()
        SoundCapsuleData(
            from = start,
            to = today,
            listeningMs = listening,
            plays = events.size,
            uniqueSongs = events.map { it.songId }.distinct().size,
            uniqueArtists = artistTotals.size,
            peakHour = hourCounts.indices.maxByOrNull { hourCounts[it] }?.takeIf { hourCounts[it] > 0 },
            topTracks = top,
            topGenres = genres,
        )
    }
}
