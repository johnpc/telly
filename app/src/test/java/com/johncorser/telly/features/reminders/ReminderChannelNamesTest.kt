package com.johncorser.telly.features.reminders

import com.johncorser.telly.testutil.testChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderChannelNamesTest {
    private val names = ReminderChannelNames(MutableStateFlow(listOf(testChannel(7, 1, "News One"))))

    @Test
    fun `resolves a known channel to its name`() =
        runTest {
            assertEquals("News One", names.nameOf(7L))
        }

    @Test
    fun `an unknown channel resolves to an empty name`() =
        runTest {
            assertEquals("", names.nameOf(99L))
        }
}
