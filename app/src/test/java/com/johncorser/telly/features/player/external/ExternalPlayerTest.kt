package com.johncorser.telly.features.player.external

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The external-player decision logic (the launch itself is injected). */
class ExternalPlayerTest {
    private var enabled = false
    private var launchResult = true
    private val launched = mutableListOf<String>()
    private val player =
        ExternalPlayer(
            enabledForTuning = { enabled },
            launch = { url ->
                launched += url
                launchResult
            },
        )

    @Test
    fun `open fires the chooser for a usable url regardless of the setting`() {
        assertTrue(player.open("http://s/1.ts"))
        assertEquals(listOf("http://s/1.ts"), launched)
    }

    @Test
    fun `open refuses a blank url without launching`() {
        assertFalse(player.open(""))
        assertTrue(launched.isEmpty())
    }

    @Test
    fun `maybeLaunch only fires while the setting is on`() {
        assertFalse(player.maybeLaunch("http://s/1.ts"))
        assertTrue(launched.isEmpty())

        enabled = true
        assertTrue(player.maybeLaunch("http://s/1.ts"))
        assertEquals(listOf("http://s/1.ts"), launched)
    }

    @Test
    fun `a failed launch - no handler - reports false so tunes fall back`() {
        enabled = true
        launchResult = false
        assertFalse(player.maybeLaunch("http://s/1.ts"))
    }

    @Test
    fun `the OFF default is inert`() {
        assertFalse(ExternalPlayer.OFF.open("http://s/1.ts"))
        assertFalse(ExternalPlayer.OFF.maybeLaunch("http://s/1.ts"))
    }

    @Test
    fun `a per-channel override wins over the global setting in both directions`() {
        // Channel says On while the global is Off: the tune hands off.
        assertTrue(player.maybeLaunch("http://s/1.ts", channelOverride = true))
        assertEquals(listOf("http://s/1.ts"), launched)

        // Channel says Off while the global is On: the tune stays internal.
        enabled = true
        assertFalse(player.maybeLaunch("http://s/1.ts", channelOverride = false))
        assertEquals(1, launched.size)

        // No override: the global decides (surfaced via enabledByDefault too).
        assertTrue(player.maybeLaunch("http://s/1.ts", channelOverride = null))
        assertTrue(player.enabledByDefault)
    }

    @Test
    fun `the override parser maps On-Off raws and follows the global otherwise`() {
        assertEquals(true, ExternalPlayerSetting.overrideOf("On"))
        assertEquals(false, ExternalPlayerSetting.overrideOf("Off"))
        assertEquals(null, ExternalPlayerSetting.overrideOf(null))
        assertEquals(null, ExternalPlayerSetting.overrideOf("Always"))
    }

    @Test
    fun `the setting parser knows On from everything else`() {
        assertEquals(listOf("Off", "On"), ExternalPlayerSetting.options)
        assertTrue(ExternalPlayerSetting.isOn("On"))
        assertFalse(ExternalPlayerSetting.isOn("Off"))
        assertFalse(ExternalPlayerSetting.isOn("No"))
    }
}
