package com.johncorser.telly.features.recording

import com.johncorser.telly.features.guide.GuideLayer
import com.johncorser.telly.features.guide.GuideRecordingPrompts
import com.johncorser.telly.features.playback.PlaybackOverlay
import com.johncorser.telly.features.playback.PlaybackRecordingPrompts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The host-agnostic prompt to layer/overlay mapping for both surfaces. */
class RecordingPromptMappingTest {
    @Test
    fun `playback Done from a sheet lands back on the panel`() {
        val overlay = PlaybackRecordingPrompts.overlayFor(RecordingPrompt.Done, PlaybackOverlay.ChannelMenu(1L))
        assertEquals(PlaybackOverlay.Panel, overlay)
    }

    @Test
    fun `playback Done from bare playback lands on None`() {
        assertEquals(
            PlaybackOverlay.None,
            PlaybackRecordingPrompts.overlayFor(RecordingPrompt.Done, PlaybackOverlay.None),
        )
    }

    @Test
    fun `playback Stop and custom form ride the pushed back chain`() {
        val stop =
            PlaybackRecordingPrompts.overlayFor(
                RecordingPrompt.StopConfirm(7L, "News One"),
                PlaybackOverlay.ChannelMenu(1L),
            )
        assertTrue(stop is PlaybackOverlay.RecordingStop)
        assertEquals(PlaybackOverlay.Panel, (stop as PlaybackOverlay.RecordingStop).back)

        val custom = PlaybackRecordingPrompts.overlayFor(RecordingPrompt.CustomForm, PlaybackOverlay.None)
        assertTrue(custom is PlaybackOverlay.CustomRecording)
    }

    @Test
    fun `guide Done returns to the grid and the rest get their own layers`() {
        assertEquals(GuideLayer.Grid, GuideRecordingPrompts.layerFor(RecordingPrompt.Done, GuideLayer.RowMenu))
        assertTrue(
            GuideRecordingPrompts.layerFor(
                RecordingPrompt.StopConfirm(3L, "A"),
                GuideLayer.RowMenu,
            ) is GuideLayer.RecordingStop,
        )
        assertTrue(
            GuideRecordingPrompts.layerFor(
                RecordingPrompt.CustomForm,
                GuideLayer.RowMenu,
            ) is GuideLayer.CustomRecording,
        )
    }
}
