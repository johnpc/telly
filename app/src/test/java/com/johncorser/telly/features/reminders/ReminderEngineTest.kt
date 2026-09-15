package com.johncorser.telly.features.reminders

import com.johncorser.telly.features.reminders.db.ReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderEngineTest {
    private val dao = FakeReminderDao()
    private val store = ReminderStore(dao)
    private val ticks = MutableSharedFlow<Unit>()
    private val due = mutableListOf<ReminderEntity>()
    private var nowMs = 0L
    private var leadMinutes = 5

    private val minute = ReminderDue.MINUTE_MS

    private fun TestScope.start() =
        ReminderEngine(
            store = store,
            clock = { nowMs },
            leadMinutes = { leadMinutes },
            onDue = { due += it },
        ).startIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)), ticks)

    @Test
    fun `fires once the clock reaches start minus lead and deletes the row`() =
        runTest {
            dao.upsert(testReminder(1, startMs = 10 * minute))
            nowMs = 4 * minute
            start()
            assertTrue(due.isEmpty())

            nowMs = 5 * minute
            ticks.emit(Unit)

            assertEquals(listOf(1L), due.map { it.id })
            assertTrue(store.reminders.first().isEmpty())
        }

    @Test
    fun `fires exactly once across later ticks`() =
        runTest {
            dao.upsert(testReminder(1, startMs = 10 * minute))
            nowMs = 6 * minute
            start()

            ticks.emit(Unit)
            ticks.emit(Unit)

            assertEquals(1, due.size)
        }

    @Test
    fun `honors the configured lead time`() =
        runTest {
            leadMinutes = 30
            dao.upsert(testReminder(1, startMs = 60 * minute))
            nowMs = 29 * minute
            start()
            assertTrue(due.isEmpty())

            nowMs = 30 * minute
            ticks.emit(Unit)

            assertEquals(1, due.size)
        }

    @Test
    fun `a reminder for an ended programme is dropped without firing`() =
        runTest {
            dao.upsert(testReminder(1, startMs = 10 * minute, stopMs = 20 * minute))
            nowMs = 20 * minute
            start()

            assertTrue(due.isEmpty())
            assertTrue(store.reminders.first().isEmpty())
        }

    @Test
    fun `a reminder created inside the lead window fires on the list change`() =
        runTest {
            nowMs = 8 * minute
            start()

            dao.upsert(testReminder(1, startMs = 10 * minute))

            assertEquals(1, due.size)
        }

    @Test
    fun `pending reminders stay stored until their window opens`() =
        runTest {
            dao.upsert(testReminder(1, startMs = 10 * minute))
            nowMs = 0
            start()
            ticks.emit(Unit)

            assertTrue(due.isEmpty())
            assertEquals(1, store.reminders.first().size)
        }
}
