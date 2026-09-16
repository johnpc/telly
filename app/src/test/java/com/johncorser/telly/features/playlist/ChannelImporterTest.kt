package com.johncorser.telly.features.playlist

import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.ChannelFlags
import com.johncorser.telly.features.playlist.db.ChannelOverrides
import com.johncorser.telly.features.playlist.db.ChannelSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChannelImporterTest {
    private fun channel(
        title: String,
        streamUrl: String = "http://s/$title.ts",
        tvgId: String? = null,
    ) = M3uChannel(title = title, streamUrl = streamUrl, tvgId = tvgId, groupTitle = "News")

    @Test
    fun `numbers channels sequentially from playlist order`() {
        val rows =
            ChannelImporter.import(
                playlistId = 7,
                parsed = listOf(channel("A"), channel("B"), channel("C")),
                previous = emptyList(),
            )

        assertEquals(listOf(1, 2, 3), rows.map { it.number })
        assertEquals(listOf(0, 1, 2), rows.map { it.sortIndex })
        assertEquals(listOf(7L, 7L, 7L), rows.map { it.playlistId })
        assertEquals(listOf("A", "B", "C"), rows.map { it.source.name })
    }

    @Test
    fun `maps all m3u fields onto the row`() {
        val parsed =
            M3uChannel(
                title = "News One",
                streamUrl = "http://s/1.ts",
                tvgId = "news-1",
                tvgLogo = "http://l/1.png",
                groupTitle = "News",
            )

        val row = ChannelImporter.import(1, listOf(parsed), emptyList()).single()

        assertEquals(
            ChannelSource(
                name = "News One",
                groupTitle = "News",
                logoUrl = "http://l/1.png",
                streamUrl = "http://s/1.ts",
                tvgId = "news-1",
            ),
            row.source,
        )
        assertEquals(ChannelFlags(), row.flags)
    }

    @Test
    fun `favorite and hidden flags survive a refresh matched by tvg id`() {
        val previous =
            ChannelEntity(
                id = 9,
                playlistId = 1,
                number = 5,
                sortIndex = 4,
                source = ChannelSource(name = "Old Name", streamUrl = "http://old/x.ts", tvgId = "keep-1"),
                flags = ChannelFlags(favorite = true, hidden = true),
            )

        val rows =
            ChannelImporter.import(
                playlistId = 1,
                parsed = listOf(channel("New Name", streamUrl = "http://new/y.ts", tvgId = "keep-1")),
                previous = listOf(previous),
            )

        assertEquals(ChannelFlags(favorite = true, hidden = true), rows.single().flags)
        assertEquals(1, rows.single().number)
    }

    @Test
    fun `the blocked flag survives a refresh like favorite and hidden`() {
        val previous =
            ChannelEntity(
                id = 9,
                playlistId = 1,
                number = 5,
                sortIndex = 4,
                source = ChannelSource(name = "Old Name", streamUrl = "http://old/x.ts", tvgId = "keep-1"),
                flags = ChannelFlags(blocked = true),
            )

        val rows =
            ChannelImporter.import(
                playlistId = 1,
                parsed = listOf(channel("New Name", streamUrl = "http://new/y.ts", tvgId = "keep-1")),
                previous = listOf(previous),
            )

        assertEquals(ChannelFlags(blocked = true), rows.single().flags)
    }

    @Test
    fun `the channel-options overrides survive a refresh like the flags`() {
        val overrides =
            ChannelOverrides(
                customName = "News Uno",
                audioDecoder = "Software",
                videoDecoder = "Hardware",
                epgOffsetMinutes = 60,
                externalPlayer = "On",
            )
        val previous =
            ChannelEntity(
                id = 9,
                playlistId = 1,
                number = 5,
                sortIndex = 4,
                source = ChannelSource(name = "Old Name", streamUrl = "http://old/x.ts", tvgId = "keep-1"),
                overrides = overrides,
            )

        val rows =
            ChannelImporter.import(
                playlistId = 1,
                parsed = listOf(channel("New Name", streamUrl = "http://new/y.ts", tvgId = "keep-1")),
                previous = listOf(previous),
            )

        assertEquals(overrides, rows.single().overrides)
        // An unmatched channel starts with no overrides at all.
        val fresh =
            ChannelImporter.import(
                playlistId = 1,
                parsed = listOf(channel("Other", streamUrl = "http://new/z.ts", tvgId = "other-1")),
                previous = listOf(previous),
            )
        assertEquals(ChannelOverrides(), fresh.single().overrides)
    }

    @Test
    fun `flags fall back to stream url plus name when tvg id is missing`() {
        val previous =
            ChannelEntity(
                playlistId = 1,
                number = 1,
                sortIndex = 0,
                source = ChannelSource(name = "A", streamUrl = "http://s/A.ts", tvgId = null),
                flags = ChannelFlags(favorite = true),
            )

        val kept = ChannelImporter.import(1, listOf(channel("A")), listOf(previous)).single()
        val renamed = ChannelImporter.import(1, listOf(channel("B")), listOf(previous)).single()

        assertTrue(kept.flags.favorite)
        assertFalse(renamed.flags.favorite)
    }

    @Test
    fun `a blank tvg id does not match another blank tvg id channel`() {
        assertEquals("http://s/1.ts|One", ChannelImporter.identityOf("", "http://s/1.ts", "One"))
        assertEquals("id-1", ChannelImporter.identityOf("id-1", "http://s/1.ts", "One"))
    }
}
