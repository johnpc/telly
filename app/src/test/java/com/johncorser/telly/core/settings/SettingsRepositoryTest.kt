package com.johncorser.telly.core.settings

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {
    private val store = InMemoryKeyValueStore()
    private val repository = SettingsRepository(store)

    @Test
    fun `unset settings return their captured defaults`() {
        assertEquals(0, repository.get(TellySettings.EPG_UPDATE_INTERVAL_HOURS))
        assertEquals(7, repository.get(TellySettings.EPG_PAST_DAYS_TO_KEEP))
        assertTrue(repository.get(TellySettings.EPG_STORE_DESCRIPTIONS))
        assertTrue(repository.get(TellySettings.SEND_STATISTICS))
        assertFalse(repository.get(TellySettings.PARENTAL_ENABLED))
        assertEquals("Small", repository.get(TellySettings.BUFFER_SIZE))
        assertEquals("Blue", repository.get(TellySettings.ACCENT_COLOR))
        assertEquals("Always require", repository.get(TellySettings.PARENTAL_RELOCK))
        assertEquals("By name", repository.get(TellySettings.PLAYLISTS_SORTING))
        assertEquals(emptySet<String>(), repository.get(TellySettings.PARENTAL_LOCKED_GROUPS))
    }

    @Test
    fun `set persists and get round-trips every codec`() {
        repository.set(TellySettings.EPG_UPDATE_INTERVAL_HOURS, 6)
        repository.set(TellySettings.CONFIRM_EXIT, true)
        repository.set(TellySettings.USER_AGENT, "telly/1.0")
        repository.set(TellySettings.PARENTAL_LOCKED_GROUPS, setOf("Movies", "Sports"))

        assertEquals(6, repository.get(TellySettings.EPG_UPDATE_INTERVAL_HOURS))
        assertTrue(repository.get(TellySettings.CONFIRM_EXIT))
        assertEquals("telly/1.0", repository.get(TellySettings.USER_AGENT))
        assertEquals(setOf("Movies", "Sports"), repository.get(TellySettings.PARENTAL_LOCKED_GROUPS))
    }

    @Test
    fun `corrupt stored values fall back to the default`() {
        store.write(TellySettings.EPG_PAST_DAYS_TO_KEEP.key, "not-a-number")
        store.write(TellySettings.CONFIRM_EXIT.key, "not-a-bool")

        assertEquals(7, repository.get(TellySettings.EPG_PAST_DAYS_TO_KEEP))
        assertFalse(repository.get(TellySettings.CONFIRM_EXIT))
    }

    @Test
    fun `flow emits the default then every distinct change`() =
        runTest {
            repository.flow(TellySettings.EPG_PAST_DAYS_TO_KEEP).test {
                assertEquals(7, awaitItem())
                repository.set(TellySettings.EPG_PAST_DAYS_TO_KEEP, 3)
                assertEquals(3, awaitItem())
                repository.set(TellySettings.CONFIRM_EXIT, true)
                repository.set(TellySettings.EPG_PAST_DAYS_TO_KEEP, 3)
                expectNoEvents()
            }
        }

    @Test
    fun `writeRaw stores picker selections by key`() {
        repository.writeRaw(TellySettings.BUFFER_SIZE.key, "Large")
        assertEquals("Large", repository.get(TellySettings.BUFFER_SIZE))
    }

    @Test
    fun `restore replaces the snapshot and drops stale keys`() {
        repository.set(TellySettings.CONFIRM_EXIT, true)
        repository.set(TellySettings.USER_AGENT, "old")

        repository.restore(mapOf(TellySettings.UDP_PROXY.key to "1.2.3.4:1234"))

        assertNull(store.read(TellySettings.CONFIRM_EXIT.key))
        assertNull(store.read(TellySettings.USER_AGENT.key))
        assertEquals("1.2.3.4:1234", repository.get(TellySettings.UDP_PROXY))
        assertEquals(mapOf(TellySettings.UDP_PROXY.key to "1.2.3.4:1234"), repository.snapshot())
    }

    @Test
    fun `string set codec survives empty and single values`() {
        repository.set(TellySettings.PARENTAL_LOCKED_GROUPS, emptySet())
        assertEquals(emptySet<String>(), repository.get(TellySettings.PARENTAL_LOCKED_GROUPS))
        repository.set(TellySettings.PARENTAL_LOCKED_GROUPS, setOf("Kids"))
        assertEquals(setOf("Kids"), repository.get(TellySettings.PARENTAL_LOCKED_GROUPS))
    }

    @Test
    fun `in-memory store removes keys on null writes`() {
        store.write("k", "v")
        assertEquals("v", store.read("k"))
        store.write("k", null)
        assertNull(store.read("k"))
        assertEquals(emptyMap<String, String>(), store.readAll())
    }
}
