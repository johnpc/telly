package com.johncorser.telly.features.player.afr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AfrPreferenceTest {
    @Test
    fun `raw values follow the documented picker shape`() {
        assertEquals(
            listOf("Off", "On", "On (also switch refresh rate on stop)"),
            AfrPreference.options,
        )
    }

    @Test
    fun `fromRaw maps each option and falls back to Off`() {
        AfrPreference.entries.forEach { assertEquals(it, AfrPreference.fromRaw(it.raw)) }
        assertEquals(AfrPreference.OFF, AfrPreference.fromRaw("garbage"))
    }

    @Test
    fun `only the on variants enable and only the third restores`() {
        assertFalse(AfrPreference.OFF.enabled)
        assertTrue(AfrPreference.ON.enabled)
        assertTrue(AfrPreference.ON_RESTORE.enabled)
        assertFalse(AfrPreference.OFF.restoreOnStop)
        assertFalse(AfrPreference.ON.restoreOnStop)
        assertTrue(AfrPreference.ON_RESTORE.restoreOnStop)
    }
}
