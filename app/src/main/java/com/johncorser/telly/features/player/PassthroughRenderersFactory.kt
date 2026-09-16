package com.johncorser.telly.features.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioCapabilities
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink

/**
 * "Audio passthrough", implemented as what Media3 actually supports:
 * Media3 already hands AC-3/E-AC-3/DTS/TrueHD to the AudioTrack WITHOUT
 * decoding whenever the platform reports the encoding on the current
 * output (DefaultAudioSink's automatic capability detection) — that
 * automatic behavior is the toggle's OFF state, i.e. telly's previous
 * behavior, unchanged. ON builds the player with this factory, whose audio
 * sink force-advertises the standard surround encodings even when the HDMI
 * stack fails to report them (common behind ARC/older receivers), so
 * encoded surround is passed through instead of decoded to PCM.
 */
internal class PassthroughRenderersFactory(
    context: Context,
) : DefaultRenderersFactory(context) {
    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean,
    ): AudioSink =
        DefaultAudioSink
            .Builder(context)
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
            .setAudioCapabilities(AudioCapabilities(FORCED_ENCODINGS, MAX_CHANNELS))
            .build()

    private companion object {
        val FORCED_ENCODINGS =
            intArrayOf(
                C.ENCODING_PCM_16BIT,
                C.ENCODING_AC3,
                C.ENCODING_E_AC3,
                C.ENCODING_E_AC3_JOC,
                C.ENCODING_DTS,
                C.ENCODING_DTS_HD,
                C.ENCODING_DOLBY_TRUEHD,
            )
        const val MAX_CHANNELS = 8
    }
}
