package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackKeyPolicyTest {
    private fun at(
        overlay: PlaybackOverlay,
        key: PlaybackKey,
        keymap: PlayerKeymap = PlayerKeymap(),
    ): PlaybackCommand? = PlaybackKeyPolicy.commandFor(overlay, key, keymap)

    @Test
    fun `bare playback follows the device-verified key map`() {
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.None, PlaybackKey.OK))
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.None, PlaybackKey.DOWN))
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.None, PlaybackKey.UP))
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.None, PlaybackKey.CHANNEL_UP))
        assertEquals(PlaybackCommand.Zap(-1), at(PlaybackOverlay.None, PlaybackKey.CHANNEL_DOWN))
        assertEquals(PlaybackCommand.OpenQuickBar, at(PlaybackOverlay.None, PlaybackKey.LONG_OK))
        assertEquals(PlaybackCommand.OpenQuickBar, at(PlaybackOverlay.None, PlaybackKey.MENU))
        // Device-verified BACK chain: from clean playback BACK returns to the TV guide.
        assertEquals(PlaybackCommand.ExitToGuide, at(PlaybackOverlay.None, PlaybackKey.BACK))
    }

    @Test
    fun `left and right do nothing at bare playback per the device answers`() {
        assertNull(at(PlaybackOverlay.None, PlaybackKey.LEFT))
        assertNull(at(PlaybackOverlay.None, PlaybackKey.RIGHT))
    }

    @Test
    fun `the info overlay dismisses on back, opens the quick-bar and keeps zapping`() {
        assertEquals(PlaybackCommand.Dismiss, at(PlaybackOverlay.Info, PlaybackKey.BACK))
        assertEquals(PlaybackCommand.OpenQuickBar, at(PlaybackOverlay.Info, PlaybackKey.LONG_OK))
        assertEquals(PlaybackCommand.OpenQuickBar, at(PlaybackOverlay.Info, PlaybackKey.MENU))
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.Info, PlaybackKey.CHANNEL_UP))
        assertEquals(PlaybackCommand.Zap(-1), at(PlaybackOverlay.Info, PlaybackKey.CHANNEL_DOWN))
        assertNull(at(PlaybackOverlay.Info, PlaybackKey.LEFT))
        assertNull(at(PlaybackOverlay.Info, PlaybackKey.OK))
    }

    @Test
    fun `a second up expands the transport row and a third does nothing`() {
        assertEquals(PlaybackCommand.ShowTransport, at(PlaybackOverlay.Info, PlaybackKey.UP))
        assertNull(at(PlaybackOverlay.InfoTransport, PlaybackKey.UP))
        assertEquals(PlaybackCommand.Dismiss, at(PlaybackOverlay.InfoTransport, PlaybackKey.BACK))
        assertEquals(PlaybackCommand.OpenQuickBar, at(PlaybackOverlay.InfoTransport, PlaybackKey.MENU))
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.InfoTransport, PlaybackKey.CHANNEL_UP))
    }

    @Test
    fun `the zap overlay promotes to the full info overlay and keeps zapping`() {
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.ZapInfo, PlaybackKey.OK))
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.ZapInfo, PlaybackKey.DOWN))
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.ZapInfo, PlaybackKey.UP))
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.ZapInfo, PlaybackKey.CHANNEL_UP))
        assertEquals(PlaybackCommand.OpenQuickBar, at(PlaybackOverlay.ZapInfo, PlaybackKey.MENU))
        assertEquals(PlaybackCommand.Dismiss, at(PlaybackOverlay.ZapInfo, PlaybackKey.BACK))
        assertNull(at(PlaybackOverlay.ZapInfo, PlaybackKey.LEFT))
    }

    @Test
    fun `panels, quick-bar and placeholders only react to back`() {
        assertEquals(PlaybackCommand.Dismiss, at(PlaybackOverlay.Panel, PlaybackKey.BACK))
        assertEquals(PlaybackCommand.Dismiss, at(PlaybackOverlay.QuickBar, PlaybackKey.BACK))
        assertEquals(
            PlaybackCommand.PopTo(PlaybackOverlay.None),
            at(PlaybackOverlay.ComingSoon("Recordings"), PlaybackKey.BACK),
        )
        assertNull(at(PlaybackOverlay.Panel, PlaybackKey.OK))
        assertNull(at(PlaybackOverlay.QuickBar, PlaybackKey.UP))
    }

    @Test
    fun `back from a channel menu returns to the panel`() {
        assertEquals(PlaybackCommand.BackToPanel, at(PlaybackOverlay.ChannelMenu(1), PlaybackKey.BACK))
        assertNull(at(PlaybackOverlay.ChannelMenu(1), PlaybackKey.OK))
    }

    @Test
    fun `back from a sheet-pushed screen pops one level back to the sheet`() {
        val sheet = PlaybackOverlay.ChannelMenu(1)
        val pushed =
            listOf(
                PlaybackOverlay.ComingSoon("Record", back = sheet),
                PlaybackOverlay.Description("T", "D", back = sheet),
                PlaybackOverlay.ComingSoon("Assign EPG", back = sheet),
            )
        pushed.forEach { overlay ->
            assertEquals(PlaybackCommand.PopTo(sheet), at(overlay, PlaybackKey.BACK))
            assertNull(at(overlay, PlaybackKey.OK))
        }
        // Channel options replaced the sheet (ref-round6 §A): its back is
        // the panel, so BACK lands there directly, never on the sheet.
        val pane = PlaybackOverlay.ChannelOptions("News One", back = PlaybackOverlay.Panel)
        assertEquals(PlaybackCommand.PopTo(PlaybackOverlay.Panel), at(pane, PlaybackKey.BACK))
        assertNull(at(pane, PlaybackKey.OK))
        // A locked pane row opens the coming-soon placeholder over the pane.
        val comingSoonOverPane = PlaybackOverlay.ComingSoon("Channel options", back = pane)
        assertEquals(PlaybackCommand.PopTo(pane), at(comingSoonOverPane, PlaybackKey.BACK))
    }

    @Test
    fun `ok remaps to the channels list or to nothing at bare playback`() {
        val panel = PlayerKeymap(ok = PlayerOkAction.CHANNELS_LIST)
        assertEquals(PlaybackCommand.OpenPanel, at(PlaybackOverlay.None, PlaybackKey.OK, panel))
        assertNull(at(PlaybackOverlay.None, PlaybackKey.OK, PlayerKeymap(ok = PlayerOkAction.NOTHING)))
        // The remap never leaks into other keys or into the info overlay.
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.None, PlaybackKey.DOWN, panel))
        assertNull(at(PlaybackOverlay.Info, PlaybackKey.OK, panel))
    }

    @Test
    fun `up and down remap to zapping or to nothing at bare playback`() {
        val zap = PlayerKeymap(upDown = PlayerUpDownAction.SWITCH_CHANNELS)
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.None, PlaybackKey.UP, zap))
        assertEquals(PlaybackCommand.Zap(-1), at(PlaybackOverlay.None, PlaybackKey.DOWN, zap))
        val off = PlayerKeymap(upDown = PlayerUpDownAction.NOTHING)
        assertNull(at(PlaybackOverlay.None, PlaybackKey.UP, off))
        assertNull(at(PlaybackOverlay.None, PlaybackKey.DOWN, off))
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.None, PlaybackKey.OK, off))
    }

    @Test
    fun `left and right remap to zapping at bare playback`() {
        val zap = PlayerKeymap(leftRight = PlayerLeftRightAction.SWITCH_CHANNELS)
        assertEquals(PlaybackCommand.Zap(-1), at(PlaybackOverlay.None, PlaybackKey.LEFT, zap))
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.None, PlaybackKey.RIGHT, zap))
        // Inside the info overlay LEFT/RIGHT keep moving the real focus.
        assertNull(at(PlaybackOverlay.Info, PlaybackKey.LEFT, zap))
    }

    @Test
    fun `long ok remaps to the channels list while menu keeps the quick-bar`() {
        val panel = PlayerKeymap(longOk = PlayerLongOkAction.CHANNELS_LIST)
        assertEquals(PlaybackCommand.OpenPanel, at(PlaybackOverlay.None, PlaybackKey.LONG_OK, panel))
        assertEquals(PlaybackCommand.OpenQuickBar, at(PlaybackOverlay.None, PlaybackKey.MENU, panel))
        assertEquals(PlaybackCommand.OpenPanel, at(PlaybackOverlay.ZapInfo, PlaybackKey.LONG_OK, panel))
    }

    @Test
    fun `remapped zap keys keep zapping through the transient zap overlay`() {
        val zap =
            PlayerKeymap(
                upDown = PlayerUpDownAction.SWITCH_CHANNELS,
                leftRight = PlayerLeftRightAction.SWITCH_CHANNELS,
            )
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.ZapInfo, PlaybackKey.UP, zap))
        assertEquals(PlaybackCommand.Zap(-1), at(PlaybackOverlay.ZapInfo, PlaybackKey.DOWN, zap))
        assertEquals(PlaybackCommand.Zap(-1), at(PlaybackOverlay.ZapInfo, PlaybackKey.LEFT, zap))
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.ZapInfo, PlaybackKey.RIGHT, zap))
        // OK still promotes to the info overlay; a panel remap opens the panel.
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.ZapInfo, PlaybackKey.OK, zap))
        assertEquals(
            PlaybackCommand.OpenPanel,
            at(PlaybackOverlay.ZapInfo, PlaybackKey.OK, PlayerKeymap(ok = PlayerOkAction.CHANNELS_LIST)),
        )
        // Keys mapped to nothing still promote the zap overlay like today.
        assertEquals(
            PlaybackCommand.ShowInfo,
            at(PlaybackOverlay.ZapInfo, PlaybackKey.UP, PlayerKeymap(upDown = PlayerUpDownAction.NOTHING)),
        )
    }
}
