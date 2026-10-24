// NetEaseLyricsProvider.kt

package com.example.audora.lyrics

import android.content.Context
import com.music.directlyrics.NetEase
import com.example.audora.constants.EnableNetEaseKey
import com.example.audora.utils.dataStore
import com.example.audora.utils.get

object NetEaseLyricsProvider : LyricsProvider {
    override val name = "NetEase"
    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableNetEaseKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
    ): Result<String> = NetEase.getLyrics(title, artist, duration)
}
