package com.johncorser.telly.features.recording

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecordingNotificationsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `text shows channel and elapsed, plus a more count for parallel captures`() {
        val session = RecordingSession("News One", startedAtMs = 1_000L)

        assertEquals("News One — 00:59", RecordingNotifications.text(session, count = 1, nowMs = 60_000L))
        assertEquals("News One and 1 more — 00:59", RecordingNotifications.text(session, count = 2, nowMs = 60_000L))
    }

    @Test
    fun `the start intent round-trips the session and count`() {
        val intent = RecordingNotifications.intent(context, RecordingSession("Sports", 5_000L), count = 3)

        assertEquals(RecordingSession("Sports", 5_000L), RecordingNotifications.sessionOf(intent))
        assertEquals(3, RecordingNotifications.countOf(intent))
    }

    @Test
    fun `a null intent decodes to an empty session and a single count`() {
        assertEquals(RecordingSession("", 0L), RecordingNotifications.sessionOf(null))
        assertEquals(1, RecordingNotifications.countOf(null))
    }

    @Test
    fun `building the notification renders the elapsed text`() {
        val notification =
            RecordingNotifications.build(context, RecordingSession("News One", 0L), count = 1, nowMs = 90_000L)

        assertEquals(
            "News One — 01:30",
            notification.extras.getString("android.text"),
        )
    }
}
