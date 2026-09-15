package com.johncorser.telly.features.pip

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PipStateTest {
    @Test
    fun `starts outside pip and allows the background stop`() {
        val state = PipState()

        assertFalse(state.inPip.value)
        assertTrue(state.allowsBackgroundStop())
    }

    @Test
    fun `entering pip vetoes the background stop until the mode flips back`() {
        val state = PipState()

        state.setInPip(true)
        assertTrue(state.inPip.value)
        assertFalse(state.allowsBackgroundStop())

        // Closing the PIP window flips the mode off before its stop lands.
        state.setInPip(false)
        assertTrue(state.allowsBackgroundStop())
    }

    @Test
    fun `the shared production instance is a stable singleton`() {
        assertSame(PipState.shared, PipState.shared)
        assertFalse(PipState.shared.inPip.value)
    }
}
