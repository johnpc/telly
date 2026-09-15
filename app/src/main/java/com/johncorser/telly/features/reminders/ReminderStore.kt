package com.johncorser.telly.features.reminders

import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.reminders.db.ReminderDao
import com.johncorser.telly.features.reminders.db.ReminderEntity
import kotlinx.coroutines.flow.Flow

/**
 * Storage boundary for programme reminders. The guide dropdown toggles per
 * programme; the settings pane and the engine observe and delete.
 */
class ReminderStore(
    private val dao: ReminderDao,
) {
    /** Upcoming reminders in air order. */
    val reminders: Flow<List<ReminderEntity>> = dao.observeUpcoming()

    /**
     * Creates a reminder for [program] on the channel, or removes the one
     * already set for it (the dropdown row's "Remind"/"Remove reminder").
     */
    suspend fun toggle(
        channelId: Long,
        program: ProgramEntity,
    ) {
        val existing = dao.find(channelId, program.startMs, program.details.title)
        if (existing == null) {
            dao.upsert(
                ReminderEntity(
                    channelId = channelId,
                    title = program.details.title,
                    startMs = program.startMs,
                    stopMs = program.endMs,
                ),
            )
        } else {
            dao.delete(existing.id)
        }
    }

    /** Deletes one reminder (settings confirm; engine after firing). */
    suspend fun remove(id: Long) = dao.delete(id)
}
