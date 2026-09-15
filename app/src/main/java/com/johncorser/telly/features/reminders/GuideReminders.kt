package com.johncorser.telly.features.reminders

import com.johncorser.telly.features.epg.db.ProgramEntity
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Identity of a reminder as the guide dropdown sees it. */
data class ReminderKey(
    val channelId: Long,
    val startMs: Long,
    val title: String,
)

/**
 * The guide-facing reminders seam: the cell dropdown's Remind row toggles a
 * reminder for the focused future programme and relabels to "Remove
 * reminder" while one is set (the reference gated the row behind premium;
 * telly acts on it per the charter's no-premium-tier precedent).
 */
class GuideReminders(
    private val store: ReminderStore,
    private val scope: CoroutineScope,
) {
    /** Keys of every pending reminder; drives the dropdown label live. */
    val keys: StateFlow<Set<ReminderKey>> =
        store.reminders
            .map { list -> list.mapTo(mutableSetOf()) { ReminderKey(it.channelId, it.startMs, it.title) } }
            .stateIn(scope, SharingStarted.Eagerly, emptySet())

    fun toggle(
        channel: ChannelEntity,
        program: ProgramEntity,
    ) {
        scope.launch { store.toggle(channel.id, program) }
    }

    companion object {
        const val SET_LABEL = "Remind"
        const val UNSET_LABEL = "Remove reminder"

        /** The dropdown row label for the cell's programme under [keys]. */
        fun labelFor(
            keys: Set<ReminderKey>,
            channelId: Long?,
            program: ProgramEntity?,
        ): String {
            val key = program?.let { p -> channelId?.let { ReminderKey(it, p.startMs, p.details.title) } }
            return if (key != null && key in keys) UNSET_LABEL else SET_LABEL
        }
    }
}
