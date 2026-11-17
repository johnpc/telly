package com.johncorser.telly.features.player

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import java.io.IOException

/** Feeds Media3's analytics callbacks into [PlaybackTelemetry]. */
@UnstableApi
class TelemetryAnalytics(
    private val telemetry: PlaybackTelemetry,
    private val player: Player,
) : AnalyticsListener {
    override fun onMediaItemTransition(
        eventTime: AnalyticsListener.EventTime,
        mediaItem: MediaItem?,
        reason: Int,
    ) = telemetry.onLoad(streamLabel(mediaItem?.localConfiguration?.uri?.toString().orEmpty()))

    override fun onPlaybackStateChanged(
        eventTime: AnalyticsListener.EventTime,
        state: Int,
    ) = reportState()

    override fun onIsPlayingChanged(
        eventTime: AnalyticsListener.EventTime,
        isPlaying: Boolean,
    ) = reportState()

    override fun onPlayerError(
        eventTime: AnalyticsListener.EventTime,
        error: PlaybackException,
    ) = telemetry.note("player error ${error.errorCodeName}: ${error.message}")

    private fun reportState() =
        telemetry.onState(
            stateLabel(player.playbackState, player.isPlaying),
            player.currentPosition,
        )

    override fun onDroppedVideoFrames(
        eventTime: AnalyticsListener.EventTime,
        droppedFrames: Int,
        elapsedMs: Long,
    ) = telemetry.onDroppedFrames(droppedFrames, elapsedMs)

    override fun onAudioUnderrun(
        eventTime: AnalyticsListener.EventTime,
        bufferSize: Int,
        bufferSizeMs: Long,
        elapsedSinceLastFeedMs: Long,
    ) = telemetry.onAudioUnderrun(bufferSizeMs)

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean,
    ) = telemetry.onLoadError("${error.javaClass.simpleName}: ${error.message}")
}

/** "READY/playing", "BUFFERING", … — one label per (state, playing) pair. */
internal fun stateLabel(
    playbackState: Int,
    isPlaying: Boolean,
): String {
    val name =
        when (playbackState) {
            Player.STATE_BUFFERING -> "BUFFERING"
            Player.STATE_READY -> "READY"
            Player.STATE_ENDED -> "ENDED"
            else -> "IDLE"
        }
    return if (playbackState == Player.STATE_READY) "$name/${if (isPlaying) "playing" else "paused"}" else name
}
