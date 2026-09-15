package com.johncorser.telly.core.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `system and unknown labels mean no locale override`() {
        assertNull(AppLanguage.tagFor("System"))
        assertNull(AppLanguage.tagFor("garbage"))
    }

    @Test
    fun `every picker language maps to its bcp47 tag`() {
        assertEquals("en", AppLanguage.tagFor("English"))
        assertEquals("es", AppLanguage.tagFor("Español"))
        assertEquals("fr", AppLanguage.tagFor("Français"))
        assertEquals("de", AppLanguage.tagFor("Deutsch"))
        assertEquals("pt", AppLanguage.tagFor("Português"))
        assertEquals("it", AppLanguage.tagFor("Italiano"))
    }

    @Test
    fun `the picker options lead with the System default`() {
        assertEquals("System", AppLanguage.OPTIONS.first())
        assertEquals(7, AppLanguage.OPTIONS.size)
    }
}
