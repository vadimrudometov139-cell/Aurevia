package com.aurevia.player.audio

import android.content.Context
import android.media.audiofx.DynamicsProcessing
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import java.io.File

class TenBandEqualizer {
    private var dynamicsProcessing: DynamicsProcessing? = null
    val frequencies = floatArrayOf(31f, 62f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
    var isBitPerfectMode: Boolean = false
        private set

    fun attachToSession(audioSessionId: Int) {
        release()
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET) return

        val config = DynamicsProcessing.Config.Builder(
            DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
            2,
            true, 10,
            false, 0,
            false, 0,
            true
        ).build()

        dynamicsProcessing = DynamicsProcessing(0, audioSessionId, config).apply {
            enabled = !isBitPerfectMode
            frequencies.forEachIndexed { index, freq ->
                val band = DynamicsProcessing.EqBand(true, freq, 0f)
                setPreEqBandAllChannelsTo(index, band)
            }
        }
    }

    fun setBandGain(bandIndex: Int, gainDb: Float) {
        if (isBitPerfectMode) return
        val clampedGain = gainDb.coerceIn(-15f, 15f)
        val band = DynamicsProcessing.EqBand(true, frequencies[bandIndex], clampedGain)
        dynamicsProcessing?.setPreEqBandAllChannelsTo(bandIndex, band)
    }

    fun setBitPerfect(enabled: Boolean) {
        isBitPerfectMode = enabled
        dynamicsProcessing?.enabled = !enabled
    }

    fun release() {
        dynamicsProcessing?.release()
        dynamicsProcessing = null
    }
}

@OptIn(UnstableApi::class)
object PlayerFactory {
    private var simpleCache: SimpleCache? = null

    fun createOptimizedPlayer(context: Context): ExoPlayer {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "aurevia_flac_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(1024L * 1024L * 1024L)
            simpleCache = SimpleCache(cacheDir, evictor, StandaloneDatabaseProvider(context))
        }

        val httpFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache!!)
            .setUpstreamDataSourceFactory(httpFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(30_000, 120_000, 2_500, 5_000)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build()

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }
}
