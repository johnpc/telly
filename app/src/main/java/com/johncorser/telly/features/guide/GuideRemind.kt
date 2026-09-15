package com.johncorser.telly.features.guide

import com.johncorser.telly.features.reminders.GuideReminders
import com.johncorser.telly.features.reminders.ReminderKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Resolves the cell dropdown's Remind row against the focused row and the
 * open cell menu: toggles the programme's reminder and supplies the live
 * "Remind"/"Remove reminder" label. Without an attached [reminders] seam
 * the row behaves like the other unbuilt dropdown rows.
 */
class GuideRemind(
    private val focusedRow: () -> GuideRow?,
    private val layer: () -> GuideLayer,
) {
    /** The reminders seam; attached by GuideController when the slice is wired. */
    var reminders: GuideReminders? = null

    /** Pending-reminder keys driving the dropdown's live Remind label. */
    val keys: StateFlow<Set<ReminderKey>> get() = reminders?.keys ?: NO_REMINDERS

    /** The dropdown label for [action]: "Remove reminder" while one is set. */
    fun label(
        action: GuideCellAction,
        keys: Set<ReminderKey>,
    ): String {
        if (action != GuideCellAction.REMIND || reminders == null) return action.label
        return GuideReminders.labelFor(keys, focusedRow()?.channel?.id, menuProgram())
    }

    /** True when the focused cell's programme reminder was toggled. */
    fun toggle(): Boolean {
        val hook = reminders ?: return false
        val channel = focusedRow()?.channel ?: return false
        val program = menuProgram() ?: return false
        hook.toggle(channel, program)
        return true
    }

    private fun menuProgram() = (layer() as? GuideLayer.CellMenu)?.cell?.program

    private companion object {
        /** The label source while no reminders seam is attached. */
        val NO_REMINDERS = MutableStateFlow(emptySet<ReminderKey>())
    }
}
