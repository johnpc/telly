package com.johncorser.telly.features.pip

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PipHomePolicyTest {
    @Test
    fun `home enters pip only with the toggle on and fullscreen playback up`() {
        assertTrue(PipHomePolicy.shouldEnterOnHome(pipOnHome = true, playbackFullscreen = true, inPip = false))
    }

    @Test
    fun `the disabled setting never enters pip`() {
        assertFalse(PipHomePolicy.shouldEnterOnHome(pipOnHome = false, playbackFullscreen = true, inPip = false))
    }

    @Test
    fun `guide or settings or multiview on top never enters pip`() {
        assertFalse(PipHomePolicy.shouldEnterOnHome(pipOnHome = true, playbackFullscreen = false, inPip = false))
    }

    @Test
    fun `an activity already in pip never re-enters`() {
        assertFalse(PipHomePolicy.shouldEnterOnHome(pipOnHome = true, playbackFullscreen = true, inPip = true))
    }
}
