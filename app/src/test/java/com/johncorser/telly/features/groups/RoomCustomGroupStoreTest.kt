package com.johncorser.telly.features.groups

import com.johncorser.telly.core.db.inMemoryDb
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The Room-backed store behind custom groups, over the real DAO. */
@RunWith(RobolectricTestRunner::class)
class RoomCustomGroupStoreTest {
    private val database = inMemoryDb()
    private val store = RoomCustomGroupStore(database.customGroupDao())

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `groups observe in creation order with their members attached`() =
        runTest {
            val picks = store.create("Picks")
            val kids = store.create("Kids OK")
            store.addMembers(picks, listOf("tvg-1", "tvg-2"))
            store.addMembers(kids, listOf("tvg-3"))

            assertEquals(
                listOf("Picks" to setOf("tvg-1", "tvg-2"), "Kids OK" to setOf("tvg-3")),
                store.observe().first().map { it.name to it.members },
            )
        }

    @Test
    fun `adding an already-present member is a no-op`() =
        runTest {
            val picks = store.create("Picks")
            store.addMembers(picks, listOf("tvg-1"))
            store.addMembers(picks, listOf("tvg-1", "tvg-2"))

            assertEquals(setOf("tvg-1", "tvg-2"), store.observe().first().single().members)
        }

    @Test
    fun `rename keeps the members and the column position`() =
        runTest {
            val picks = store.create("Picks")
            store.create("Kids OK")
            store.addMembers(picks, listOf("tvg-1"))

            store.rename(picks, "Legends")

            val renamed = store.observe().first().first()
            assertEquals("Legends", renamed.name)
            assertEquals(setOf("tvg-1"), renamed.members)
        }

    @Test
    fun `delete removes the group and its membership rows`() =
        runTest {
            val picks = store.create("Picks")
            store.addMembers(picks, listOf("tvg-1"))

            store.delete(picks)

            assertTrue(store.observe().first().isEmpty())
            assertTrue(database.customGroupDao().observeMembers().first().isEmpty())
        }
}
