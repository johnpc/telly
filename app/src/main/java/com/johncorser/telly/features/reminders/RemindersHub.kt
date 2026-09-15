package com.johncorser.telly.features.reminders

import com.johncorser.telly.core.db.TellyDatabase
import kotlinx.coroutines.CoroutineScope

/**
 * App-scoped reminders wiring: one store + engine + popup controller shared
 * by the guide dropdown, the settings pane and the root-hosted popup. Built
 * (and rebuilt when the database singleton changes — the e2e harness wipes
 * it per scenario) by [remindersHub] in RemindersLocator.kt.
 */
class RemindersHub(
    internal val database: TellyDatabase,
    val store: ReminderStore,
    val guide: GuideReminders,
    val settingsFeed: ReminderSettingsFeed,
    val popup: ReminderPopupController,
    internal val scope: CoroutineScope,
)
