package com.johncorser.telly.features.player

import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The engine's video-details bookkeeping: a snapshot of the stream formats
 * at READY, plus the render-time frame-rate estimate that fills in what TS
 * containers do not report.
 */
class EngineVideoFeed {
    private val mutable = MutableStateFlow<VideoDetails?>(null)
    private var frameRates = FrameRateEstimator()

    val video: StateFlow<VideoDetails?> = mutable.asStateFlow()

    /** A new stream starts a fresh frame-rate estimate. */
    fun reset() {
        frameRates = FrameRateEstimator()
    }

    fun onFrame(presentationTimeUs: Long) {
        frameRates.onFrame(presentationTimeUs)?.let(::onFrameRateMeasured)
    }

    fun onReady(player: ExoPlayer) {
        val videoFormat = player.videoFormat
        mutable.value =
            VideoDetails(
                width = videoFormat?.width ?: 0,
                height = videoFormat?.height ?: 0,
                frameRate = (videoFormat?.frameRate ?: 0f).coerceAtLeast(0f),
                audioChannels = player.audioFormat?.channelCount ?: 0,
            )
    }

    /** TS formats report no frame rate; the render-time estimate fills it in. */
    private fun onFrameRateMeasured(fps: Float) {
        mutable.update { details ->
            if (details != null && details.frameRate <= 0f) details.copy(frameRate = fps) else details
        }
    }
}
