package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The single routing table both sheets consume (catalogue §3 38-42):
 * pinning every row here means a drift in either the guide's or the panel's
 * sheet can only come from a change to this table. Every sheet row is LIVE:
 * the table is total, so no row can reach a coming-soon placeholder through
 * routing anymore (the last six — the group/bulk tools — shipped with the
 * custom-groups slice).
 */
class PlayerMenuRoutingTest {
    @Test
    fun `every sheet row routes exactly per the captured reference`() {
        val expected =
            mapOf(
                PlayerMenuItem.SEARCH to PlayerMenuRoute.SEARCH,
                PlayerMenuItem.SETTINGS to PlayerMenuRoute.SETTINGS,
                PlayerMenuItem.OPEN_IN_EXTERNAL_PLAYER to PlayerMenuRoute.EXTERNAL_PLAYER,
                PlayerMenuItem.RECORD to PlayerMenuRoute.RECORD,
                PlayerMenuItem.CUSTOM_RECORDING to PlayerMenuRoute.CUSTOM_RECORDING,
                PlayerMenuItem.ADD_TO_MY_LIST to PlayerMenuRoute.MY_LIST_TOGGLE,
                PlayerMenuItem.PROGRAM_DESCRIPTION to PlayerMenuRoute.DESCRIPTION,
                PlayerMenuItem.ADD_TO_FAVORITES to PlayerMenuRoute.TOGGLE_FAVORITE,
                PlayerMenuItem.BLOCK_CHANNEL to PlayerMenuRoute.TOGGLE_BLOCK,
                PlayerMenuItem.HIDE_CHANNEL to PlayerMenuRoute.HIDE_CHANNEL,
                PlayerMenuItem.ASSIGN_EPG to PlayerMenuRoute.ASSIGN_EPG,
                PlayerMenuItem.CHANNEL_OPTIONS to PlayerMenuRoute.CHANNEL_OPTIONS,
                PlayerMenuItem.MANAGE_FAVORITES to PlayerMenuRoute.MANAGE_FAVORITES,
                PlayerMenuItem.MANAGE_BLOCKING to PlayerMenuRoute.MANAGE_BLOCKING,
                PlayerMenuItem.MANAGE_VISIBILITY to PlayerMenuRoute.MANAGE_VISIBILITY,
                PlayerMenuItem.REORDER_CHANNELS to PlayerMenuRoute.REORDER_CHANNELS,
                PlayerMenuItem.COPY_CHANNELS to PlayerMenuRoute.COPY_CHANNELS,
                PlayerMenuItem.CREATE_GROUP to PlayerMenuRoute.CREATE_GROUP,
                PlayerMenuItem.GROUP_OPTIONS to PlayerMenuRoute.GROUP_OPTIONS,
            )

        assertEquals(expected, PlayerMenuItem.entries.associateWith(PlayerMenuRouting::routeOf))
    }

    @Test
    fun `the six group and bulk rows route through the group-tool set`() {
        val expected =
            setOf(
                PlayerMenuRoute.CREATE_GROUP,
                PlayerMenuRoute.GROUP_OPTIONS,
                PlayerMenuRoute.COPY_CHANNELS,
                PlayerMenuRoute.ASSIGN_EPG,
                PlayerMenuRoute.MANAGE_BLOCKING,
                PlayerMenuRoute.MANAGE_VISIBILITY,
            )

        assertEquals(expected, PlayerMenuRouting.GROUP_TOOL_ROUTES)
    }
}
