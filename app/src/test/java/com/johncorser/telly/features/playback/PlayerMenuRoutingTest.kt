package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The single routing table both sheets consume (catalogue §3 38-42 +
 * captures 28/31/41): pinning every row here means a drift in either the
 * guide's or the panel's sheet can only come from a change to this table.
 */
class PlayerMenuRoutingTest {
    @Test
    fun `every sheet row routes exactly per the captured reference`() {
        val expected =
            mapOf(
                PlayerMenuItem.SEARCH to PlayerMenuRoute.SEARCH,
                PlayerMenuItem.SETTINGS to PlayerMenuRoute.SETTINGS,
                PlayerMenuItem.OPEN_IN_EXTERNAL_PLAYER to PlayerMenuRoute.PAYWALL,
                PlayerMenuItem.RECORD to PlayerMenuRoute.PAYWALL,
                PlayerMenuItem.CUSTOM_RECORDING to PlayerMenuRoute.PAYWALL,
                PlayerMenuItem.ADD_TO_MY_LIST to PlayerMenuRoute.PAYWALL,
                PlayerMenuItem.PROGRAM_DESCRIPTION to PlayerMenuRoute.DESCRIPTION,
                PlayerMenuItem.ADD_TO_FAVORITES to PlayerMenuRoute.TOGGLE_FAVORITE,
                PlayerMenuItem.BLOCK_CHANNEL to PlayerMenuRoute.PAYWALL,
                PlayerMenuItem.HIDE_CHANNEL to PlayerMenuRoute.HIDE_CHANNEL,
                PlayerMenuItem.ASSIGN_EPG to PlayerMenuRoute.COMING_SOON,
                PlayerMenuItem.CHANNEL_OPTIONS to PlayerMenuRoute.CHANNEL_OPTIONS,
                PlayerMenuItem.MANAGE_FAVORITES to PlayerMenuRoute.PAYWALL,
                PlayerMenuItem.MANAGE_BLOCKING to PlayerMenuRoute.COMING_SOON,
                PlayerMenuItem.MANAGE_VISIBILITY to PlayerMenuRoute.COMING_SOON,
                PlayerMenuItem.REORDER_CHANNELS to PlayerMenuRoute.PAYWALL,
                PlayerMenuItem.COPY_CHANNELS to PlayerMenuRoute.COMING_SOON,
                PlayerMenuItem.CREATE_GROUP to PlayerMenuRoute.COMING_SOON,
                PlayerMenuItem.GROUP_OPTIONS to PlayerMenuRoute.COMING_SOON,
            )

        assertEquals(expected, PlayerMenuItem.entries.associateWith(PlayerMenuRouting::routeOf))
    }
}
