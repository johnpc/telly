package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playback.PlaybackKey
import com.johncorser.telly.features.playback.PlaybackOverlay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatchupKeyPolicyTest {
    private val back = CatchupCommand.Seek(-CatchupSkip.DEFAULT_BACK_MS)
    private val forward = CatchupCommand.Seek(CatchupSkip.DEFAULT_FORWARD_MS)
    private val rewindLive = CatchupCommand.RewindLive(CatchupSkip.DEFAULT_BACK_MS)

    private fun playing(
        key: PlaybackKey,
        keys: CatchupSeekKeys = CatchupSeekKeys(),
        overlay: PlaybackOverlay = PlaybackOverlay.None,
    ): CatchupCommand? = CatchupKeyPolicy.commandFor(overlay, key, CatchupMode.PLAYING, keys)

    private fun live(
        key: PlaybackKey,
        keys: CatchupSeekKeys = CatchupSeekKeys(),
        overlay: PlaybackOverlay = PlaybackOverlay.None,
    ): CatchupCommand? = CatchupKeyPolicy.commandFor(overlay, key, CatchupMode.LIVE_CAPABLE, keys)

    @Test
    fun `without a catch-up channel every key falls through to the live map`() {
        PlaybackKey.entries.forEach { key ->
            assertNull(CatchupKeyPolicy.commandFor(PlaybackOverlay.None, key, CatchupMode.NONE, CatchupSeekKeys()))
        }
    }

    @Test
    fun `rw and ff seek during catch-up while the default toggle is on`() {
        assertEquals(back, playing(PlaybackKey.REWIND))
        assertEquals(forward, playing(PlaybackKey.FAST_FORWARD))
        assertNull(playing(PlaybackKey.REWIND, CatchupSeekKeys(rwFf = false)))
        assertNull(playing(PlaybackKey.FAST_FORWARD, CatchupSeekKeys(rwFf = false)))
    }

    @Test
    fun `rw and ff also seek over the transient overlays`() {
        assertEquals(back, playing(PlaybackKey.REWIND, overlay = PlaybackOverlay.Info))
        assertEquals(forward, playing(PlaybackKey.FAST_FORWARD, overlay = PlaybackOverlay.InfoTransport))
        assertEquals(back, playing(PlaybackKey.REWIND, overlay = PlaybackOverlay.ZapInfo))
        assertNull(playing(PlaybackKey.REWIND, overlay = PlaybackOverlay.Panel))
    }

    @Test
    fun `left-right seeking honors its toggle and only at bare playback`() {
        val enabled = CatchupSeekKeys(leftRight = true)

        assertEquals(back, playing(PlaybackKey.LEFT, enabled))
        assertEquals(forward, playing(PlaybackKey.RIGHT, enabled))
        assertNull(playing(PlaybackKey.LEFT))
        assertNull(playing(PlaybackKey.LEFT, enabled, overlay = PlaybackOverlay.Info))
    }

    @Test
    fun `down-up seeking honors its toggle and only at bare playback`() {
        val enabled = CatchupSeekKeys(downUp = true)

        assertEquals(back, playing(PlaybackKey.DOWN, enabled))
        assertEquals(forward, playing(PlaybackKey.UP, enabled))
        assertNull(playing(PlaybackKey.DOWN))
        assertNull(playing(PlaybackKey.UP, enabled, overlay = PlaybackOverlay.Info))
    }

    @Test
    fun `back at bare catch-up playback leaves the mode`() {
        assertEquals(CatchupCommand.Back, playing(PlaybackKey.BACK))
        assertNull(playing(PlaybackKey.BACK, overlay = PlaybackOverlay.Info))
    }

    @Test
    fun `rewind-live toggles jump into catch-up during live playback`() {
        assertEquals(rewindLive, live(PlaybackKey.REWIND, CatchupSeekKeys(rwLive = true)))
        assertEquals(rewindLive, live(PlaybackKey.LEFT, CatchupSeekKeys(leftLive = true)))
        assertEquals(rewindLive, live(PlaybackKey.DOWN, CatchupSeekKeys(downLive = true)))
    }

    @Test
    fun `rewind-live is off by default so live keys keep their meaning`() {
        assertNull(live(PlaybackKey.REWIND))
        assertNull(live(PlaybackKey.LEFT))
        assertNull(live(PlaybackKey.DOWN))
    }

    @Test
    fun `left and down rewind-live apply at bare playback only, rw also over overlays`() {
        assertEquals(rewindLive, live(PlaybackKey.REWIND, CatchupSeekKeys(rwLive = true), PlaybackOverlay.Info))
        assertNull(live(PlaybackKey.LEFT, CatchupSeekKeys(leftLive = true), PlaybackOverlay.Info))
        assertNull(live(PlaybackKey.DOWN, CatchupSeekKeys(downLive = true), PlaybackOverlay.Info))
        assertNull(live(PlaybackKey.REWIND, CatchupSeekKeys(rwLive = true), PlaybackOverlay.Panel))
    }
}
