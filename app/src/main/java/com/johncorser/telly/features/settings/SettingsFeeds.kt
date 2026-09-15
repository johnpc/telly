package com.johncorser.telly.features.settings

import com.johncorser.telly.features.epg.EpgSource
import com.johncorser.telly.features.reminders.ReminderListItem

/** The live data feeds the deeper settings panes join (parameter object). */
data class SettingsFeeds(
    val epgSources: List<EpgSource> = emptyList(),
    val reminders: List<ReminderListItem> = emptyList(),
)
