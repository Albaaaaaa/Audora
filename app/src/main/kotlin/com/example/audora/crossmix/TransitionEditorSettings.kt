package com.example.audora.crossmix

import androidx.datastore.preferences.core.stringPreferencesKey

/** Persisted per outgoing/incoming pair; encoded as a compact value in the app's preferences. */
data class TransitionEditorSettings(
    val bars: Int = 8,
    val volume: String = "Smooth crossfade",
    val eq: String = "Center bass swap",
    val effect: String = "None",
    val preset: String = "Custom",
) {
    fun encode() = "$bars bars|$volume|$eq|$effect|$preset"

    companion object {
        fun decode(value: String?): TransitionEditorSettings? {
            val parts = value?.split('|') ?: return null
            if (parts.size != 5) return null
            return TransitionEditorSettings(
                bars = parts[0].substringBefore(' ').toIntOrNull()?.coerceIn(2, 16) ?: return null,
                volume = parts[1], eq = parts[2], effect = parts[3], preset = parts[4],
            )
        }
    }
}

fun transitionEditorKey(outgoingId: String, incomingId: String) = stringPreferencesKey(
    "transition_editor_${outgoingId.hashCode().toUInt().toString(16)}_${incomingId.hashCode().toUInt().toString(16)}",
)
