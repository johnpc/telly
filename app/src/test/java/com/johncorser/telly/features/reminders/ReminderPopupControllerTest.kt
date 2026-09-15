package com.johncorser.telly.features.reminders

import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.testutil.FakeKeyValueStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderPopupControllerTest {
    private val store = FakeKeyValueStore()
    private val zone = TimeZone.getTimeZone("UTC")

    private fun TestScope.controller() = ReminderPopupController(store, this, zone)

    // 1970-01-01 15:45 UTC.
    private val startMs = (15 * 60 + 45) * 60_000L

    @Test
    fun `show renders title, channel and a starts-at stamp`() =
        runTest {
            val controller = controller()

            controller.show(testReminder(1, channelId = 9, title = "Morning Report", startMs = startMs), "News One")

            val popup = controller.popup.value!!
            assertEquals(9L, popup.channelId)
            assertEquals("News One", popup.channelName)
            assertEquals("Morning Report", popup.programmeTitle)
            assertEquals("Starts at 03:45 PM", popup.startsAtLabel)
            controller.dismiss()
        }

    @Test
    fun `watch persists the channel as last-channel, bumps the epoch and closes`() =
        runTest {
            val controller = controller()
            controller.show(testReminder(1, channelId = 9, startMs = startMs), "News One")

            controller.watch(controller.popup.value!!)

            assertEquals(9L, store.values[TuneController.LAST_CHANNEL_KEY])
            assertEquals(1, controller.tuneEpoch.value)
            assertNull(controller.popup.value)
        }

    @Test
    fun `dismiss closes without touching the store`() =
        runTest {
            val controller = controller()
            controller.show(testReminder(1, startMs = startMs), "News One")

            controller.dismiss()

            assertNull(controller.popup.value)
            assertEquals(0, controller.tuneEpoch.value)
        }

    @Test
    fun `the popup auto-dismisses after thirty seconds`() =
        runTest {
            val controller = controller()
            controller.show(testReminder(1, startMs = startMs), "News One")

            advanceTimeBy(ReminderPopupController.AUTO_DISMISS_MS)
            runCurrent()

            assertNull(controller.popup.value)
        }

    @Test
    fun `a newer popup re-arms the auto-dismiss`() =
        runTest {
            val controller = controller()
            controller.show(testReminder(1, startMs = startMs), "News One")
            advanceTimeBy(ReminderPopupController.AUTO_DISMISS_MS / 2)

            controller.show(testReminder(2, startMs = startMs), "Sports Arena")
            advanceTimeBy(ReminderPopupController.AUTO_DISMISS_MS / 2)
            runCurrent()

            assertEquals("Sports Arena", controller.popup.value?.channelName)
            controller.dismiss()
        }
}
