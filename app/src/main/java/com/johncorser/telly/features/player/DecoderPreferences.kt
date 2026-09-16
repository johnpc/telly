package com.johncorser.telly.features.player

/**
 * The "Audio decoder" / "Video decoder" picks (global Playback rows +
 * per-channel Channel-options overrides). What each honestly does:
 *
 * - HARDWARE (default): Media3's stock MediaCodec ranking — the platform
 *   lists hardware-accelerated decoders first, so they win when present.
 * - SOFTWARE: telly re-ranks the SAME MediaCodec list to put decoders the
 *   platform marks non-hardware-accelerated (`c2.android.*`/`OMX.google.*`)
 *   first. Software audio decoders exist for every common codec, so audio
 *   really moves off the DSP; software video exists for AVC/VP9/AV1 on most
 *   devices but may be missing or capability-limited for high resolutions —
 *   when no software decoder supports the format, playback falls back to
 *   the hardware codec instead of failing.
 */
enum class DecoderMode(
    val raw: String,
) {
    HARDWARE("Hardware"),
    SOFTWARE("Software"),
    ;

    companion object {
        fun fromRaw(raw: String?): DecoderMode? = entries.firstOrNull { it.raw == raw }
    }
}

/**
 * Live decoder preference the codec selector consults on every prepare:
 * the base comes from the global settings at engine build; a tune applies
 * the channel's overrides ([overrideWith]) BEFORE the stream loads, so the
 * per-channel pick wins for exactly that channel (null = back to global).
 */
class DecoderPreferences(
    private val baseAudio: DecoderMode = DecoderMode.HARDWARE,
    private val baseVideo: DecoderMode = DecoderMode.HARDWARE,
) {
    @Volatile
    var audio: DecoderMode = baseAudio
        private set

    @Volatile
    var video: DecoderMode = baseVideo
        private set

    /** Per-channel raws ("Hardware"/"Software"/null = follow the global). */
    fun overrideWith(
        audioRaw: String?,
        videoRaw: String?,
    ) {
        audio = DecoderMode.fromRaw(audioRaw) ?: baseAudio
        video = DecoderMode.fromRaw(videoRaw) ?: baseVideo
    }

    /** MediaCodec selection is keyed by mime type, not renderer. */
    fun forMimeType(mimeType: String): DecoderMode = if (mimeType.startsWith("audio/")) audio else video

    companion object {
        /** Inert default for engines/fakes without decoder wiring. */
        val NONE = DecoderPreferences()

        fun of(tuning: PlayerTuning): DecoderPreferences =
            DecoderPreferences(
                baseAudio = DecoderMode.fromRaw(tuning.audioDecoder) ?: DecoderMode.HARDWARE,
                baseVideo = DecoderMode.fromRaw(tuning.videoDecoder) ?: DecoderMode.HARDWARE,
            )
    }
}

/** Stable re-ranking used by the selector: software decoders first. */
object SoftwareCodecOrdering {
    fun <T> preferSoftware(
        infos: List<T>,
        isHardwareAccelerated: (T) -> Boolean,
    ): List<T> = infos.sortedBy(isHardwareAccelerated)
}
