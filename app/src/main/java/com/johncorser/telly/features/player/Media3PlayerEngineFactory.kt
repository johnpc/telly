package com.johncorser.telly.features.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.johncorser.telly.features.player.tracks.AudioOffsetHolder
import com.johncorser.telly.features.player.tracks.ExoTrackFacade
import com.johncorser.telly.features.player.tracks.OffsetRenderersFactory
import com.johncorser.telly.features.player.tracks.disableCaptionsByDefault

/**
 * Builds the real ExoPlayer behind [Media3PlayerEngine.create]: audio focus
 * via the audio attributes, the per-playlist user agent through the resolving
 * media source factory, and the wrapped audio sink so the quick-bar's
 * audio-sync offset can shift the A/V clock live (features/player/tracks).
 */
internal fun buildMedia3PlayerEngine(
    context: Context,
    handleAudioFocus: Boolean,
    userAgentFor: (streamUrl: String) -> String,
): Media3PlayerEngine {
    val audioAttributes =
        AudioAttributes
            .Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    val offsets = AudioOffsetHolder()
    val userAgent = StreamUserAgent(userAgentFor)
    val player =
        ExoPlayer
            .Builder(context, OffsetRenderersFactory(context, offsets))
            .setMediaSourceFactory(streamMediaSourceFactory(context, userAgent::current))
            .setAudioAttributes(audioAttributes, handleAudioFocus)
            .build()
    player.disableCaptionsByDefault()
    return Media3PlayerEngine(player, userAgent, ExoTrackFacade(player, offsets))
}
