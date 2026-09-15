package com.johncorser.telly.features.reminders.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One scheduled programme reminder (guide dropdown "Remind", capture 27/31;
 * premium in the reference — telly ships it per the ux-spec §3 description:
 * a popup shortly before the programme starts that can switch to it). The
 * unique (channelId, startMs, title) index makes the dropdown toggle
 * idempotent: re-reminding the same programme replaces the row.
 */
@Entity(
    tableName = "reminders",
    indices = [Index(value = ["channelId", "startMs", "title"], unique = true)],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: Long,
    val title: String,
    val startMs: Long,
    val stopMs: Long,
)
