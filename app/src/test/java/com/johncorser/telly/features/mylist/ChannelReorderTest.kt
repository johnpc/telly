package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.testutil.asFavorite
import com.johncorser.telly.testutil.testChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure favorites/group ordering math behind the management screens. */
class ChannelReorderTest {
    private fun favorite(
        id: Long,
        order: Int,
    ): ChannelEntity {
        val channel = testChannel(id, id.toInt(), "Channel $id").asFavorite()
        return channel.copy(flags = channel.flags.copy(favoriteOrder = order))
    }

    private val plain = testChannel(9, 9, "Plain Nine")

    @Test
    fun `favorites order by favoriteOrder with ties keeping the zap order`() {
        val untouchedA = testChannel(1, 1, "A").asFavorite()
        val untouchedB = testChannel(2, 2, "B").asFavorite()
        val moved = favorite(3, order = -1)

        assertEquals(
            listOf(3L, 1L, 2L),
            ChannelReorder.favorites(listOf(untouchedA, untouchedB, moved, plain)).map { it.id },
        )
    }

    @Test
    fun `editor rows put the ordered favorites before the rest`() {
        val favorite = favorite(3, order = 0)

        assertEquals(listOf(3L, 9L), ChannelReorder.editorRows(listOf(plain, favorite)).map { it.id })
    }

    @Test
    fun `toggling on appends to the favorites order and toggling off keeps it`() {
        val existing = favorite(1, order = 4)

        val added = ChannelReorder.toggled(listOf(existing, plain), plain)
        assertTrue(added.flags.favorite)
        assertEquals(5, added.flags.favoriteOrder)

        val removed = ChannelReorder.toggled(listOf(existing, plain), existing)
        assertFalse(removed.flags.favorite)
        assertEquals(4, removed.flags.favoriteOrder)
    }

    @Test
    fun `moving a favorite swaps it with its neighbour and reindexes only changes`() {
        val channels = listOf(favorite(1, 0), favorite(2, 1), favorite(3, 2), plain)

        val updates = ChannelReorder.moveFavorite(channels, channelId = 1, delta = +1)

        assertEquals(
            mapOf(2L to 0, 1L to 1),
            updates.associate { it.id to it.flags.favoriteOrder },
        )
    }

    @Test
    fun `moves past either end are no-ops`() {
        val channels = listOf(favorite(1, 0), favorite(2, 1))

        assertEquals(emptyList<ChannelEntity>(), ChannelReorder.moveFavorite(channels, 1, delta = -1))
        assertEquals(emptyList<ChannelEntity>(), ChannelReorder.moveFavorite(channels, 2, delta = +1))
        assertEquals(emptyList<ChannelEntity>(), ChannelReorder.moveFavorite(channels, 9, delta = +1))
    }

    @Test
    fun `group moves swap the two neighbours' sort indices`() {
        val a = testChannel(1, 1, "A")
        val b = testChannel(2, 2, "B")

        val updates = ChannelReorder.moveInGroup(listOf(a, b), channelId = 1, delta = +1)

        assertEquals(mapOf(1L to 1, 2L to 0), updates.associate { it.id to it.sortIndex })
        assertEquals(emptyList<ChannelEntity>(), ChannelReorder.moveInGroup(listOf(a, b), 1, delta = -1))
    }
}
