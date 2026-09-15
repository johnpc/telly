package com.johncorser.telly.features.reminders

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.db.TellyDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RemindersLocatorTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `the hub is a singleton over the current database`() =
        runTest {
            val hub = ServiceLocator.remindersHub(context)
            assertSame(hub, ServiceLocator.remindersHub(context))
            assertSame(ServiceLocator.database(context), hub.database)
            assertTrue(hub.store.reminders.first().isEmpty())
            assertTrue(hub.guide.keys.value.isEmpty())
            assertTrue(hub.settingsFeed.items.first().isEmpty())
        }

    @Test
    fun `the hub is rebuilt when the database singleton changes`() {
        val before = ServiceLocator.remindersHub(context)

        resetDatabaseSingleton()
        val after = ServiceLocator.remindersHub(context)

        assertNotSame(before, after)
        assertSame(ServiceLocator.database(context), after.database)
    }

    /** Mirrors the e2e harness's per-scenario ServiceLocator wipe. */
    private fun resetDatabaseSingleton() {
        val field = ServiceLocator::class.java.getDeclaredField("database")
        field.isAccessible = true
        (field.get(ServiceLocator) as? TellyDatabase)?.close()
        field.set(ServiceLocator, null)
    }
}
