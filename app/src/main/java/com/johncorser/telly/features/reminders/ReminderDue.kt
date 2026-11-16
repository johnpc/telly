package com.johncorser.telly.features.reminders

import com.johncorser.telly.core.state.StateMachine
import com.johncorser.telly.features.reminders.db.ReminderEntity

/**
 * Pure due-state math for one reminder: DUE once "now" reaches
 * start − lead (TiviMate pops the reminder shortly before the programme
 * starts), EXPIRED once the programme ended without firing (a reminder set
 * for the past never pops), PENDING otherwise.
 */
@StateMachine("Reminder")
object ReminderDue {
    const val MINUTE_MS = 60_000L

    enum class State { PENDING, DUE, EXPIRED }

    fun stateOf(
        reminder: ReminderEntity,
        nowMs: Long,
        leadMs: Long,
    ): State =
        when {
            nowMs >= reminder.stopMs -> State.EXPIRED
            nowMs >= reminder.startMs - leadMs -> State.DUE
            else -> State.PENDING
        }
}
