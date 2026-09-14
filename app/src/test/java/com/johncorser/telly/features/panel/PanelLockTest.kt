package com.johncorser.telly.features.panel

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PanelLockTest {
    private val parental = ParentalControls(SettingsRepository(InMemoryKeyValueStore()), Random(seed = 7))
    private val lock = PanelLock(parental)

    private fun enableWithPin(pin: String) {
        parental.setEnabled(true)
        parental.setPin(pin)
    }

    @Test
    fun `unlocked groups pass straight through`() {
        enableWithPin("2468")

        assertFalse(lock.intercept("News"))
        assertNull(lock.pinPrompt.value)
    }

    @Test
    fun `locked groups open the pin prompt instead of switching`() {
        enableWithPin("2468")
        parental.setGroupLocked("Movies", true)

        assertTrue(lock.intercept("Movies"))
        assertEquals("Movies", lock.pinPrompt.value)
    }

    @Test
    fun `the right pin releases the pending group and closes the prompt`() {
        enableWithPin("2468")
        parental.setGroupLocked("Movies", true)
        lock.intercept("Movies")

        assertEquals("Movies", lock.unlock("2468"))
        assertNull(lock.pinPrompt.value)
    }

    @Test
    fun `a wrong pin keeps prompting`() {
        enableWithPin("2468")
        parental.setGroupLocked("Movies", true)
        lock.intercept("Movies")

        assertNull(lock.unlock("1111"))
        assertEquals("Movies", lock.pinPrompt.value)
    }

    @Test
    fun `unlock without a pending prompt is a no-op`() {
        enableWithPin("2468")

        assertNull(lock.unlock("2468"))
    }

    @Test
    fun `dismiss clears the pending prompt`() {
        enableWithPin("2468")
        parental.setGroupLocked("Movies", true)
        lock.intercept("Movies")

        lock.dismiss()

        assertNull(lock.pinPrompt.value)
    }

    @Test
    fun `locks are inert while the master toggle is off`() {
        parental.setPin("2468")
        parental.setGroupLocked("Movies", true)

        assertFalse(lock.intercept("Movies"))
    }

    @Test
    fun `without parental controls nothing is ever locked`() {
        val bare = PanelLock()

        assertFalse(bare.intercept("Movies"))
        assertNull(bare.unlock("2468"))
    }
}
