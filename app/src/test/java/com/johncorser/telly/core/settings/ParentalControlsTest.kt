package com.johncorser.telly.core.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ParentalControlsTest {
    private val settings = SettingsRepository(InMemoryKeyValueStore())
    private val parental = ParentalControls(settings, Random(seed = 42))

    @Test
    fun `disabled by default with no pin`() {
        assertFalse(parental.isEnabled)
        assertFalse(parental.hasPin)
        assertFalse(parental.verifyPin("0000"))
    }

    @Test
    fun `setPin stores salted hash and verifies only the right pin`() {
        parental.setPin("2468")

        assertTrue(parental.hasPin)
        assertTrue(parental.verifyPin("2468"))
        assertFalse(parental.verifyPin("8642"))
        assertFalse(settings.get(TellySettings.PARENTAL_PIN_HASH).contains("2468"))
        assertEquals(64, settings.get(TellySettings.PARENTAL_PIN_HASH).length)
    }

    @Test
    fun `changing the pin re-salts and invalidates the old pin`() {
        parental.setPin("1111")
        val firstHash = settings.get(TellySettings.PARENTAL_PIN_HASH)
        parental.setPin("2222")

        assertFalse(parental.verifyPin("1111"))
        assertTrue(parental.verifyPin("2222"))
        assertFalse(firstHash == settings.get(TellySettings.PARENTAL_PIN_HASH))
    }

    @Test
    fun `a locked group gates only while the master toggle is on`() {
        parental.setGroupLocked("Movies", locked = true)
        assertFalse(parental.isGroupLocked("Movies"))

        parental.setEnabled(true)
        assertTrue(parental.isGroupLocked("Movies"))
        assertFalse(parental.isGroupLocked("News"))

        parental.setGroupLocked("Movies", locked = false)
        assertFalse(parental.isGroupLocked("Movies"))
    }

    @Test
    fun `settings gate follows the Require PIN for toggle`() {
        settings.set(TellySettings.PARENTAL_REQUIRE_FOR_SETTINGS, true)
        assertFalse(parental.isSettingsLocked())
        parental.setEnabled(true)
        assertTrue(parental.isSettingsLocked())
    }
}
