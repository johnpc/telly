package com.johncorser.telly.features.playback.tracks

import com.johncorser.telly.features.playback.PlaybackCommand
import com.johncorser.telly.features.playback.PlaybackOverlay
import com.johncorser.telly.features.playback.QuickBarAction
import com.johncorser.telly.features.player.tracks.TextTrack
import com.johncorser.telly.features.player.tracks.TrackSnapshot
import com.johncorser.telly.testutil.FakeTrackFacade
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackPickerControllerTest {
    /** The picker expresses every overlay move as a command (the one overlay writer executes them). */
    private val commands = mutableListOf<PlaybackCommand>()
    private val tracks = FakeTrackFacade(TrackSnapshot(texts = listOf(TextTrack("2:0", "en"))))
    private val controller = TrackPickerController(tracks) { commands += it }

    @Test
    fun `each stream slot opens its picker overlay`() {
        controller.open(QuickBarAction.RESOLUTION)
        assertEquals(PlaybackCommand.Show(PlaybackOverlay.TrackPicker(TrackPickerKind.VIDEO)), commands.last())

        controller.open(QuickBarAction.AUDIO)
        assertEquals(PlaybackCommand.Show(PlaybackOverlay.TrackPicker(TrackPickerKind.AUDIO)), commands.last())

        controller.open(QuickBarAction.LATENCY)
        assertEquals(PlaybackCommand.Show(PlaybackOverlay.TrackPicker(TrackPickerKind.SYNC)), commands.last())

        controller.open(QuickBarAction.SUBTITLES)
        assertEquals(PlaybackCommand.Show(PlaybackOverlay.TrackPicker(TrackPickerKind.SUBTITLES)), commands.last())
    }

    @Test
    fun `picking an option applies it and closes the picker`() {
        controller.open(QuickBarAction.SUBTITLES)

        controller.onRow(TrackPickerKind.SUBTITLES, "2:0")

        assertEquals(listOf("2:0"), tracks.textSelections)
        assertEquals(PlaybackCommand.Dismiss, commands.last())
        assertEquals("English", controller.subtitleLabel())
    }

    @Test
    fun `sync steps keep the stepper open and update the slot label`() {
        controller.open(QuickBarAction.LATENCY)
        val plus50 = controller.rows(TrackPickerKind.SYNC).first { it.label == "+50 ms" }.id

        controller.onRow(TrackPickerKind.SYNC, plus50)

        assertEquals(PlaybackCommand.Show(PlaybackOverlay.TrackPicker(TrackPickerKind.SYNC)), commands.last())
        assertEquals("+50 ms", controller.syncLabel())
    }

    @Test
    fun `labels default to a zero offset and captions off`() {
        assertEquals("0 ms", controller.syncLabel())
        assertEquals("Off", controller.subtitleLabel())
        assertEquals("Auto", controller.rows(TrackPickerKind.VIDEO).single().label)
    }
}
