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

    @Test
    fun `don't require for channels only exempts watching from the PIN`() {
        parental.setEnabled(true)
        parental.setGroupLocked("Movies", locked = true)
        assertTrue(parental.isGroupLocked("Movies"))

        settings.set(TellySettings.PARENTAL_CHANNELS_ONLY, true)

        assertFalse("watching is exempt", parental.isGroupLocked("Movies"))
    }

    @Test
    fun `channels-only leaves the settings gates untouched`() {
        parental.setEnabled(true)
        settings.set(TellySettings.PARENTAL_CHANNELS_ONLY, true)
        settings.set(TellySettings.PARENTAL_REQUIRE_FOR_SETTINGS, true)
        settings.set(TellySettings.PARENTAL_REQUIRE_FOR_PLAYLISTS, true)

        assertTrue(parental.isSettingsLocked())
        assertTrue(parental.isPlaylistsLocked())
    }

    @Test
    fun `playlists gate follows its Require PIN for toggle and the master`() {
        settings.set(TellySettings.PARENTAL_REQUIRE_FOR_PLAYLISTS, true)
        assertFalse(parental.isPlaylistsLocked())
        parental.setEnabled(true)
        assertTrue(parental.isPlaylistsLocked())
        settings.set(TellySettings.PARENTAL_REQUIRE_FOR_PLAYLISTS, false)
        assertFalse(parental.isPlaylistsLocked())
    }

    @Test
    fun `the PIN input method picks the keyboard entry`() {
        assertFalse("captured default is the Picker wheels", parental.usesKeyboardPin)
        settings.set(TellySettings.PARENTAL_PIN_INPUT_METHOD, "Keyboard")
        assertTrue(parental.usesKeyboardPin)
        settings.set(TellySettings.PARENTAL_PIN_INPUT_METHOD, "Picker")
        assertFalse(parental.usesKeyboardPin)
    }
}
