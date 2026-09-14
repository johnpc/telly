package com.johncorser.telly.features.guide

import com.johncorser.telly.testutil.FakeKeyValueStore
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideHintTest {
    private val store = FakeKeyValueStore()

    @Test
    fun `shows on the first ever guide open and auto-hides`() =
        runTest {
            val visible = GuideHint(store).startIn(backgroundScope)

            assertTrue(visible.value)

            advanceTimeBy(GuideHint.HIDE_AFTER_MS + 1)
            runCurrent()

            assertFalse(visible.value)
        }

    @Test
    fun `never shows again once seen`() =
        runTest {
            GuideHint(store).startIn(backgroundScope)

            val second = GuideHint(store).startIn(backgroundScope)

            assertFalse(second.value)
        }
}
