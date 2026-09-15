package com.johncorser.telly.features.player.tracks

import android.content.Context
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink

/**
 * Default renderers with the audio sink wrapped in [AudioOffsetSink], so the
 * quick-bar's audio-sync stepper really shifts the A/V clock (the only
 * Media3 hook that lets the offset change live without re-preparing).
 */
class OffsetRenderersFactory(
    context: Context,
    private val offsets: AudioOffsetHolder,
) : DefaultRenderersFactory(context) {
    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean,
    ): AudioSink? =
        super
            .buildAudioSink(context, enableFloatOutput, enableAudioTrackPlaybackParams)
            ?.let { sink -> AudioOffsetSink(sink, offsets) }
}
