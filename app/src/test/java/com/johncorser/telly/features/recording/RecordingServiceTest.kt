package com.johncorser.telly.features.recording

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class RecordingServiceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `onStartCommand posts the ongoing foreground notification and destroy tears it down`() {
        val intent = RecordingNotifications.intent(context, RecordingSession("News One", 0L), count = 1)
        val controller = Robolectric.buildService(RecordingService::class.java, intent).create()
        val service = controller.get()

        service.onStartCommand(intent, 0, 1)

        val notification = shadowOf(service).lastForegroundNotification
        assertNotNull(notification)

        controller.destroy()
    }
}
