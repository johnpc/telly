package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackKeyPolicyTest {
    private fun at(
        overlay: PlaybackOverlay,
        key: PlaybackKey,
    ): PlaybackCommand? = PlaybackKeyPolicy.commandFor(overlay, key)

    @Test
    fun `bare playback follows the catalogue key map`() {
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.None, PlaybackKey.OK))
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.None, PlaybackKey.DOWN))
        assertEquals(PlaybackCommand.OpenPanelAtPrevious, at(PlaybackOverlay.None, PlaybackKey.UP))
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.None, PlaybackKey.CHANNEL_UP))
        assertEquals(PlaybackCommand.Zap(-1), at(PlaybackOverlay.None, PlaybackKey.CHANNEL_DOWN))
        assertEquals(PlaybackCommand.OpenMenu, at(PlaybackOverlay.None, PlaybackKey.LONG_OK))
        assertEquals(PlaybackCommand.OpenMenu, at(PlaybackOverlay.None, PlaybackKey.MENU))
        // Device-verified BACK chain: from clean playback BACK returns to the TV guide.
        assertEquals(PlaybackCommand.ExitToGuide, at(PlaybackOverlay.None, PlaybackKey.BACK))
    }

    @Test
    fun `left and right do nothing at bare playback per the device answers`() {
        assertNull(at(PlaybackOverlay.None, PlaybackKey.LEFT))
        assertNull(at(PlaybackOverlay.None, PlaybackKey.RIGHT))
    }

    @Test
    fun `the info overlay dismisses on back, opens the menu and keeps zapping`() {
        assertEquals(PlaybackCommand.Dismiss, at(PlaybackOverlay.Info, PlaybackKey.BACK))
        assertEquals(PlaybackCommand.OpenMenu, at(PlaybackOverlay.Info, PlaybackKey.LONG_OK))
        assertEquals(PlaybackCommand.OpenMenu, at(PlaybackOverlay.Info, PlaybackKey.MENU))
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.Info, PlaybackKey.CHANNEL_UP))
        assertEquals(PlaybackCommand.Zap(-1), at(PlaybackOverlay.Info, PlaybackKey.CHANNEL_DOWN))
        assertNull(at(PlaybackOverlay.Info, PlaybackKey.LEFT))
        assertNull(at(PlaybackOverlay.Info, PlaybackKey.OK))
    }

    @Test
    fun `panels and menus only react to back`() {
        assertEquals(PlaybackCommand.Dismiss, at(PlaybackOverlay.Panel, PlaybackKey.BACK))
        assertEquals(PlaybackCommand.Dismiss, at(PlaybackOverlay.Menu, PlaybackKey.BACK))
        assertEquals(PlaybackCommand.Dismiss, at(PlaybackOverlay.ComingSoon("Record"), PlaybackKey.BACK))
        assertNull(at(PlaybackOverlay.Panel, PlaybackKey.OK))
        assertNull(at(PlaybackOverlay.Menu, PlaybackKey.UP))
    }

    @Test
    fun `back from a channel menu returns to the panel`() {
        assertEquals(PlaybackCommand.BackToPanel, at(PlaybackOverlay.ChannelMenu(1), PlaybackKey.BACK))
        assertNull(at(PlaybackOverlay.ChannelMenu(1), PlaybackKey.OK))
    }
}
