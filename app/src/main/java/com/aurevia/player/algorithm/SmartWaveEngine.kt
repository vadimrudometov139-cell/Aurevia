package com.aurevia.player.algorithm

import com.aurevia.player.data.Track
import kotlin.math.abs

class SmartWaveEngine {
    private val artistWeights = mutableMapOf<String, Float>()
    private val skippedTrackIds = mutableSetOf<String>()
    private val recentlyPlayedArtists = ArrayDeque<String>(5)

    fun registerPlaybackFeedback(track: Track, listenedPercent: Float) {
        if (listenedPercent < 0.15f) {
            skippedTrackIds.add(track.id)
            artistWeights[track.artist] = (artistWeights[track.artist] ?: 1.0f) * 0.4f
        } else if (listenedPercent > 0.80f) {
            artistWeights[track.artist] = ((artistWeights[track.artist] ?: 1.0f) * 1.25f).coerceAtMost(3.0f)
        }

        if (recentlyPlayedArtists.size >= 5) recentlyPlayedArtists.removeFirst()
        recentlyPlayedArtists.addLast(track.artist)
    }

    fun generateNextWave(
        pool: List<Track>,
        currentTrack: Track?,
        targetMood: String? = null
    ): List<Track> {
        return pool
            .filter { it.id !in skippedTrackIds }
            .filter { it.artist !in recentlyPlayedArtists }
            .sortedByDescending { candidate ->
                var score = artistWeights[candidate.artist] ?: 1.0f

                if (!targetMood.isNullOrBlank() && candidate.moodTag.equals(targetMood, ignoreCase = true)) {
                    score += 2.5f
                }

                if (currentTrack != null) {
                    val bpmDiff = abs(currentTrack.bpm - candidate.bpm) / 100f
                    val energyDiff = abs(currentTrack.energy - candidate.energy)
                    score += (1.5f - bpmDiff - energyDiff)
                }

                if (candidate.source.supportsLossless) score += 0.5f

                score
            }
    }
}
