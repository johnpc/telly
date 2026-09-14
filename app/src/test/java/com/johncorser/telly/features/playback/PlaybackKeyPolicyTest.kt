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
    fun `bare playback follows the device-verified key map`() {
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.None, PlaybackKey.OK))
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.None, PlaybackKey.DOWN))
        assertEquals(PlaybackCommand.ShowInfo, at(PlaybackOverlay.None, PlaybackKey.UP))
        assertEquals(PlaybackCommand.Zap(+1), at(PlaybackOverlay.None, PlaybackKey.CHANNEL_UP))
        assertEquals(PlaybackCommand.Zap(-1), at(PlaybackOverlay.None, PlaybackKey.CHANNEL_DOWN))
        assertEquals(PlaybackCommand.OpenQuickBar, at(PlaybackOverlay.None, PlaybackKey.LONG_OK))
        assertEquals(PlaybackCommand.OpenQuickBar, at(PlaybackOverlay.None, PlaybackKey.MENU))
        assertEquals(PlaybackCommand.OpenPanelAtCurrent, at(PlaybackOverlay.None, PlaybackKey.BACK))
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
        assertEquals(PlaybackCommand.Dismiss, at(PlaybackOverlay.ComingSoon("Record"), PlaybackKey.BACK))
        assertNull(at(PlaybackOverlay.Panel, PlaybackKey.OK))
        assertNull(at(PlaybackOverlay.QuickBar, PlaybackKey.UP))
    }

    @Test
    fun `back from a channel menu returns to the panel`() {
        assertEquals(PlaybackCommand.BackToPanel, at(PlaybackOverlay.ChannelMenu(1), PlaybackKey.BACK))
        assertNull(at(PlaybackOverlay.ChannelMenu(1), PlaybackKey.OK))
    }
}
