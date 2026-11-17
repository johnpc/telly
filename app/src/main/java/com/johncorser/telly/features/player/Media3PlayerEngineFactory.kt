package com.johncorser.telly.features.player

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import com.johncorser.telly.features.player.tracks.AudioOffsetHolder
import com.johncorser.telly.features.player.tracks.ExoTrackFacade
import com.johncorser.telly.features.player.tracks.OffsetRenderersFactory
import com.johncorser.telly.features.player.tracks.disableCaptionsByDefault

/**
 * Built in [buildMedia3PlayerEngine] so devices get audio focus and
 * TV-ready defaults. Multiview panes pass [handleAudioFocus] = false:
 * N players each grabbing focus would pause one another, so the
 * pool's engines share the app's focus and the focused pane owns
 * audio by mute state instead. Single fullscreen keeps the default.
 * [audio] carries the persisted surround-by-default + passthrough
 * choices (defaults off = the previous behavior); [tuning] the
 * Playback buffer-size + decoder rows read at build time.
 */
fun Media3PlayerEngine.Companion.create(
    context: Context,
    handleAudioFocus: Boolean = true,
    userAgentFor: (streamUrl: String) -> String = { STREAM_USER_AGENT },
    audio: PlayerAudioPrefs = PlayerAudioPrefs(),
    tuning: PlayerTuning = PlayerTuning(),
): Media3PlayerEngine = buildMedia3PlayerEngine(context, handleAudioFocus, userAgentFor, audio, tuning)

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
    val telemetry = engineTelemetry()
    player.addAnalyticsListener(TelemetryAnalytics(telemetry, player))
    player.addAnalyticsListener(TelemetryCodecAnalytics(telemetry))
    return Media3PlayerEngine(
        player = player,
        userAgent = userAgent,
        tracks = ExoTrackFacade(player, offsets, audio.surroundByDefault),
        decoders = decoders,
        // Real devices get the stall watchdog: it samples on the player's
        // thread and auto-recovers silently wedged streams.
        schedulers = EngineSchedulers.forPlayer(player, telemetry),
    )
}

/** logcat tag for playback health (`adb logcat -s telly-play`). */
internal const val PLAY_LOG_TAG = "telly-play"

private var engineCount = 0

/** One telemetry per engine, its lines prefixed `[eN]` (guide preview, fullscreen and panes interleave). */
private fun engineTelemetry(): PlaybackTelemetry {
    val id = ++engineCount
    return PlaybackTelemetry(SystemClock::elapsedRealtime) { Log.i(PLAY_LOG_TAG, "[e$id] $it") }
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
