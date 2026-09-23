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
 * Small — the captured TiviMate default — keeps Media3's stock 50 s target
 * and 1 s first-tune start (zap speed untouched) but restarts a stalled
 * stream only after 5 s: Media3's stock 2 s restart is too thin for jittery
 * IPTV — a stream that just underran restarts and immediately underruns
 * again, which reads as continuous stutter. Medium and Large also
 * double/quadruple the target and accept a slower 2.5 s start (and a 10 s
 * restart on Large) so flaky sources ride out longer stalls at the cost of
 * memory and zap latency.
 */
object BufferSizes {
    const val DEFAULT = "Small"

    // Media3 DefaultLoadControl defaults (asserted against the real
    // constants in PlayerTuningTest so a library bump can't drift them).
    private const val MEDIA3_MIN_MS = 50_000
    private const val MEDIA3_MAX_MS = 50_000
    private const val MEDIA3_PLAYBACK_MS = 1_000

    private const val REBUFFER_MS = 5_000
    private const val SLOW_START_MS = 2_500
    private const val LARGE_REBUFFER_MS = 10_000

    private val small = BufferDurations(MEDIA3_MIN_MS, MEDIA3_MAX_MS, MEDIA3_PLAYBACK_MS, REBUFFER_MS)

    /** Unknown raws fall back to the default pick (defensive). */
    fun durations(raw: String): BufferDurations =
        when (raw) {
            "Medium" ->
                small.copy(
                    minBufferMs = MEDIA3_MIN_MS * 2,
                    maxBufferMs = MEDIA3_MAX_MS * 2,
                    bufferForPlaybackMs = SLOW_START_MS,
                )
            "Large" ->
                small.copy(
                    minBufferMs = MEDIA3_MIN_MS * 4,
                    maxBufferMs = MEDIA3_MAX_MS * 4,
                    bufferForPlaybackMs = SLOW_START_MS,
                    bufferForPlaybackAfterRebufferMs = LARGE_REBUFFER_MS,
                )
            else -> small
        }
}
