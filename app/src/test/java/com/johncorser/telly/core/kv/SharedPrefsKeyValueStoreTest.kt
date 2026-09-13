package com.johncorser.telly.core.kv

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SharedPrefsKeyValueStoreTest {
    private val store =
        SharedPrefsKeyValueStore(
            ApplicationProvider
                .getApplicationContext<Context>()
                .getSharedPreferences("test", Context.MODE_PRIVATE),
        )

    @Test
    fun `missing keys read as null`() {
        assertNull(store.getLong("lastChannelId"))
    }

    @Test
    fun `longs round-trip and overwrite`() {
        store.putLong("lastChannelId", 7)
        assertEquals(7L, store.getLong("lastChannelId"))

        store.putLong("lastChannelId", 0)
        assertEquals(0L, store.getLong("lastChannelId"))
    }
}
