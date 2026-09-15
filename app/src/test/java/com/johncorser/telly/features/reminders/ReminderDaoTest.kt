package com.johncorser.telly.features.reminders

import com.johncorser.telly.core.db.inMemoryDb
import com.johncorser.telly.features.reminders.db.ReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The real reminder queries against an in-memory Room DB. */
@RunWith(RobolectricTestRunner::class)
class ReminderDaoTest {
    private val database = inMemoryDb()
    private val dao = database.reminderDao()

    @After
    fun tearDown() {
        database.close()
    }

    private fun reminder(
        channelId: Long,
        title: String,
        startMs: Long,
    ) = ReminderEntity(channelId = channelId, title = title, startMs = startMs, stopMs = startMs + 1_000L)

    @Test
    fun `reminders come back in air order`() =
        runTest {
            dao.upsert(reminder(2, "Later", 5_000L))
            dao.upsert(reminder(1, "Sooner", 1_000L))

            assertEquals(listOf("Sooner", "Later"), dao.observeUpcoming().first().map { it.title })
        }

    @Test
    fun `re-reminding the same programme replaces its row`() =
        runTest {
            dao.upsert(reminder(1, "Show", 1_000L))
            dao.upsert(reminder(1, "Show", 1_000L))

            assertEquals(1, dao.observeUpcoming().first().size)
        }

    @Test
    fun `find matches only the exact programme and delete removes it`() =
        runTest {
            dao.upsert(reminder(1, "Show", 1_000L))
            assertNull(dao.find(1, 2_000L, "Show"))
            assertNull(dao.find(2, 1_000L, "Show"))

            val found = dao.find(1, 1_000L, "Show")!!
            dao.delete(found.id)

            assertEquals(emptyList<ReminderEntity>(), dao.observeUpcoming().first())
        }
}
