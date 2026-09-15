package com.johncorser.telly.features.playback

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerKeymapTest {
    private val settings = SettingsRepository(InMemoryKeyValueStore())

    @Test
    fun `an untouched store parses to the device-verified default map`() {
        assertEquals(PlayerKeymap(), PlayerKeymap.from(settings))
    }

    @Test
    fun `the persisted defaults are the default choices' labels`() {
        assertEquals(PlayerOkAction.SHOW_INFO.label, TellySettings.REMOTE_PLAYER_OK.default)
        assertEquals(PlayerUpDownAction.SHOW_INFO.label, TellySettings.REMOTE_PLAYER_UP_DOWN.default)
        assertEquals(PlayerLeftRightAction.NOTHING.label, TellySettings.REMOTE_PLAYER_LEFT_RIGHT.default)
        assertEquals(PlayerLongOkAction.QUICK_MENU.label, TellySettings.REMOTE_PLAYER_LONG_OK.default)
    }

    @Test
    fun `every persisted label parses back to its choice`() {
        settings.set(TellySettings.REMOTE_PLAYER_OK, "Open channels list")
        settings.set(TellySettings.REMOTE_PLAYER_UP_DOWN, "Switch channels")
        settings.set(TellySettings.REMOTE_PLAYER_LEFT_RIGHT, "Switch channels")
        settings.set(TellySettings.REMOTE_PLAYER_LONG_OK, "Open channels list")

        assertEquals(
            PlayerKeymap(
                ok = PlayerOkAction.CHANNELS_LIST,
                upDown = PlayerUpDownAction.SWITCH_CHANNELS,
                leftRight = PlayerLeftRightAction.SWITCH_CHANNELS,
                longOk = PlayerLongOkAction.CHANNELS_LIST,
            ),
            PlayerKeymap.from(settings),
        )
    }

    @Test
    fun `unknown raws fall back to the defaults instead of crashing`() {
        settings.set(TellySettings.REMOTE_PLAYER_OK, "Seek")
        settings.set(TellySettings.REMOTE_PLAYER_UP_DOWN, "")

        assertEquals(PlayerKeymap(), PlayerKeymap.from(settings))
    }
}
