package com.johncorser.telly.features.player

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * The Playback settings the engine honors at build time: Buffer size and
 * the Hardware/Software decoder preferences. Engines are created per
 * playback screen, so a changed setting applies the next time a screen
 * (playback, guide, multiview, VOD, recordings) builds its engine.
 */
data class PlayerTuning(
    val bufferSize: String = BufferSizes.DEFAULT,
    val audioDecoder: String = DecoderMode.HARDWARE.raw,
    val videoDecoder: String = DecoderMode.HARDWARE.raw,
) {
    companion object {
        /** Reads the three Playback rows; called inside each engine factory. */
        fun from(settings: SettingsRepository): PlayerTuning =
            PlayerTuning(
                bufferSize = settings.get(TellySettings.BUFFER_SIZE),
                audioDecoder = settings.get(TellySettings.AUDIO_DECODER),
                videoDecoder = settings.get(TellySettings.VIDEO_DECODER),
            )
    }
}

/** Target buffer durations behind the "Buffer size" picker. */
data class BufferDurations(
    val minBufferMs: Int,
    val maxBufferMs: Int,
    val bufferForPlaybackMs: Int,
    val bufferForPlaybackAfterRebufferMs: Int,
)

/**
 * "Buffer size" (Small/Medium/Large) -> DefaultLoadControl durations.
 * Small — the captured TiviMate default — keeps Media3's stock values
 * (50 s target, 1 s start, 2 s after a rebuffer); Medium and Large
 * double/quadruple the target so flaky IPTV sources ride out longer
 * stalls at the cost of memory and zap latency. Start thresholds stay
 * stock: a bigger pre-play delay would slow every zap.
 */
object BufferSizes {
    const val DEFAULT = "Small"

    // Media3 DefaultLoadControl defaults (asserted against the real
    // constants in BufferSizesTest so a library bump can't drift them).
    private const val MEDIA3_MIN_MS = 50_000
    private const val MEDIA3_MAX_MS = 50_000
    private const val MEDIA3_PLAYBACK_MS = 1_000
    private const val MEDIA3_REBUFFER_MS = 2_000

    private val small = BufferDurations(MEDIA3_MIN_MS, MEDIA3_MAX_MS, MEDIA3_PLAYBACK_MS, MEDIA3_REBUFFER_MS)

    /** Unknown raws fall back to the default pick (defensive). */
    fun durations(raw: String): BufferDurations =
        when (raw) {
            "Medium" -> small.copy(minBufferMs = MEDIA3_MIN_MS * 2, maxBufferMs = MEDIA3_MAX_MS * 2)
            "Large" -> small.copy(minBufferMs = MEDIA3_MIN_MS * 4, maxBufferMs = MEDIA3_MAX_MS * 4)
            else -> small
        }
}
