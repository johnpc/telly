package com.johncorser.telly.features.groups

import com.johncorser.telly.features.playlist.ChannelImporter
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.displayName
import com.johncorser.telly.features.settings.SettingsRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * "Copy channels": pick the target custom group (skipped when exactly one
 * exists), then check channels — OK toggles a row, the explicit Done row
 * copies the checked channels into the group by refresh-stable key.
 */
class CopyChannelsSession(
    private val groups: List<CustomGroup>,
    private val channels: StateFlow<List<ChannelEntity>>,
    private val store: CustomGroupStore,
    private val onDone: () -> Unit,
    private val scope: CoroutineScope,
) : GroupToolSession {
    private val target = MutableStateFlow(groups.singleOrNull())
    private val selection = MutableStateFlow(emptySet<Long>())

    override val ui: StateFlow<GroupToolUi> =
        combine(target, selection, channels) { into, picked, list -> uiOf(into, picked, list) }
            .sessionState(scope, uiOf(target.value, emptySet(), channels.value))

    override fun activate(rowId: String) {
        val id = rowId.substringAfter(':').toLongOrNull()
        when {
            rowId == "done" -> commit()
            rowId.startsWith("group:") -> target.value = groups.firstOrNull { it.id == id }
            rowId.startsWith("channel:") && id != null ->
                selection.update { if (id in it) it - id else it + id }
        }
    }

    override fun submitText(text: String) = Unit

    override fun submitPin(pin: String) = Unit

    private fun commit() {
        val into = target.value ?: return
        val keys = channels.value.filter { it.id in selection.value }.map(ChannelImporter::keyOf)
        scope.launch {
            store.addMembers(into.id, keys)
            onDone()
        }
    }

    private fun uiOf(
        into: CustomGroup?,
        picked: Set<Long>,
        list: List<ChannelEntity>,
    ): GroupToolUi =
        when {
            groups.isEmpty() ->
                GroupToolUi(
                    title = "Copy channels",
                    rows = listOf(SettingsRow.Note("Create a custom group first")),
                )
            into == null ->
                GroupToolUi(
                    title = "Copy channels to",
                    rows = groups.map { SettingsRow.Action(id = "group:${it.id}", title = it.name) },
                )
            else ->
                GroupToolUi(
                    title = "Copy channels to ${into.name}",
                    rows =
                        listOf<SettingsRow>(SettingsRow.Action(id = "done", title = "Done")) +
                            list.filterNot { it.flags.hidden }.map { channelRow(it, picked) },
                )
        }

    private fun channelRow(
        channel: ChannelEntity,
        picked: Set<Long>,
    ): SettingsRow =
        SettingsRow.Toggle(
            id = "channel:${channel.id}",
            title = channel.displayName,
            checked = channel.id in picked,
        )
}
