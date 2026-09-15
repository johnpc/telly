package com.johncorser.telly.features.reminders

import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.TimeZone

/** One scheduled reminder as the settings pane lists it. */
data class ReminderListItem(
    val id: Long,
    val title: String,
    val channelName: String,
    val airTime: String,
)

/**
 * Feeds Settings -> Other -> Reminders: the scheduled reminders joined with
 * their channel names and a TiviMate-style air-time stamp, in air order.
 */
class ReminderSettingsFeed(
    private val store: ReminderStore,
    channels: Flow<List<ChannelEntity>>,
    private val zone: TimeZone,
) {
    val items: Flow<List<ReminderListItem>> =
        combine(store.reminders, channels) { reminders, list ->
            reminders.map { reminder ->
                ReminderListItem(
                    id = reminder.id,
                    title = reminder.title,
                    channelName = list.firstOrNull { it.id == reminder.channelId }?.source?.name.orEmpty(),
                    airTime = ProgramTimes.clock(reminder.startMs, zone),
                )
            }
        }

    /** The pane's OK -> delete confirm lands here. */
    suspend fun delete(id: Long) = store.remove(id)
}
