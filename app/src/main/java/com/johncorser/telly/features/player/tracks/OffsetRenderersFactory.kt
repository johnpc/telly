package com.johncorser.telly.features.player.tracks

import android.content.Context
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import com.johncorser.telly.features.player.PassthroughAudioSink

/**
 * Default renderers with the audio sink wrapped in [AudioOffsetSink], so the
 * quick-bar's audio-sync stepper really shifts the A/V clock (the only
 * Media3 hook that lets the offset change live without re-preparing).
 * With [passthrough] on, the wrapped sink is the capability-forcing
 * [PassthroughAudioSink] instead of the stock one — ONE factory composes
 * both behaviors, so passthrough streams keep the offset stepper too.
 */
class OffsetRenderersFactory(
    context: Context,
    private val offsets: AudioOffsetHolder,
    private val passthrough: Boolean = false,
) : DefaultRenderersFactory(context) {
    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean,
    ): AudioSink? =
        baseAudioSink(context, enableFloatOutput, enableAudioTrackPlaybackParams)
            ?.let { sink -> AudioOffsetSink(sink, offsets) }

    private fun baseAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean,
    ): AudioSink? =
        if (passthrough) {
            PassthroughAudioSink.build(context, enableFloatOutput, enableAudioTrackPlaybackParams)
        } else {
            super.buildAudioSink(context, enableFloatOutput, enableAudioTrackPlaybackParams)
        }
}
