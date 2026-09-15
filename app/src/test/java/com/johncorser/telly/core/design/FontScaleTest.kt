package com.johncorser.telly.core.design

import org.junit.Assert.assertEquals
import org.junit.Test

class FontScaleTest {
    @Test
    fun `medium is exactly today's 1x scale`() {
        assertEquals(1f, FontScale.factorFor("Medium"), 0f)
        assertEquals(1f, FontScale.factorFor("garbage"), 0f)
    }

    @Test
    fun `the other sizes map to their factors`() {
        assertEquals(0.85f, FontScale.factorFor("Small"), 0f)
        assertEquals(1.15f, FontScale.factorFor("Large"), 0f)
        assertEquals(1.3f, FontScale.factorFor("Huge"), 0f)
    }
}
