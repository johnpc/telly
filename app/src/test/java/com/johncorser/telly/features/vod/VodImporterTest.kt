package com.johncorser.telly.features.vod

import com.johncorser.telly.features.playlist.M3uChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VodImporterTest {
    @Test
    fun `imports preserve group-title, tvg-logo and playlist order`() {
        val rows =
            VodImporter.import(
                playlistId = 7,
                parsed =
                    listOf(
                        M3uChannel(
                            title = "Big Buck Bunny",
                            streamUrl = "http://s/bbb.mp4",
                            tvgLogo = "http://l/bbb.png",
                            groupTitle = "Cinema",
                        ),
                        M3uChannel(title = "Sintel", streamUrl = "http://s/sintel.mp4"),
                    ),
            )

        assertEquals(listOf(0, 1), rows.map { it.sortIndex })
        assertEquals(listOf(7L, 7L), rows.map { it.playlistId })
        val first = rows.first()
        assertEquals("Big Buck Bunny", first.name)
        assertEquals("Cinema", first.groupTitle)
        assertEquals("http://l/bbb.png", first.logoUrl)
        assertEquals("http://s/bbb.mp4", first.streamUrl)
        assertEquals("http://s/bbb.mp4|Big Buck Bunny", first.itemKey)
        assertNull(rows.last().groupTitle)
    }
}
