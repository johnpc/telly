package com.johncorser.telly.features.search

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchQueryTest {
    @Test
    fun `normalize trims and collapses whitespace`() {
        assertEquals("news one", SearchQuery.normalize("  news \t one  "))
        assertEquals("", SearchQuery.normalize("   "))
    }

    @Test
    fun `nameLike anchors the query to a word start`() {
        // Paired with the DAO's `' ' || column`, "% news%" matches word
        // starts only — live 5.2.0 never matches mid-word (round7 probes).
        assertEquals("% news%", SearchQuery.nameLike("news"))
    }

    @Test
    fun `nameLike escapes LIKE wildcards so input never widens the match`() {
        assertEquals("% 100\\%%", SearchQuery.nameLike("100%"))
        assertEquals("% a\\_b%", SearchQuery.nameLike("a_b"))
        assertEquals("% c\\\\d%", SearchQuery.nameLike("c\\d"))
    }

    @Test
    fun `numberLike is a prefix pattern for digits-only queries`() {
        assertEquals("2%", SearchQuery.numberLike("2"))
        assertEquals("24%", SearchQuery.numberLike("24"))
    }

    @Test
    fun `numberLike matches nothing for non-numeric or empty queries`() {
        assertEquals("", SearchQuery.numberLike("news"))
        assertEquals("", SearchQuery.numberLike("2a"))
        assertEquals("", SearchQuery.numberLike(""))
    }
}
