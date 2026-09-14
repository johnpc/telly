package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** BACK from a pushed screen re-lands sheet focus on the row that pushed it. */
class PlayerMenuFocusTest {
    private val focus = PlayerMenuFocus()

    @Test
    fun `rows that push a screen remember themselves`() {
        listOf(
            PlayerMenuItem.PROGRAM_DESCRIPTION,
            PlayerMenuItem.RECORD,
            PlayerMenuItem.ASSIGN_EPG,
        ).forEach { item ->
            focus.onActivated(item)
            assertEquals(item, focus.restore)
        }
    }

    @Test
    fun `rows that leave the sheet clear the memory`() {
        listOf(
            PlayerMenuItem.SEARCH,
            PlayerMenuItem.SETTINGS,
            PlayerMenuItem.ADD_TO_FAVORITES,
            PlayerMenuItem.HIDE_CHANNEL,
            // Channel options replaces the sheet; BACK never returns to it
            // (ref-round6 §A), so it must not leave a restore target either.
            PlayerMenuItem.CHANNEL_OPTIONS,
        ).forEach { item ->
            focus.onActivated(PlayerMenuItem.PROGRAM_DESCRIPTION)
            focus.onActivated(item)
            assertNull(focus.restore)
        }
    }

    @Test
    fun `a fresh sheet clears the memory`() {
        focus.onActivated(PlayerMenuItem.PROGRAM_DESCRIPTION)

        focus.clear()

        assertNull(focus.restore)
    }
}
