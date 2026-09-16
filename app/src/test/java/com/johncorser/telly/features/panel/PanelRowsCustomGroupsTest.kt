package com.johncorser.telly.features.panel

import com.johncorser.telly.features.groups.CustomGroup
import com.johncorser.telly.features.playlist.ChannelImporter
import com.johncorser.telly.features.playlist.M3uChannel
import com.johncorser.telly.testutil.testChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Custom groups in the group columns: order, membership, refresh survival. */
class PanelRowsCustomGroupsTest {
    private val channels =
        listOf(
            testChannel(1, 1, "News One", group = "News"),
            testChannel(2, 2, "Sports Arena", group = "Sports"),
            testChannel(3, 3, "Movie House", group = "Movies"),
        )
    private val picks = CustomGroup(1, "Picks", members = setOf("tvg-2"))

    @Test
    fun `custom groups append after the playlist groups`() {
        assertEquals(
            listOf("Favorites", "All channels", "News", "Sports", "Movies", "Picks"),
            PanelRows.groupNames(channels, listOf(picks)),
        )
    }

    @Test
    fun `a custom group filters its members by refresh-stable key`() {
        assertEquals(
            listOf(2L),
            PanelRows.channelsIn(channels, "Picks", listOf(picks)).map { it.id },
        )
    }

    @Test
    fun `an unknown group yields no channels`() {
        assertTrue(PanelRows.channelsIn(channels, "Nope", listOf(picks)).isEmpty())
    }

    @Test
    fun `a custom group named like a playlist group resolves to the playlist group`() {
        val shadow = CustomGroup(2, "News", members = setOf("tvg-2"))

        assertEquals(
            listOf(1L),
            PanelRows.channelsIn(channels, "News", listOf(shadow)).map { it.id },
        )
    }

    @Test
    fun `membership survives a playlist refresh that replaces the channel rows`() {
        val parsed =
            channels.map { row ->
                M3uChannel(
                    title = row.source.name,
                    streamUrl = row.source.streamUrl,
                    tvgId = row.source.tvgId,
                    tvgName = null,
                    tvgLogo = row.source.logoUrl,
                    groupTitle = row.source.groupTitle,
                )
            }
        val reimported =
            ChannelImporter
                .import(playlistId = 1, parsed = parsed.reversed(), previous = channels)
                .mapIndexed { index, row -> row.copy(id = 100L + index) }

        assertEquals(
            listOf("Sports Arena"),
            PanelRows.channelsIn(reimported, "Picks", listOf(picks)).map { it.source.name },
        )
    }
}
