package com.johncorser.telly.features.guide

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExitConfirmTest {
    private var now = 0L

    @Test
    fun `the first BACK warns instead of exiting and arms the window`() =
        runTest {
            val exit = ExitConfirm({ now }, this)

            assertFalse(exit.onBack())
            assertTrue(exit.warning.value)
        }

    @Test
    fun `a second BACK inside the window exits`() =
        runTest {
            val exit = ExitConfirm({ now }, this)
            exit.onBack()
            now += ExitConfirm.WINDOW_MS - 1

            assertTrue(exit.onBack())
        }

    @Test
    fun `a second BACK after the window warns again`() =
        runTest {
            val exit = ExitConfirm({ now }, this)
            exit.onBack()
            now += ExitConfirm.WINDOW_MS + 1

            assertFalse(exit.onBack())
            assertTrue(exit.warning.value)
        }

    @Test
    fun `the warning hides itself after the window`() =
        runTest {
            val exit = ExitConfirm({ now }, this)
            exit.onBack()
            runCurrent()

            advanceTimeBy(ExitConfirm.WINDOW_MS + 1)
            runCurrent()

            assertFalse(exit.warning.value)
        }
}
