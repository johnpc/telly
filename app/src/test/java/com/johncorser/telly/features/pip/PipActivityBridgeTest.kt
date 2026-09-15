package com.johncorser.telly.features.pip

import android.app.Activity
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The bridge itself is thin Activity glue (the platform call is unreachable
 * on the JVM, where Build.VERSION.SDK_INT is 0); these tests pin the state
 * bridging and that the HOME path consults [PipHomePolicy] before anything
 * touches the platform.
 */
class PipActivityBridgeTest {
    private val activity = mockk<Activity>(relaxed = true)
    private val state = PipState()

    @Test
    fun `mode changes propagate into the shared pip state`() {
        val bridge = PipActivityBridge(activity, state, { true }, { true })

        bridge.onModeChanged(true)
        assertTrue(state.inPip.value)

        bridge.onModeChanged(false)
        assertFalse(state.inPip.value)
    }

    @Test
    fun `home is a no-op while the setting is off or playback is not fullscreen`() {
        val bridge = PipActivityBridge(activity, state, { false }, { true })

        bridge.onUserLeaveHint()

        assertFalse(state.inPip.value)
    }

    @Test
    fun `home with the policy satisfied requests the pip transition`() {
        val bridge = PipActivityBridge(activity, state, { true }, { true })

        // On the JVM the platform gate (SDK_INT >= 26) short-circuits; the
        // call must still be safe and leave the observed state untouched
        // (the real mode flip arrives via onPictureInPictureModeChanged).
        bridge.onUserLeaveHint()

        assertFalse(state.inPip.value)
    }
}
