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
    @Test
    fun `the first BACK warns instead of exiting and arms the window`() =
        runTest {
            val exit = ExitConfirm(this)

            assertFalse(exit.onBack())
            assertTrue(exit.warning.value)
        }

    @Test
    fun `a second BACK inside the window exits`() =
        runTest {
            val exit = ExitConfirm(this)
            exit.onBack()
            advanceTimeBy(ExitConfirm.WINDOW_MS - 1)

            assertTrue(exit.onBack())
        }

    @Test
    fun `a second BACK after the window warns again`() =
        runTest {
            val exit = ExitConfirm(this)
            exit.onBack()
            advanceTimeBy(ExitConfirm.WINDOW_MS + 1)
            runCurrent()

            assertFalse(exit.onBack())
            assertTrue(exit.warning.value)
        }

    @Test
    fun `the warning hides itself after the window and the exit disarms with it`() =
        runTest {
            val exit = ExitConfirm(this)
            exit.onBack()
            runCurrent()

            advanceTimeBy(ExitConfirm.WINDOW_MS + 1)
            runCurrent()

            assertFalse(exit.warning.value)
            assertFalse("the toast is gone, so BACK warns again instead of exiting", exit.onBack())
        }
}
