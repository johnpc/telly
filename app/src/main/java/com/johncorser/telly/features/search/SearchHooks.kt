package com.johncorser.telly.features.search

import com.johncorser.telly.features.mylist.MyListStore
import com.johncorser.telly.features.recording.RecordingCenter
import com.johncorser.telly.features.reminders.GuideReminders

/**
 * Cross-slice hooks the search surface plugs into: the programme
 * dropdown's collaborators (the SAME app-scoped stores the guide's cell
 * dropdown drives, so the two menus can never drift) plus the mic orb's
 * platform voice-recognition seam. Null collaborators (JVM tests) keep
 * the row's coming-soon fallback.
 */
data class SearchHooks(
    /** Reminders seam behind the Remind row (relabels while one is set). */
    val reminders: GuideReminders? = null,
    /** Saved-programme store behind the Add to My list row. */
    val myList: MyListStore? = null,
    /** DVR facade behind the Record / Custom recording rows. */
    val recording: RecordingCenter? = null,
    /** MainActivity's speech-recognizer glue behind the voice orb. */
    val voice: VoiceSearch = VoiceSearch(),
)
