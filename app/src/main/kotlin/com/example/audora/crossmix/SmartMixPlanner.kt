package com.example.audora.crossmix

import kotlin.math.abs
import kotlin.math.max

enum class SmartMixArc(val title: String, val subtitle: String) {
    Chill("Chill", "easy, even flow"),
    Balanced("Balanced", "steady energy"),
    Build("Build", "gradually rises"),
    Peak("Peak", "climbs, then winds down"),
}

data class SmartMixCandidate(
    val uid: Int,
    val title: String,
    val artist: String,
    val originalOrder: Int,
    val analysis: TrackAnalysis?,
) {
    val energy: Float get() = analysis?.energy?.takeIf { it.isNotEmpty() }?.average()?.toFloat()?.coerceIn(0f, 1f) ?: 0.5f
    val bpm: Float? get() = analysis?.bpm?.takeIf { it > 0f }
    val key: String? get() = analysis?.key
}

/** Deterministic greedy ordering: follow the selected energy arc while avoiding abrupt tempo/key jumps. */
object SmartMixPlanner {
    fun order(tracks: List<SmartMixCandidate>, arc: SmartMixArc): List<SmartMixCandidate> {
        if (tracks.size < 3) return tracks
        val remaining = tracks.toMutableList()
        val ordered = ArrayList<SmartMixCandidate>(tracks.size)
        while (remaining.isNotEmpty()) {
            val target = targetEnergy(arc, ordered.size, tracks.size)
            val previous = ordered.lastOrNull()
            val next = remaining.minByOrNull { candidate ->
                val energyCost = abs(candidate.energy - target) * 1.7f
                val bpmCost = previous?.bpm?.let { old -> candidate.bpm?.let { (abs(it - old) / max(old, it)).coerceAtMost(0.5f) * 0.5f } } ?: 0.12f
                val keyCost = previous?.key?.let { old ->
                    candidate.key?.let { key -> (Harmony.camelotDistance(old, key) ?: 3) / 6f * 0.35f }
                } ?: 0.08f
                energyCost + bpmCost + keyCost + candidate.originalOrder * 0.0001f
            } ?: break
            ordered += next
            remaining.remove(next)
        }
        return ordered
    }

    fun targetEnergy(arc: SmartMixArc, index: Int, count: Int): Float {
        val x = if (count <= 1) 0f else index.toFloat() / (count - 1)
        return when (arc) {
            SmartMixArc.Chill -> 0.22f + x * 0.10f
            SmartMixArc.Balanced -> 0.52f
            SmartMixArc.Build -> 0.20f + x * 0.65f
            SmartMixArc.Peak -> if (x < 0.62f) 0.24f + x / 0.62f * 0.62f else 0.86f - (x - 0.62f) / 0.38f * 0.38f
        }
    }
}
