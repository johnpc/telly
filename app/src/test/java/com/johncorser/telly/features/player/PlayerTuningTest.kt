package com.johncorser.telly.features.player

import androidx.media3.exoplayer.DefaultLoadControl
import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import org.junit.Assert.assertEquals
import org.junit.Test

/** Buffer-size mapping + the settings snapshot the engine factory reads. */
class PlayerTuningTest {
    @Test
    fun `small - the captured default - keeps the exact Media3 defaults`() {
        assertEquals(
            BufferDurations(
                minBufferMs = DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                maxBufferMs = DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                bufferForPlaybackMs = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                bufferForPlaybackAfterRebufferMs = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
            ),
            BufferSizes.durations("Small"),
        )
    }

    @Test
    fun `medium and large scale the target buffer, start thresholds stay stock`() {
        val small = BufferSizes.durations("Small")
        val medium = BufferSizes.durations("Medium")
        val large = BufferSizes.durations("Large")
        assertEquals(small.minBufferMs * 2, medium.minBufferMs)
        assertEquals(small.maxBufferMs * 2, medium.maxBufferMs)
        assertEquals(small.minBufferMs * 4, large.minBufferMs)
        assertEquals(small.maxBufferMs * 4, large.maxBufferMs)
        assertEquals(small.bufferForPlaybackMs, large.bufferForPlaybackMs)
        assertEquals(small.bufferForPlaybackAfterRebufferMs, medium.bufferForPlaybackAfterRebufferMs)
    }

    @Test
    fun `an unknown raw falls back to the default pick`() {
        assertEquals(BufferSizes.durations("Small"), BufferSizes.durations("Gigantic"))
    }

    @Test
    fun `from reads the three Playback settings, defaults included`() {
        val settings = SettingsRepository(InMemoryKeyValueStore())
        assertEquals(PlayerTuning("Small", "Hardware", "Hardware"), PlayerTuning.from(settings))

        settings.set(TellySettings.BUFFER_SIZE, "Large")
        settings.set(TellySettings.AUDIO_DECODER, "Software")
        assertEquals(PlayerTuning("Large", "Software", "Hardware"), PlayerTuning.from(settings))
    }
}
