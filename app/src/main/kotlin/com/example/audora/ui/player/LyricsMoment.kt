package com.example.audora.ui.player

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.example.audora.models.MediaMetadata
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

internal data class LyricsMoment(
    val id: Long,
    val text: String,
    val title: String,
    val artist: String,
    val positionMs: Long,
)

internal object LyricsMomentStore {
    private const val PREFS = "audora_lyrics_moments"
    private const val KEY = "saved"

    fun all(context: Context): List<LyricsMoment> = runCatching {
        val rows = JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]"))
        List(rows.length()) { index -> rows.getJSONObject(index).toMoment() }.sortedByDescending { it.id }
    }.getOrDefault(emptyList())

    fun save(context: Context, moment: LyricsMoment) {
        val list = (listOf(moment) + all(context).filterNot { it.id == moment.id }).take(100)
        val rows = JSONArray().apply { list.forEach { put(it.toJson()) } }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, rows.toString()).apply()
    }

    fun delete(context: Context, moment: LyricsMoment) {
        val rows = JSONArray().apply { all(context).filterNot { it.id == moment.id }.forEach { put(it.toJson()) } }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, rows.toString()).apply()
    }

    private fun LyricsMoment.toJson() = JSONObject().apply {
        put("id", id); put("text", text); put("title", title); put("artist", artist); put("position", positionMs)
    }

    private fun JSONObject.toMoment() = LyricsMoment(
        id = optLong("id"), text = optString("text"), title = optString("title"),
        artist = optString("artist"), positionMs = optLong("position"),
    )
}

internal object LyricsMomentShare {
    fun share(context: Context, moment: LyricsMoment) {
        val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.LinearGradient(0f, 0f, 1080f, 1350f, 0xFF271949.toInt(), 0xFF091D28.toInt(), android.graphics.Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, 1080f, 1350f, bg)
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x335F46A8 }
        canvas.drawCircle(930f, 130f, 370f, glow)
        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
        val muted = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xBFFFFFFF.toInt() }
        white.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        white.textSize = 68f
        drawWrapped(canvas, moment.text.trim().ifBlank { "A moment in music" }, 92f, 565f, 850f, white, 86f, 6)
        muted.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        muted.textSize = 34f
        canvas.drawText(moment.title.take(48), 92f, 1085f, white)
        canvas.drawText(moment.artist.take(52), 92f, 1138f, muted)
        muted.textSize = 27f
        canvas.drawText(timestamp(moment.positionMs), 92f, 1215f, muted)
        white.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        white.textSize = 35f
        canvas.drawText("AUDORA  ✦  LYRICS MOMENT", 92f, 1288f, white)

        val output = File(context.cacheDir, "lyrics-moment-${moment.id}.png")
        FileOutputStream(output).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.FileProvider", output)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "${moment.text}\n— ${moment.title} · ${moment.artist} (${timestamp(moment.positionMs)})")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Lyrics Moment"))
    }

    internal fun timestamp(milliseconds: Long): String {
        val seconds = (milliseconds / 1000L).coerceAtLeast(0L)
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }

    private fun drawWrapped(canvas: Canvas, text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint, lineHeight: Float, maxLines: Int) {
        val lines = mutableListOf<String>()
        var line = StringBuilder()
        text.split(Regex("\\s+")).forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                lines += line.toString(); line = StringBuilder(word)
            } else line = StringBuilder(candidate)
        }
        if (line.isNotEmpty()) lines += line.toString()
        lines.take(maxLines).forEachIndexed { index, value ->
            canvas.drawText(value, x, startY + index * lineHeight, paint)
        }
    }
}
