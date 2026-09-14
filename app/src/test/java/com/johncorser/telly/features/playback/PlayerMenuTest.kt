package com.johncorser.telly.features.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlayerMenuTest {
    @Test
    fun `player menu clones the captured sections and rows verbatim`() {
        val sections = PlayerMenu.sections("Business Hour. S1 E7", "News One")

        assertEquals(listOf(null, "Business Hour. S1 E7", "News One", "All channels"), sections.map { it.header })
        assertEquals(listOf("Search", "Settings"), sections[0].items.map { it.label })
        assertEquals(
            listOf("Open in external player", "Record", "Custom recording", "Add to My list", "Program description"),
            sections[1].items.map { it.label },
        )
        assertEquals(
            listOf("Add to Favorites", "Block channel", "Hide channel", "Assign EPG", "Channel options"),
            sections[2].items.map { it.label },
        )
        assertEquals(
            listOf(
                "Manage Favorites",
                "Manage blocking",
                "Manage visibility",
                "Reorder channels",
                "Copy channels",
                "Create group",
                "Group options",
            ),
            sections[3].items.map { it.label },
        )
    }

    @Test
    fun `a missing programme falls back to the no-information header`() {
        assertEquals("No information", PlayerMenu.sections(null, "News One")[1].header)
    }

    @Test
    fun `the favorites row flips its label for favorite channels`() {
        assertEquals("Add to Favorites", PlayerMenuItem.ADD_TO_FAVORITES.labelFor(favorite = false))
        assertEquals("Remove from Favorites", PlayerMenuItem.ADD_TO_FAVORITES.labelFor(favorite = true))
        assertEquals("Hide channel", PlayerMenuItem.HIDE_CHANNEL.labelFor(favorite = true))
    }

    @Test
    fun `flat indexes count section headers as lazy rows`() {
        val sections = PlayerMenu.sections("Business Hour", "News One")

        assertEquals(0, PlayerMenu.flatIndexOf(sections, PlayerMenuItem.SEARCH))
        assertEquals(3, PlayerMenu.flatIndexOf(sections, PlayerMenuItem.OPEN_IN_EXTERNAL_PLAYER))
        assertEquals(7, PlayerMenu.flatIndexOf(sections, PlayerMenuItem.PROGRAM_DESCRIPTION))
        assertEquals(13, PlayerMenu.flatIndexOf(sections, PlayerMenuItem.CHANNEL_OPTIONS))
        assertEquals(21, PlayerMenu.flatIndexOf(sections, PlayerMenuItem.GROUP_OPTIONS))
    }

    @Test
    fun `no premium-only sleep timer or track rows exist in this build`() {
        val labels = PlayerMenuItem.entries.map { it.label }
        assertNull(labels.firstOrNull { it.contains("Sleep") || it.contains("Aspect") || it.contains("Audio track") })
    }
}
