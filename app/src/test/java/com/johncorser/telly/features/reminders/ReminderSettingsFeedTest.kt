package com.johncorser.telly.features.reminders

import com.johncorser.telly.testutil.testChannel
import com.johncorser.telly.testutil.testProgram
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class ReminderSettingsFeedTest {
    private val dao = FakeReminderDao()
    private val store = ReminderStore(dao)
    private val channels = MutableStateFlow(listOf(testChannel(7, 1, "News One")))
    private val feed = ReminderSettingsFeed(store, channels, TimeZone.getTimeZone("UTC"))

    // 1970-01-01 15:45 UTC.
    private val startMs = (15 * 60 + 45) * 60_000L

    @Test
    fun `items join the channel name and render the air time`() =
        runTest {
            store.toggle(7L, testProgram("tvg-7", startMs, startMs + 60_000L, "Morning Report"))

            val item = feed.items.first().single()
            assertEquals("Morning Report", item.title)
            assertEquals("News One", item.channelName)
            assertEquals("Thu, Jan 1, 3:45 PM", item.airTime)
        }

    @Test
    fun `a reminder on an unknown channel keeps an empty name`() =
        runTest {
            store.toggle(99L, testProgram("tvg-99", startMs, startMs + 60_000L, "Ghost Show"))

            assertEquals("", feed.items.first().single().channelName)
        }

    @Test
    fun `delete removes the reminder from the feed`() =
        runTest {
            store.toggle(7L, testProgram("tvg-7", startMs, startMs + 60_000L, "Morning Report"))

            feed.delete(feed.items.first().single().id)

            assertTrue(feed.items.first().isEmpty())
        }
}
