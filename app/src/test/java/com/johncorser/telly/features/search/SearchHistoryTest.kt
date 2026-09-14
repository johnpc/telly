package com.johncorser.telly.features.search

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchHistoryTest {
    private val store = InMemoryKeyValueStore()
    private val history = SearchHistory(store)

    @Test
    fun `starts empty`() {
        assertTrue(history.list().isEmpty())
    }

    @Test
    fun `records committed queries most recent first`() {
        history.record("news")
        history.record("sports")

        assertEquals(listOf("sports", "news"), history.list())
    }

    @Test
    fun `normalizes and ignores blank queries`() {
        history.record("  news   one ")
        history.record("   ")

        assertEquals(listOf("news one"), history.list())
    }

    @Test
    fun `re-searching moves the entry back to the front without duplicating`() {
        history.record("news")
        history.record("sports")
        history.record("NEWS")

        assertEquals(listOf("NEWS", "sports"), history.list())
    }

    @Test
    fun `the list is capped at twenty entries`() {
        repeat(25) { history.record("query $it") }

        assertEquals(SearchHistory.MAX_ENTRIES, history.list().size)
        assertEquals("query 24", history.list().first())
    }

    @Test
    fun `clear empties the list and the store`() {
        history.record("news")

        history.clear()

        assertTrue(history.list().isEmpty())
        assertEquals(null, store.read(SearchHistory.KEY))
    }

    @Test
    fun `entries survive a new instance over the same store`() {
        history.record("news")

        assertEquals(listOf("news"), SearchHistory(store).list())
    }
}
