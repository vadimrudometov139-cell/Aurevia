package com.aurevia.player.data

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

enum class MusicSource(val title: String, val supportsLossless: Boolean) {
    ALL("Все площадки (Авто-FLAC)", true),
    TIDAL("TIDAL (Hi-Res / FLAC)", true),
    DEEZER("Deezer (FLAC 16-bit)", true),
    YANDEX("Яндекс Музыка", true),
    SOUNDCLOUD("SoundCloud", false),
    VK("VK Music", false),
    YOUTUBE("YouTube Music", false),
    SPOTIFY("Spotify (Метаданные + FLAC-поиск)", true)
}

@Immutable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String,
    val streamUrl: String,
    val source: MusicSource,
    val qualityBadge: String,
    val bpm: Int,
    val energy: Float,
    val moodTag: String
)

interface SourceProvider {
    val source: MusicSource
    suspend fun search(query: String): List<Track>
}

class MusicAggregator(private val providers: List<SourceProvider>) {
    suspend fun searchTracks(query: String, selectedSource: MusicSource): List<Track> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val activeProviders = if (selectedSource == MusicSource.ALL) {
            providers
        } else {
            providers.filter { it.source == selectedSource }
        }

        val results = activeProviders.map { provider ->
            async {
                runCatching { provider.search(query) }.getOrDefault(emptyList())
            }
        }.awaitAll().flatten()

        results.sortedByDescending { it.source.supportsLossless }
    }
}
