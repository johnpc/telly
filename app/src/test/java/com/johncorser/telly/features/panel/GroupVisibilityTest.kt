package com.johncorser.telly.features.panel

import org.junit.Assert.assertEquals
import org.junit.Test

class GroupVisibilityTest {
    private val groups = listOf("Favorites", "All channels", "News", "Movies")

    @Test
    fun `the default shows every group like today`() {
        assertEquals(groups, GroupVisibility.DEFAULT.filter(groups))
    }

    @Test
    fun `hiding Favorites removes only the synthetic Favorites group`() {
        assertEquals(
            listOf("All channels", "News", "Movies"),
            GroupVisibility(favorites = false).filter(groups),
        )
    }

    @Test
    fun `hiding All channels removes only the synthetic All channels group`() {
        assertEquals(
            listOf("Favorites", "News", "Movies"),
            GroupVisibility(allChannels = false).filter(groups),
        )
    }

    @Test
    fun `hiding both keeps the playlist groups`() {
        assertEquals(
            listOf("News", "Movies"),
            GroupVisibility(allChannels = false, favorites = false).filter(groups),
        )
    }
}
