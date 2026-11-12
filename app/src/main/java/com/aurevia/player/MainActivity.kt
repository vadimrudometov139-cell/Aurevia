package com.aurevia.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.aurevia.player.algorithm.SmartWaveEngine
import com.aurevia.player.audio.PlayerFactory
import com.aurevia.player.audio.TenBandEqualizer
import com.aurevia.player.data.MusicSource
import com.aurevia.player.data.Track
import com.aurevia.player.ui.AureviaApp

class MainActivity : ComponentActivity() {
    private lateinit var player: Player
    private val equalizer = TenBandEqualizer()
    private val waveEngine = SmartWaveEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val exoPlayer = PlayerFactory.createOptimizedPlayer(this)
        player = exoPlayer

        exoPlayer.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                equalizer.attachToSession(audioSessionId)
            }
        })

        val demoLibrary = listOf(
            Track(
                id = "1",
                title = "Красная Помада",
                artist = "FACE",
                coverUrl = "",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                source = MusicSource.TIDAL,
                qualityBadge = "FLAC 24-bit/96kHz",
                bpm = 135,
                energy = 0.85f,
                moodTag = "Агрессивный"
            ),
            Track(
                id = "2",
                title = "Там ревели горы",
                artist = "MiyaGi & Andy Panda",
                coverUrl = "",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                source = MusicSource.DEEZER,
                qualityBadge = "FLAC 16-bit/44.1kHz",
                bpm = 98,
                energy = 0.65f,
                moodTag = "Ночной драйв"
            )
        )

        setContent {
            var displayedTracks by remember { mutableStateOf(demoLibrary) }
            var currentTrack by remember { mutableStateOf<Track?>(null) }
            var isPlaying by remember { mutableStateOf(false) }

            AureviaApp(
                tracks = displayedTracks,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                equalizer = equalizer,
                onSearch = { query, source ->
                    displayedTracks = demoLibrary.filter {
                        (query.isBlank() || it.title.contains(query, true) || it.artist.contains(query, true)) &&
                        (source == MusicSource.ALL || it.source == source)
                    }
                },
                onTrackClick = { track ->
                    currentTrack?.let { prev ->
                        val progress = if (player.duration > 0) player.currentPosition.toFloat() / player.duration else 0f
                        waveEngine.registerPlaybackFeedback(prev, progress)
                    }
                    currentTrack = track
                    player.setMediaItem(MediaItem.fromUri(track.streamUrl))
                    player.prepare()
                    player.play()
                    isPlaying = true
                },
                onPlayPause = {
                    if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.play()
                        isPlaying = true
                    }
                },
                onStartWave = { mood ->
                    displayedTracks = waveEngine.generateNextWave(demoLibrary, currentTrack, mood)
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        equalizer.release()
        player.release()
    }
}
