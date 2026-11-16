package com.johncorser.telly.features.reminders

import com.johncorser.telly.features.reminders.db.ReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * Watches the injected clock (a minute tick, the GuideNow pattern) and the
 * upcoming reminders; when a reminder's programme is within the configured
 * lead time it emits [onDue] EXACTLY once and deletes the row — the
 * TiviMate popup appears once. The claim is PERSISTED first
 * (`firedAtMs`, a guarded PENDING -> FIRED update), so a process that dies
 * between popup and delete removes the row on restart instead of firing it
 * again. Reminders whose programme already ended are dropped silently. No
 * wall-clock reads: clock and ticks are injected.
 */
class ReminderEngine(
    private val store: ReminderStore,
    private val clock: () -> Long,
    private val leadMinutes: () -> Int,
    private val onDue: (ReminderEntity) -> Unit,
) {
    /** Ids already handled this session; guards re-emission until Room catches up. */
    private val handled = mutableSetOf<Long>()

    /** Sweeps on every tick and on every change to the reminder list. */
    fun startIn(
        scope: CoroutineScope,
        ticks: Flow<Unit>,
    ): Job =
        scope.launch {
            combine(store.reminders, ticks.onStart { emit(Unit) }) { reminders, _ -> reminders }
                .collect { sweep(it) }
        }

    private suspend fun sweep(reminders: List<ReminderEntity>) {
        handled.retainAll(reminders.mapTo(mutableSetOf()) { it.id })
        val now = clock()
        val leadMs = leadMinutes() * ReminderDue.MINUTE_MS
        reminders.filterNot { it.id in handled }.forEach { reminder ->
            when {
                // Claimed by a previous process that died before the delete.
                reminder.firedAtMs != 0L -> drop(reminder)
                else ->
                    when (ReminderDue.stateOf(reminder, now, leadMs)) {
                        ReminderDue.State.DUE -> fire(reminder, now)
                        ReminderDue.State.EXPIRED -> drop(reminder)
                        ReminderDue.State.PENDING -> Unit
                    }
            }
        }
    }

    private suspend fun fire(
        reminder: ReminderEntity,
        now: Long,
    ) {
        handled += reminder.id
        if (!store.markFired(reminder.id, now)) return
        onDue(reminder)
        store.remove(reminder.id)
    }

    private suspend fun drop(reminder: ReminderEntity) {
        handled += reminder.id
        store.remove(reminder.id)
    }
}
