package com.johncorser.telly.features.playback.tracks

import com.johncorser.telly.features.playback.OverlayState
import com.johncorser.telly.features.playback.PlaybackOverlay
import com.johncorser.telly.features.playback.QuickBarAction
import com.johncorser.telly.features.player.tracks.TextTrack
import com.johncorser.telly.features.player.tracks.TrackSnapshot
import com.johncorser.telly.testutil.FakeTrackFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackPickerControllerTest {
    private val overlays = OverlayState(CoroutineScope(Dispatchers.Unconfined))
    private val tracks = FakeTrackFacade(TrackSnapshot(texts = listOf(TextTrack("2:0", "en"))))
    private val controller = TrackPickerController(tracks, overlays)

    @Test
    fun `each stream slot opens its picker overlay`() {
        controller.open(QuickBarAction.RESOLUTION)
        assertEquals(PlaybackOverlay.TrackPicker(TrackPickerKind.VIDEO), overlays.value)

        controller.open(QuickBarAction.AUDIO)
        assertEquals(PlaybackOverlay.TrackPicker(TrackPickerKind.AUDIO), overlays.value)

        controller.open(QuickBarAction.LATENCY)
        assertEquals(PlaybackOverlay.TrackPicker(TrackPickerKind.SYNC), overlays.value)

        controller.open(QuickBarAction.SUBTITLES)
        assertEquals(PlaybackOverlay.TrackPicker(TrackPickerKind.SUBTITLES), overlays.value)
    }

    @Test
    fun `picking an option applies it and closes the picker`() {
        controller.open(QuickBarAction.SUBTITLES)

        controller.onRow(TrackPickerKind.SUBTITLES, "2:0")

        assertEquals(listOf("2:0"), tracks.textSelections)
        assertEquals(PlaybackOverlay.None, overlays.value)
        assertEquals("English", controller.subtitleLabel())
    }

    @Test
    fun `sync steps keep the stepper open and update the slot label`() {
        controller.open(QuickBarAction.LATENCY)
        val plus50 = controller.rows(TrackPickerKind.SYNC).first { it.label == "+50 ms" }.id

        controller.onRow(TrackPickerKind.SYNC, plus50)

        assertEquals(PlaybackOverlay.TrackPicker(TrackPickerKind.SYNC), overlays.value)
        assertEquals("+50 ms", controller.syncLabel())
    }

    @Test
    fun `labels default to a zero offset and captions off`() {
        assertEquals("0 ms", controller.syncLabel())
        assertEquals("Off", controller.subtitleLabel())
        assertEquals("Auto", controller.rows(TrackPickerKind.VIDEO).single().label)
    }
}
