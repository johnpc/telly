package com.johncorser.telly.features.reminders

import com.johncorser.telly.features.reminders.db.ReminderDao
import com.johncorser.telly.features.reminders.db.ReminderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [ReminderDao] mirroring the real one's ordering and replace. */
class FakeReminderDao : ReminderDao {
    val rows = MutableStateFlow<List<ReminderEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun upsert(reminder: ReminderEntity): Long {
        val id = if (reminder.id == 0L) nextId++ else reminder.id
        rows.update { list ->
            list.filterNot {
                it.channelId == reminder.channelId && it.startMs == reminder.startMs && it.title == reminder.title
            } + reminder.copy(id = id)
        }
        return id
    }

    override fun observeUpcoming(): Flow<List<ReminderEntity>> =
        rows.map { list -> list.sortedWith(compareBy({ it.startMs }, { it.id })) }

    override suspend fun find(
        channelId: Long,
        startMs: Long,
        title: String,
    ): ReminderEntity? =
        rows.value.firstOrNull { it.channelId == channelId && it.startMs == startMs && it.title == title }

    override suspend fun delete(id: Long) {
        rows.update { list -> list.filterNot { it.id == id } }
    }
}

fun testReminder(
    id: Long,
    channelId: Long = 1,
    title: String = "Programme $id",
    startMs: Long,
    stopMs: Long = startMs + 30 * 60_000L,
): ReminderEntity = ReminderEntity(id = id, channelId = channelId, title = title, startMs = startMs, stopMs = stopMs)
