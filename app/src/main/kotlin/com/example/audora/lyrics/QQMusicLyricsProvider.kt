// QQMusicLyricsProvider.kt

package com.example.audora.lyrics

import android.content.Context
import com.music.directlyrics.QQMusic
import com.example.audora.constants.EnableQQMusicKey
import com.example.audora.utils.dataStore
import com.example.audora.utils.get

object QQMusicLyricsProvider : LyricsProvider {
    override val name = "QQMusic"
    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableQQMusicKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
    ): Result<String> = QQMusic.getLyrics(title, artist, duration)
}
