package com.johncorser.telly.features.recording

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.johncorser.telly.core.ServiceLocator
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class RecordingAndroidGlueTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        // Drop the ServiceLocator singletons the locator built.
        listOf("database", "settings").forEach { name ->
            val field = ServiceLocator::class.java.getDeclaredField(name)
            field.isAccessible = true
            field.set(ServiceLocator, null)
        }
        context.deleteDatabase("telly.db")
    }

    @Test
    fun `the locator builds an app-scoped center and screen deps over it`() {
        val center = ServiceLocator.recordingCenter(context)

        assertNotNull(center)
        assertSame(center, ServiceLocator.recordingCenter(context))
        val deps = ServiceLocator.recordingDeps(context)
        assertSame(center, deps.center)
        assertNotNull(deps.engineFactory)
    }

    @Test
    fun `the service control starts the foreground service while sessions exist and stops it when none do`() {
        val control = ContextRecordingServiceControl(context)
        val app = shadowOf(ApplicationProvider.getApplicationContext<android.app.Application>())

        control.sync(listOf(RecordingSession("News One", 0L)))
        val started = app.nextStartedService
        assertNotNull(started)
        assertTrue(started.component!!.className.endsWith("RecordingService"))

        control.sync(emptyList())
        assertTrue(app.getNextStoppedService() != null)
    }

    @Test
    fun `the settings hook exposes storage and delete-all`() {
        val center = ServiceLocator.recordingCenter(context)
        val hook = RecordingSettingsHook(center)

        assertNotNull(hook.storage())
        assertEquals(RECORDING_SCHEDULING_NOTE, "Scheduled recordings start only while telly is running")
    }

    @Test
    fun `recording rows render storage numbers, and the null hook shows zeroes`() {
        val rows = recordingRows(null)

        assertEquals("0 KB", (rows[0] as com.johncorser.telly.features.settings.SettingsRow.Value).summary)
        assertNull(RecordingStatus.entries.firstOrNull { it.name == "NOPE" })
    }
}
