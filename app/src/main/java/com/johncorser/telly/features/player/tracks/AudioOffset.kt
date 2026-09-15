package com.johncorser.telly.features.player.tracks

import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.ForwardingAudioSink

/**
 * The mutable audio-sync offset, written by the UI thread (facade) and read
 * by the playback thread's audio sink on every position poll.
 */
class AudioOffsetHolder {
    @Volatile
    var offsetMs: Long = 0L
}

/**
 * Applies the audio-sync offset by shifting the audio clock: the sink's
 * reported position is ExoPlayer's master clock during playback, so
 * reporting the audio N ms further along makes video frames render N ms
 * earlier relative to the sound — the viewer hears the audio N ms later
 * (positive offset = audio delayed, TiviMate's convention).
 */
class AudioOffsetSink(
    sink: AudioSink,
    private val offsets: AudioOffsetHolder,
) : ForwardingAudioSink(sink) {
    override fun getCurrentPositionUs(sourceEnded: Boolean): Long {
        val position = super.getCurrentPositionUs(sourceEnded)
        if (position == AudioSink.CURRENT_POSITION_NOT_SET) return position
        return position + offsets.offsetMs * MICROS_PER_MILLI
    }

    private companion object {
        const val MICROS_PER_MILLI = 1_000L
    }
}
