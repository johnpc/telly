package com.johncorser.telly.features.groups

import com.johncorser.telly.features.settings.SettingsRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * "Create group": a name editor; committing creates an EMPTY custom group
 * (the ux-spec doesn't populate it — Copy channels fills groups) and
 * returns to the sheet's host surface.
 */
class CreateGroupSession(
    private val store: CustomGroupStore,
    private val onDone: () -> Unit,
    private val scope: CoroutineScope,
) : GroupToolSession {
    override val ui: StateFlow<GroupToolUi> = MutableStateFlow(GroupToolUi(title = "Create group", textInitial = ""))

    override fun activate(rowId: String) = Unit

    override fun submitText(text: String) {
        val name = text.trim()
        if (name.isEmpty()) return
        scope.launch {
            store.create(name)
            onDone()
        }
    }

    override fun submitPin(pin: String) = Unit
}

/**
 * "Group options" on the currently selected group: Rename / Delete when it
 * is a custom group; on playlist groups (and Favorites / All channels)
 * both rows render locked — only custom groups are editable.
 */
class GroupOptionsSession(
    private val group: String,
    private val custom: CustomGroup?,
    private val store: CustomGroupStore,
    private val onDone: () -> Unit,
    private val scope: CoroutineScope,
) : GroupToolSession {
    private enum class Step { MENU, RENAME, CONFIRM_DELETE }

    private val step = MutableStateFlow(Step.MENU)

    override val ui: StateFlow<GroupToolUi> =
        step.map(::uiOf).sessionState(scope, uiOf(Step.MENU))

    override fun activate(rowId: String) {
        when (rowId) {
            "rename" -> step.value = Step.RENAME
            "delete" -> step.value = Step.CONFIRM_DELETE
            "cancel" -> step.value = Step.MENU
            "confirm" ->
                custom?.let { target ->
                    scope.launch {
                        store.delete(target.id)
                        onDone()
                    }
                }
        }
    }

    override fun submitText(text: String) {
        val name = text.trim()
        val target = custom ?: return
        if (name.isEmpty()) return
        scope.launch {
            store.rename(target.id, name)
            onDone()
        }
    }

    override fun submitPin(pin: String) = Unit

    private fun uiOf(step: Step): GroupToolUi =
        when (step) {
            Step.MENU -> GroupToolUi(title = group, rows = menuRows())
            Step.RENAME -> GroupToolUi(title = "Rename group", textInitial = group)
            Step.CONFIRM_DELETE ->
                GroupToolUi(
                    title = "Delete $group?",
                    rows =
                        listOf(
                            SettingsRow.Action(id = "confirm", title = "Delete"),
                            SettingsRow.Action(id = "cancel", title = "Cancel"),
                        ),
                )
        }

    private fun menuRows(): List<SettingsRow> {
        val locked = custom == null
        val rows =
            listOf<SettingsRow>(
                SettingsRow.Action(id = "rename", title = "Rename group", locked = locked),
                SettingsRow.Action(id = "delete", title = "Delete group", locked = locked),
            )
        return if (locked) rows + SettingsRow.Note("Only custom groups can be renamed or deleted") else rows
    }
}
