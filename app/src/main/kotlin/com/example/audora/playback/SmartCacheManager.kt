package com.example.audora.playback

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.example.audora.constants.PlaylistSortType
import com.example.audora.constants.SmartCacheEnabledKey
import com.example.audora.constants.SmartCacheSongIdsKey
import com.example.audora.constants.SmartCacheWifiOnlyKey
import com.example.audora.db.MusicDatabase
import com.example.audora.db.entities.Song
import com.example.audora.di.ApplicationScope
import com.example.audora.utils.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns downloads initiated by Audora itself. Manual downloads stay separate, while songs added
 * here get a small persistent marker so Library can present them as one tidy Smart Cache playlist.
 */
@Singleton
class SmartCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope,
    private val database: MusicDatabase,
    private val downloadUtil: DownloadUtil,
) {
    private var started = false

    /** Start observing saved/favourite playlists once per process. */
    @Synchronized
    fun start() {
        if (started) return
        started = true

        scope.launch {
            combine(
                context.dataStore.data,
                database.playlists(PlaylistSortType.CREATE_DATE, true),
            ) { settings, playlists ->
                val enabled = settings[SmartCacheEnabledKey] ?: true
                val wifiOnly = settings[SmartCacheWifiOnlyKey] ?: true
                Triple(enabled, wifiOnly, playlists.map { it.id })
            }.collect { (enabled, wifiOnly, playlistIds) ->
                if (!enabled || !canDownload(wifiOnly)) return@collect
                playlistIds.forEach { playlistId ->
                    cacheSongs(database.playlistSongs(playlistId).first().map { it.song })
                }
            }
        }
    }

    /** Called for both “Play next” and “Add to queue” actions. */
    fun cacheUpNext(items: List<MediaItem>) {
        start()
        if (items.isEmpty()) return
        scope.launch {
            val settings = context.dataStore.data.first()
            if (settings[SmartCacheEnabledKey] != false && canDownload(settings[SmartCacheWifiOnlyKey] ?: true)) {
                cacheMediaItems(items)
            }
        }
    }

    private suspend fun cacheMediaItems(items: List<MediaItem>) {
        val ids = items.map { it.mediaId }.filter { it.isNotBlank() }.distinct()
        if (ids.isEmpty()) return
        remember(ids)
        items.distinctBy { it.mediaId }.forEach { item ->
            if (item.mediaId.isBlank() || downloadUtil.downloads.value.containsKey(item.mediaId)) return@forEach
            enqueue(item.mediaId, item.mediaMetadata.title?.toString().orEmpty())
        }
    }

    private suspend fun cacheSongs(songs: List<Song>) {
        val remoteSongs = songs.filterNot { it.song.isLocal }.distinctBy { it.id }
        if (remoteSongs.isEmpty()) return
        remember(remoteSongs.map { it.id })
        remoteSongs.forEach { song ->
            if (!downloadUtil.downloads.value.containsKey(song.id)) enqueue(song.id, song.song.title)
        }
    }

    private suspend fun remember(ids: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[SmartCacheSongIdsKey] = preferences[SmartCacheSongIdsKey].orEmpty() + ids
        }
    }

    private fun enqueue(id: String, title: String) {
        val request = DownloadRequest.Builder(id, id.toUri())
            .setCustomCacheKey(id)
            .setData(title.ifBlank { "Song" }.toByteArray())
            .build()
        DownloadService.sendAddDownload(context, ExoDownloadService::class.java, request, false)
    }

    private fun canDownload(wifiOnly: Boolean): Boolean {
        if (!wifiOnly) return true
        val connectivity = context.getSystemService<ConnectivityManager>() ?: return false
        return connectivity.activeNetwork != null && !connectivity.isActiveNetworkMetered
    }
}
