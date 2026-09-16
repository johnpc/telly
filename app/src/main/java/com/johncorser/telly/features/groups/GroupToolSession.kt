package com.johncorser.telly.features.groups

import com.johncorser.telly.features.settings.SettingsRow
import com.johncorser.telly.features.settings.isLocked
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * What one group/bulk-management tool screen shows: a settings-style sheet
 * titled [title] with either a row list, a text editor ([textInitial]
 * non-null) or the parental PIN wheel ([pin]). Pure data — the shared
 * GroupToolScreen renders it for both the guide's and the panel's sheet.
 */
data class GroupToolUi(
    val title: String,
    val rows: List<SettingsRow> = emptyList(),
    val textInitial: String? = null,
    val pin: Boolean = false,
) {
    /** The row D-pad focus starts on: the first activatable one. */
    val focusId: String? get() = rows.firstOrNull { it.activatable() }?.id
}

private fun SettingsRow.activatable(): Boolean =
    when (this) {
        is SettingsRow.Header, is SettingsRow.Note -> false
        else -> !isLocked()
    }

/**
 * One live tool screen behind a context-sheet row (Create group, Group
 * options, Copy channels, Assign EPG, Manage blocking/visibility). The
 * session owns the multi-step state; hosts push it as a layer whose BACK
 * pops back to the sheet, and [GroupTools] wires persistence in.
 */
interface GroupToolSession {
    val ui: StateFlow<GroupToolUi>

    /** True when PIN prompts use the masked keyboard entry, not the wheels. */
    val keyboardPin: Boolean get() = false

    /** OK on a row; ids are the session's own ("done", "channel:7", …). */
    fun activate(rowId: String)

    /** IME Done in the name editor screens. */
    fun submitText(text: String)

    /** The parental PIN wheel's submit (Manage blocking gate). */
    fun submitPin(pin: String)
}

/** Sessions expose eagerly shared UI state so hosts read `.value` synchronously. */
internal fun <T> Flow<T>.sessionState(
    scope: CoroutineScope,
    initial: T,
): StateFlow<T> = stateIn(scope, SharingStarted.Eagerly, initial)
