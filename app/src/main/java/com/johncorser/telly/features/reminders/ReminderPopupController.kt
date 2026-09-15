package com.johncorser.telly.features.reminders

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.features.reminders.db.ReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.TimeZone

/** What the root-level reminder popup renders. */
data class ReminderPopupData(
    val channelId: Long,
    val channelName: String,
    val programmeTitle: String,
    val startsAtLabel: String,
)

/**
 * State behind the root-hosted reminder popup: a due reminder shows title +
 * channel + "Starts at HH:MM" with Watch/Dismiss and auto-dismisses after
 * [AUTO_DISMISS_MS]. Watch persists the channel as last-channel (the search
 * tune precedent) and bumps [tuneEpoch] so the route host recreates the
 * playback screen, which cold-starts onto the persisted channel.
 */
class ReminderPopupController(
    private val store: KeyValueStore,
    private val scope: CoroutineScope,
    private val zone: TimeZone,
) {
    private val mutablePopup = MutableStateFlow<ReminderPopupData?>(null)
    val popup: StateFlow<ReminderPopupData?> = mutablePopup.asStateFlow()

    private val mutableEpoch = MutableStateFlow(0)
    val tuneEpoch: StateFlow<Int> = mutableEpoch.asStateFlow()

    private var hideJob: Job? = null

    /** Fired by the engine: shows the popup and arms the auto-dismiss. */
    fun show(
        reminder: ReminderEntity,
        channelName: String,
    ) {
        mutablePopup.value =
            ReminderPopupData(
                channelId = reminder.channelId,
                channelName = channelName,
                programmeTitle = reminder.title,
                startsAtLabel = "Starts at " + ProgramTimes.startTime(reminder.startMs, zone),
            )
        hideJob?.cancel()
        hideJob =
            scope.launch {
                delay(AUTO_DISMISS_MS)
                mutablePopup.value = null
            }
    }

    /** Watch: persist the tune target, force a fresh playback screen. */
    fun watch(data: ReminderPopupData) {
        store.putLong(TuneController.LAST_CHANNEL_KEY, data.channelId)
        mutableEpoch.value += 1
        dismiss()
    }

    fun dismiss() {
        hideJob?.cancel()
        mutablePopup.value = null
    }

    companion object {
        /** The popup lingers ~30 s, then goes away on its own. */
        const val AUTO_DISMISS_MS = 30_000L
    }
}
