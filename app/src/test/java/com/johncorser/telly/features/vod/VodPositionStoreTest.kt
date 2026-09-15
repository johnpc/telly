package com.johncorser.telly.features.vod

import com.johncorser.telly.features.vod.db.VodPositionEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VodPositionStoreTest {
    private val dao = FakeVodPositionDao()
    private var remember = true
    private val store = VodPositionStore(dao, { remember }, { 42_000L })

    @Test
    fun `save upserts the position stamped by the injected clock`() =
        runTest {
            store.save("key", positionMs = 12_000, durationMs = 60_000)

            assertEquals(
                VodPositionEntity("key", positionMs = 12_000, durationMs = 60_000, updatedAtMs = 42_000L),
                dao.rows.value.single(),
            )
        }

    @Test
    fun `a finished watch clears the stored position`() =
        runTest {
            store.save("key", positionMs = 12_000, durationMs = 60_000)

            store.save("key", positionMs = 59_000, durationMs = 60_000)

            assertTrue(dao.rows.value.isEmpty())
        }

    @Test
    fun `unknown durations are never persisted`() =
        runTest {
            store.save("key", positionMs = 12_000, durationMs = 0)
            assertTrue(dao.rows.value.isEmpty())
        }

    @Test
    fun `nothing is read or written while remember is off`() =
        runTest {
            store.save("key", positionMs = 12_000, durationMs = 60_000)
            remember = false

            store.save("key", positionMs = 20_000, durationMs = 60_000)
            assertEquals(12_000L, dao.rows.value.single().positionMs)
            assertNull(store.read("key"))

            remember = true
            assertEquals(12_000L, store.read("key")?.positionMs)
        }
}
