package com.johncorser.telly.features.search

import androidx.compose.ui.focus.FocusRequester
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** DOWN-from-the-query-bar focus memory (round7 §C4 P2). */
class SearchFocusMemoryTest {
    private val memory = SearchFocusMemory()
    private val card = SearchFocusMemory.Node.ChannelCard(7)
    private val airing = SearchFocusMemory.Node.AiringRow(2, 1_000L)

    @Test
    fun `remembers only the last visited node`() {
        memory.onFocused(card)
        memory.onFocused(airing)

        assertEquals(airing, memory.node.value)
    }

    @Test
    fun `clear forgets the visited node`() {
        memory.onFocused(card)

        memory.clear()

        assertNull(memory.node.value)
    }

    @Test
    fun `down targets try the remembered node before the fresh landing`() {
        val restore = FocusRequester()
        val first = FocusRequester()

        assertEquals(listOf(restore, first), SearchFocusMemory.targets(card, restore, first))
    }

    @Test
    fun `unvisited memory and a missing landing each drop out of the targets`() {
        val restore = FocusRequester()
        val first = FocusRequester()

        assertEquals(listOf(first), SearchFocusMemory.targets(null, restore, first))
        assertEquals(listOf(restore), SearchFocusMemory.targets(card, restore, null))
        assertTrue(SearchFocusMemory.targets(null, restore, null).isEmpty())
    }
}
