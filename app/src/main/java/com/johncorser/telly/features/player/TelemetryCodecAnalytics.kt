package com.johncorser.telly.features.player

import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.analytics.AnalyticsListener

/** The decoders and video format each stream actually uses, for [PlaybackTelemetry]. */
@UnstableApi
class TelemetryCodecAnalytics(
    private val telemetry: PlaybackTelemetry,
) : AnalyticsListener {
    override fun onVideoDecoderInitialized(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long,
    ) = telemetry.note("video decoder $decoderName (init ${initializationDurationMs}ms)")

    override fun onAudioDecoderInitialized(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long,
    ) = telemetry.note("audio decoder $decoderName (init ${initializationDurationMs}ms)")

    override fun onVideoInputFormatChanged(
        eventTime: AnalyticsListener.EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?,
    ) = telemetry.note("video format ${format.sampleMimeType} ${format.width}x${format.height}")
}
