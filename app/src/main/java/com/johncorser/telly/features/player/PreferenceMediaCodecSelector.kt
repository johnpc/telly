package com.johncorser.telly.features.player

import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector

/**
 * Media3's default codec list, re-ranked software-first while the live
 * [DecoderPreferences] say SOFTWARE for the mime type's renderer. Called
 * on every prepare, so a per-channel override applied before `load` takes
 * effect on that very tune. Falling back is free: when no software codec
 * supports the format the (re-ranked, not filtered) list still contains
 * the hardware ones.
 */
class PreferenceMediaCodecSelector(
    private val preferences: DecoderPreferences,
) : MediaCodecSelector {
    override fun getDecoderInfos(
        mimeType: String,
        requiresSecureDecoder: Boolean,
        requiresTunnelingDecoder: Boolean,
    ): List<MediaCodecInfo> {
        val infos =
            MediaCodecSelector.DEFAULT.getDecoderInfos(
                mimeType,
                requiresSecureDecoder,
                requiresTunnelingDecoder,
            )
        if (preferences.forMimeType(mimeType) != DecoderMode.SOFTWARE) return infos
        return SoftwareCodecOrdering.preferSoftware(infos) { it.hardwareAccelerated }
    }
}
