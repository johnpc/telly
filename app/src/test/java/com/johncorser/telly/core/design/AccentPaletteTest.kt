package com.johncorser.telly.core.design

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccentPaletteTest {
    @Test
    fun `default accent is the sampled tivimate blue`() {
        assertEquals("Blue", AccentPalette.DEFAULT.first)
        assertEquals(0xFF2196F3, AccentPalette.DEFAULT.second)
        assertEquals(0xFF2196F3, AccentPalette.argbFor("Blue"))
    }

    @Test
    fun `every named accent resolves to its color`() {
        AccentPalette.OPTIONS.forEach { (name, argb) -> assertEquals(argb, AccentPalette.argbFor(name)) }
        assertEquals(AccentPalette.OPTIONS.map { it.first }, AccentPalette.names())
    }

    @Test
    fun `unknown names fall back to blue`() {
        assertEquals(0xFF2196F3, AccentPalette.argbFor("Chartreuse"))
    }

    @Test
    fun `blue is first so the picker default matches the capture`() {
        assertTrue(AccentPalette.names().first() == "Blue")
    }
}
