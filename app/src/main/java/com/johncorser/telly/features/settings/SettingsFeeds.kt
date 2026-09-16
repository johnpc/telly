package com.johncorser.telly.features.settings

import com.johncorser.telly.features.epg.EpgSource
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.reminders.ReminderListItem

/** The live data feeds the deeper settings panes join (parameter object). */
data class SettingsFeeds(
    val epgSources: List<EpgSource> = emptyList(),
    val reminders: List<ReminderListItem> = emptyList(),
    /** Parental -> Blocked channels; one row per blocked channel. */
    val blockedChannels: List<ChannelEntity> = emptyList(),
    /** General -> Automatic backup; the row's live summary line. */
    val autoBackup: AutoBackupStatus = AutoBackupStatus(),
)
