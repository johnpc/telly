package com.johncorser.telly.features.search

import com.johncorser.telly.features.epg.ProgramTitle
import com.johncorser.telly.features.mylist.MyListKeys
import com.johncorser.telly.features.mylist.MyListMenu
import com.johncorser.telly.features.mylist.MyListProgramme
import com.johncorser.telly.features.recording.RecordingMenu
import com.johncorser.telly.features.recording.RecordingPrompt
import com.johncorser.telly.features.recording.RecordingSupport
import com.johncorser.telly.features.reminders.GuideReminders
import com.johncorser.telly.features.reminders.ReminderKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The programme dropdown's live actions (guide-cell parity, capture 27):
 * Remind toggles a reminder and relabels, Record schedules the airing's
 * slot, Custom recording opens the DVR form, Add to My list toggles the
 * programme, and Program description stays on the branded placeholder
 * exactly like the guide's cell dropdown. The collaborators are the SAME
 * stores the guide drives; a missing one (JVM tests) falls back to
 * coming-soon like the guide's controllers do.
 */
class SearchProgramMenu(
    hooks: SearchHooks,
    private val overlays: SearchOverlays,
    clock: () -> Long,
    scope: CoroutineScope,
) {
    private val reminders = hooks.reminders
    private val myList = hooks.myList?.let { MyListMenu(it, clock, scope) }

    /** The DVR adapter; its prompts map onto search overlays below. */
    val recording: RecordingMenu? = hooks.recording?.let { RecordingMenu(it, scope, clock, ::onPrompt) }

    /** Pending-reminder keys driving the dropdown's live Remind label. */
    val reminderKeys: StateFlow<Set<ReminderKey>> = reminders?.keys ?: NO_REMINDERS

    /** Saved-programme keys driving the flipped My-list label. */
    val myListKeys: StateFlow<Set<String>> = myList?.keys ?: NO_SAVED

    /** The row label for [action] on [hit] ("Remove reminder" while set). */
    fun label(
        action: SearchProgramAction,
        hit: SearchProgramHit,
        reminderKeys: Set<ReminderKey>,
        myListKeys: Set<String>,
    ): String =
        when {
            action == SearchProgramAction.REMIND && reminders != null ->
                GuideReminders.labelFor(reminderKeys, hit.channel.id, hit.program)
            action == SearchProgramAction.ADD_TO_MY_LIST && myList != null ->
                MyListKeys.label(MyListKeys.saved(myListKeys, hit.channel, hit.program.startMs))
            else -> action.label
        }

    fun onAction(
        action: SearchProgramAction,
        hit: SearchProgramHit,
    ) {
        when (action) {
            SearchProgramAction.REMIND -> toggleReminder(hit)
            SearchProgramAction.RECORD -> withRecording { record(it, hit) }
            SearchProgramAction.CUSTOM_RECORDING -> withRecording { it.onCustomRecording(hit.channel) }
            SearchProgramAction.ADD_TO_MY_LIST -> toggleMyList(hit)
            // Uncaptured in the guide's cell dropdown too: stays branded.
            SearchProgramAction.PROGRAM_DESCRIPTION ->
                overlays.show(SearchOverlay.ComingSoon(action.label))
        }
    }

    private fun toggleReminder(hit: SearchProgramHit) {
        val hook = reminders ?: return overlays.show(SearchOverlay.ComingSoon(SearchProgramAction.REMIND.label))
        hook.toggle(hit.channel, hit.program)
        overlays.dismiss()
    }

    private fun toggleMyList(hit: SearchProgramHit) {
        val menu = myList ?: return overlays.show(SearchOverlay.ComingSoon(SearchProgramAction.ADD_TO_MY_LIST.label))
        menu.toggle(hit.channel, MyListProgramme.of(hit.program))
        overlays.dismiss()
    }

    /** Guide-cell parity: Record schedules the airing row's exact slot. */
    private fun record(
        menu: RecordingMenu,
        hit: SearchProgramHit,
    ) {
        menu.onRecordProgramme(
            channel = hit.channel,
            title = ProgramTitle.of(hit.program.details),
            startMs = hit.program.startMs,
            endMs = hit.program.endMs,
        )
    }

    /** Record rows act through the DVR menu (coming-soon while unwired). */
    private fun withRecording(action: (RecordingMenu) -> Unit) {
        val menu = recording ?: return overlays.show(SearchOverlay.ComingSoon(SearchProgramAction.RECORD.label))
        action(menu)
    }

    /** BACK with an overlay up: drop any open DVR form state, then close. */
    fun closeOverlay(): Boolean {
        if (overlays.current.value == SearchOverlay.CustomRecording) recording?.dismissForm()
        return overlays.dismiss()
    }

    /** DVR prompts onto search overlays (Stop never fires off a slot). */
    private fun onPrompt(prompt: RecordingPrompt) {
        when (prompt) {
            RecordingPrompt.CustomForm -> overlays.show(SearchOverlay.CustomRecording)
            is RecordingPrompt.Unsupported ->
                overlays.show(SearchOverlay.Message(SearchProgramAction.RECORD.label, RecordingSupport.HLS_MESSAGE))
            RecordingPrompt.Done, is RecordingPrompt.StopConfirm -> overlays.dismiss()
        }
    }

    private companion object {
        val NO_REMINDERS = MutableStateFlow(emptySet<ReminderKey>())
        val NO_SAVED = MutableStateFlow(emptySet<String>())
    }
}
