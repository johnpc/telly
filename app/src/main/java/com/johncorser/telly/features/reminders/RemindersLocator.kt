package com.johncorser.telly.features.reminders

import android.content.Context
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.db.TellyDatabase
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.playback.PlaybackTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.TimeZone

private object RemindersHolder {
    @Volatile
    var hub: RemindersHub? = null
}

/**
 * The app-scoped reminders hub over [ServiceLocator]'s singletons; rebuilt
 * (old scope cancelled) when the database instance changes so no collector
 * outlives a wiped database.
 */
fun ServiceLocator.remindersHub(context: Context): RemindersHub {
    val db = database(context)
    return RemindersHolder.hub?.takeIf { it.database === db } ?: rebuildRemindersHub(context, db)
}

private fun ServiceLocator.rebuildRemindersHub(
    context: Context,
    db: TellyDatabase,
): RemindersHub =
    synchronized(RemindersHolder) {
        RemindersHolder.hub?.takeIf { it.database === db } ?: run {
            RemindersHolder.hub?.scope?.cancel()
            buildRemindersHub(context, db).also { RemindersHolder.hub = it }
        }
    }

private fun ServiceLocator.buildRemindersHub(
    context: Context,
    db: TellyDatabase,
): RemindersHub {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val store = ReminderStore(db.reminderDao())
    val channels = db.channelDao().observeVisible()
    val names = ReminderChannelNames(channels)
    val zone = TimeZone.getDefault()
    val popup = ReminderPopupController(keyValueStore(context), scope, zone)
    val prefs = settingsRepository(context)
    ReminderEngine(
        store = store,
        clock = clock,
        leadMinutes = { prefs.get(TellySettings.REMINDER_LEAD_MINUTES) },
        onDue = { reminder -> scope.launch { popup.show(reminder, names.nameOf(reminder.channelId)) } },
    ).startIn(scope, PlaybackTime.minuteBoundaryTicks(clock))
    return RemindersHub(
        database = db,
        store = store,
        guide = GuideReminders(store, scope),
        settingsFeed = ReminderSettingsFeed(store, channels, zone),
        popup = popup,
        scope = scope,
    )
}
