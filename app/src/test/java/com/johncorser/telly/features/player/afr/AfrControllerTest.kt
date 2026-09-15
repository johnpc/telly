package com.johncorser.telly.features.player.afr

import org.junit.Assert.assertEquals
import org.junit.Test

/** AFR switch/restore orchestration over an injected fake display. */
class AfrControllerTest {
    private val uhd60 = AfrMode(1, 3840, 2160, 60f)
    private val uhd24 = AfrMode(2, 3840, 2160, 24f)
    private val uhd50 = AfrMode(3, 3840, 2160, 50f)

    private class FakeDisplay(
        var set: AfrModeSet?,
    ) : AfrDisplay {
        val applied = mutableListOf<Int>()

        override fun modes(): AfrModeSet? = set

        override fun apply(modeId: Int) {
            applied += modeId
            set = set?.let { modes -> modes.copy(current = modes.all.first { it.id == modeId }) }
        }
    }

    private var preference = AfrPreference.OFF
    private val display = FakeDisplay(AfrModeSet(uhd60, listOf(uhd60, uhd24, uhd50)))
    private val controller = AfrController(preference = { preference }, display = display)

    @Test
    fun `off never touches the display`() {
        controller.onFrameRate(24f)
        controller.onPlaybackStopped()
        assertEquals(emptyList<Int>(), display.applied)
    }

    @Test
    fun `on switches to the matching mode and plain stop does not restore`() {
        preference = AfrPreference.ON
        controller.onFrameRate(24f)
        assertEquals(listOf(uhd24.id), display.applied)

        controller.onPlaybackStopped()
        assertEquals(listOf(uhd24.id), display.applied)
    }

    @Test
    fun `the restore variant returns to the original mode on stop`() {
        preference = AfrPreference.ON_RESTORE
        controller.onFrameRate(24f)
        controller.onPlaybackStopped()
        assertEquals(listOf(uhd24.id, uhd60.id), display.applied)
    }

    @Test
    fun `zapping across content keeps the first original for the restore`() {
        preference = AfrPreference.ON_RESTORE
        controller.onFrameRate(24f)
        controller.onFrameRate(25f)
        controller.onPlaybackStopped()
        assertEquals(listOf(uhd24.id, uhd50.id, uhd60.id), display.applied)
    }

    @Test
    fun `stop without a prior switch is a no-op`() {
        preference = AfrPreference.ON_RESTORE
        controller.onFrameRate(60f)
        controller.onPlaybackStopped()
        assertEquals(emptyList<Int>(), display.applied)
    }

    @Test
    fun `a detached display is a no-op`() {
        preference = AfrPreference.ON
        display.set = null
        controller.onFrameRate(24f)
        assertEquals(emptyList<Int>(), display.applied)
    }
}
