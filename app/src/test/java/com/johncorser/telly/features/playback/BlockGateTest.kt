package com.johncorser.telly.features.playback

import com.johncorser.telly.core.settings.InMemoryKeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.testutil.testChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The PIN gate every tune of a blocked channel must pass (any path). */
class BlockGateTest {
    private val settings = SettingsRepository(InMemoryKeyValueStore())
    private val parental = ParentalControls(settings)
    private val session = BlockSession()
    private val gate = BlockGate(parental, session)
    private val blocked = testChannel(1, 1, "News One").let { it.copy(flags = it.flags.copy(blocked = true)) }
    private val open = testChannel(2, 2, "Sports Arena")

    @Test
    fun `an unblocked channel tunes straight through`() {
        parental.setPin("2468")
        assertFalse(gate.intercept(open))
        assertNull(gate.pinPrompt.value)
    }

    @Test
    fun `a blocked channel is intercepted and prompts`() {
        parental.setPin("2468")
        assertTrue(gate.intercept(blocked))
        assertEquals(blocked, gate.pinPrompt.value)
    }

    @Test
    fun `without a configured pin nothing gates`() {
        assertFalse(gate.intercept(blocked))
        assertFalse(BlockGate().intercept(blocked))
    }

    @Test
    fun `a wrong pin keeps prompting and a right one releases the channel`() {
        parental.setPin("2468")
        gate.intercept(blocked)

        assertNull(gate.unlock("1111"))
        assertEquals(blocked, gate.pinPrompt.value)

        assertEquals(blocked, gate.unlock("2468"))
        assertNull(gate.pinPrompt.value)
    }

    @Test
    fun `unlock without a pending prompt is a no-op`() {
        parental.setPin("2468")
        assertNull(gate.unlock("2468"))
    }

    @Test
    fun `dismiss cancels the prompt without tuning`() {
        parental.setPin("2468")
        gate.intercept(blocked)
        gate.dismiss()
        assertNull(gate.pinPrompt.value)
    }

    @Test
    fun `the captured relock default re-prompts after every unlock`() {
        parental.setPin("2468")
        gate.intercept(blocked)
        gate.unlock("2468")

        // The just-released tune passes the gate exactly once...
        assertFalse(gate.intercept(blocked))
        // ...then the default "Always require" re-arms it.
        assertTrue(gate.intercept(blocked))
    }

    @Test
    fun `until-app-restart keeps one unlock for the whole session across gates`() {
        settings.set(TellySettings.PARENTAL_RELOCK, ParentalControls.RELOCK_UNTIL_RESTART)
        parental.setPin("2468")
        gate.intercept(blocked)
        gate.unlock("2468")

        assertFalse(gate.intercept(blocked))
        assertFalse(gate.intercept(blocked))
        // A second gate over the same session (the other screen's tuner).
        assertFalse(BlockGate(parental, session).intercept(blocked))
    }

    @Test
    fun `flipping relock back to always-require re-arms the gate`() {
        settings.set(TellySettings.PARENTAL_RELOCK, ParentalControls.RELOCK_UNTIL_RESTART)
        parental.setPin("2468")
        gate.intercept(blocked)
        gate.unlock("2468")
        gate.intercept(blocked) // consumes the one-shot pass of the release

        settings.set(TellySettings.PARENTAL_RELOCK, "Always require")

        assertTrue(gate.intercept(blocked))
    }
}
