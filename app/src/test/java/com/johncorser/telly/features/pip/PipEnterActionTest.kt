package com.johncorser.telly.features.pip

import org.junit.Assert.assertEquals
import org.junit.Test

class PipEnterActionTest {
    @Test
    fun `clears the chrome before handing over to the activity`() {
        val calls = mutableListOf<String>()
        val action = PipEnterAction(clearChrome = { calls += "clear" }, enter = { calls += "enter" })

        action()

        assertEquals(listOf("clear", "enter"), calls)
    }

    @Test
    fun `every invocation re-runs both steps`() {
        var cleared = 0
        var entered = 0
        val action = PipEnterAction(clearChrome = { cleared += 1 }, enter = { entered += 1 })

        action()
        action()

        assertEquals(2, cleared)
        assertEquals(2, entered)
    }
}
