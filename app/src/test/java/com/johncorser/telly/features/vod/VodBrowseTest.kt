package com.johncorser.telly.features.vod

import com.johncorser.telly.features.vod.db.VodItemEntity
import com.johncorser.telly.features.vod.db.VodPositionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VodBrowseTest {
    private fun item(
        name: String,
        group: String?,
        sortIndex: Int = 0,
    ) = VodItemEntity(
        playlistId = 1,
        sortIndex = sortIndex,
        itemKey = "http://s/$name.mp4|$name",
        name = name,
        groupTitle = group,
        streamUrl = "http://s/$name.mp4",
    )

    @Test
    fun `categories are distinct group-titles in playlist order`() {
        val items =
            listOf(
                item("A", "Cinema", 0),
                item("B", "Docs", 1),
                item("C", "Cinema", 2),
                item("D", "", 3),
                item("E", null, 4),
            )

        assertEquals(listOf("Cinema", "Docs", VodBrowse.UNCATEGORIZED), VodBrowse.categories(items))
    }

    @Test
    fun `cards filter to the selected category and join stored progress`() {
        val items = listOf(item("A", "Cinema"), item("B", "Docs"), item("C", "Cinema"))
        val positions =
            listOf(VodPositionEntity(items[0].itemKey, positionMs = 15_000, durationMs = 60_000, updatedAtMs = 1))

        val cards = VodBrowse.cards(items, "Cinema", positions)

        assertEquals(listOf("A", "C"), cards.map { it.item.name })
        assertEquals(250, cards.first().progressPermille)
        assertNull(cards.last().progressPermille)
    }

    @Test
    fun `a null category keeps every item`() {
        val items = listOf(item("A", "Cinema"), item("B", null))
        assertEquals(2, VodBrowse.cards(items, null, emptyList()).size)
    }

    @Test
    fun `unknown durations never produce a progress bar`() {
        val items = listOf(item("A", "Cinema"))
        val positions =
            listOf(VodPositionEntity(items[0].itemKey, positionMs = 15_000, durationMs = 0, updatedAtMs = 1))

        assertNull(VodBrowse.cards(items, "Cinema", positions).single().progressPermille)
    }

    @Test
    fun `progress clamps to the permille range`() {
        val items = listOf(item("A", "Cinema"))
        val positions =
            listOf(VodPositionEntity(items[0].itemKey, positionMs = 90_000, durationMs = 60_000, updatedAtMs = 1))

        assertEquals(1000, VodBrowse.cards(items, "Cinema", positions).single().progressPermille)
    }
}
