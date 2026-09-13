package com.johncorser.telly.core.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SharedPrefsKeyValueStoreTest {
    private val prefs =
        ApplicationProvider
            .getApplicationContext<Context>()
            .getSharedPreferences("test-settings", Context.MODE_PRIVATE)
    private val store = SharedPrefsKeyValueStore(prefs)

    @Test
    fun `writes persist to shared preferences and read back`() {
        store.write("a", "1")
        store.write("b", "2")

        assertEquals("1", store.read("a"))
        assertEquals(mapOf("a" to "1", "b" to "2"), store.readAll())
        assertEquals("1", prefs.getString("a", null))
    }

    @Test
    fun `null write removes the key`() {
        store.write("a", "1")
        store.write("a", null)

        assertNull(store.read("a"))
        assertEquals(emptyMap<String, String>(), store.readAll())
    }

    @Test
    fun `snapshots mirror the preferences after every write`() =
        runTest {
            store.write("a", "1")
            assertEquals(mapOf("a" to "1"), store.snapshots.first())
        }

    @Test
    fun `pre-existing preference values are visible immediately`() {
        prefs.edit().putString("seed", "value").commit()
        assertEquals("value", SharedPrefsKeyValueStore(prefs).read("seed"))
    }
}
