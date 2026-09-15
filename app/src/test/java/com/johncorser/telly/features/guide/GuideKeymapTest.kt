package com.johncorser.telly.features.guide

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import org.junit.Assert.assertEquals
import org.junit.Test

class GuideKeymapTest {
    private val settings = SettingsRepository(InMemoryKeyValueStore())

    @Test
    fun `an untouched store parses to the current grid behavior`() {
        assertEquals(GuideKeymap(), GuideKeymap.from(settings))
    }

    @Test
    fun `the persisted defaults are the default choices' labels`() {
        assertEquals(GuideLeftRightAction.BY_PROGRAMME.label, TellySettings.REMOTE_GUIDE_LEFT_RIGHT.default)
        assertEquals(GuideChannelKeysAction.NOTHING.label, TellySettings.REMOTE_GUIDE_CHANNEL_UP_DOWN.default)
        assertEquals(GuideLongOkAction.CHANNEL_MENU.label, TellySettings.REMOTE_GUIDE_LONG_OK.default)
    }

    @Test
    fun `every persisted label parses back to its choice`() {
        settings.set(TellySettings.REMOTE_GUIDE_LEFT_RIGHT, "Move by page")
        settings.set(TellySettings.REMOTE_GUIDE_CHANNEL_UP_DOWN, "Move by day")
        settings.set(TellySettings.REMOTE_GUIDE_LONG_OK, "Play channel")

        assertEquals(
            GuideKeymap(
                leftRight = GuideLeftRightAction.BY_PAGE,
                channelUpDown = GuideChannelKeysAction.MOVE_BY_DAY,
                longOk = GuideLongOkAction.PLAY_CHANNEL,
            ),
            GuideKeymap.from(settings),
        )

        settings.set(TellySettings.REMOTE_GUIDE_CHANNEL_UP_DOWN, "Page the channel list")
        assertEquals(GuideChannelKeysAction.PAGE_CHANNELS, GuideKeymap.from(settings).channelUpDown)
    }

    @Test
    fun `unknown raws fall back to the defaults instead of crashing`() {
        settings.set(TellySettings.REMOTE_GUIDE_LEFT_RIGHT, "Warp")
        settings.set(TellySettings.REMOTE_GUIDE_LONG_OK, "")

        assertEquals(GuideKeymap(), GuideKeymap.from(settings))
    }
}
