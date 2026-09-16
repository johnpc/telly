package com.johncorser.telly

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutostartTest {
    @Test
    fun `boot launches only with the boot toggle on`() {
        assertTrue(Autostart.shouldLaunch(Autostart.BOOT_COMPLETED, onBoot = true, onWake = false))
        assertFalse(Autostart.shouldLaunch(Autostart.BOOT_COMPLETED, onBoot = false, onWake = true))
    }

    @Test
    fun `screen-on launches only with the wake toggle on`() {
        assertTrue(Autostart.shouldLaunch(Autostart.SCREEN_ON, onBoot = false, onWake = true))
        assertFalse(Autostart.shouldLaunch(Autostart.SCREEN_ON, onBoot = true, onWake = false))
    }

    @Test
    fun `unknown or missing actions never launch`() {
        assertFalse(Autostart.shouldLaunch(null, onBoot = true, onWake = true))
        assertFalse(Autostart.shouldLaunch("android.intent.action.USER_PRESENT", onBoot = true, onWake = true))
    }
}
