package com.johncorser.telly.core.design

import org.junit.Assert.assertEquals
import org.junit.Test

class LogoStyleTest {
    @Test
    fun `the default is today's blue tile with 4 dp corners`() {
        assertEquals(TELLY_LOGO_TILE, LogoStyle.DEFAULT.background)
        assertEquals(4f, LogoStyle.DEFAULT.cornerDp, 0f)
        assertEquals(LogoStyle.DEFAULT, LogoStyle.from("Default", roundedCorners = true))
    }

    @Test
    fun `background labels map to their fills`() {
        assertEquals(LogoStyle.TRANSPARENT, LogoStyle.backgroundFor("Transparent"))
        assertEquals(LogoStyle.DARK, LogoStyle.backgroundFor("Dark"))
        assertEquals(LogoStyle.LIGHT, LogoStyle.backgroundFor("Light"))
        assertEquals(TELLY_LOGO_TILE, LogoStyle.backgroundFor("garbage"))
    }

    @Test
    fun `rounded corners off squares the tile`() {
        assertEquals(0f, LogoStyle.from("Dark", roundedCorners = false).cornerDp, 0f)
        assertEquals(LogoStyle.DARK, LogoStyle.from("Dark", roundedCorners = false).background)
    }
}
