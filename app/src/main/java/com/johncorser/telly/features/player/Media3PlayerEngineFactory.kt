package com.johncorser.telly.features.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import com.johncorser.telly.features.player.tracks.AudioOffsetHolder
import com.johncorser.telly.features.player.tracks.ExoTrackFacade
import com.johncorser.telly.features.player.tracks.OffsetRenderersFactory
import com.johncorser.telly.features.player.tracks.disableCaptionsByDefault

/**
 * Builds the real ExoPlayer behind [Media3PlayerEngine.create]: audio focus
 * via the audio attributes, the per-playlist user agent through the resolving
 * media source factory, the wrapped audio sink so the quick-bar's audio-sync
 * offset can shift the A/V clock live (features/player/tracks), the audio
 * settings ([PlayerAudioPrefs]: passthrough sink + surround-by-default) and
 * the Playback settings ([PlayerTuning]): Buffer size -> DefaultLoadControl,
 * Audio/Video decoder -> the live-preference codec selector.
 */
internal fun buildMedia3PlayerEngine(
    context: Context,
    handleAudioFocus: Boolean,
    userAgentFor: (streamUrl: String) -> String,
    audio: PlayerAudioPrefs = PlayerAudioPrefs(),
    tuning: PlayerTuning = PlayerTuning(),
): Media3PlayerEngine {
    val audioAttributes =
        AudioAttributes
            .Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    val offsets = AudioOffsetHolder()
    val userAgent = StreamUserAgent(userAgentFor)
    val decoders = DecoderPreferences.of(tuning)
    // Passthrough is read once per engine build; each visit to a playback
    // surface constructs a fresh player, so a toggle applies on next tune.
    val renderers = OffsetRenderersFactory(context, offsets, passthrough = audio.passthrough())
    renderers.setMediaCodecSelector(PreferenceMediaCodecSelector(decoders))
    val player =
        ExoPlayer
            .Builder(context, renderers)
            .setMediaSourceFactory(streamMediaSourceFactory(context, userAgent::current))
            .setAudioAttributes(audioAttributes, handleAudioFocus)
            .setLoadControl(loadControl(BufferSizes.durations(tuning.bufferSize)))
            .build()
    player.disableCaptionsByDefault()
    return Media3PlayerEngine(player, userAgent, ExoTrackFacade(player, offsets, audio.surroundByDefault), decoders)
}

/** The picked buffer durations over Media3's stock DefaultLoadControl. */
private fun loadControl(durations: BufferDurations): DefaultLoadControl =
    DefaultLoadControl
        .Builder()
        .setBufferDurationsMs(
            durations.minBufferMs,
            durations.maxBufferMs,
            durations.bufferForPlaybackMs,
            durations.bufferForPlaybackAfterRebufferMs,
        ).build()
