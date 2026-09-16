package com.johncorser.telly.features.groups

import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.settings.SettingsRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * "Assign EPG": a scrollable picker of every EPG channel id with stored
 * data, "Auto (tvg-id)" first. OK persists the per-channel override
 * (channels.epgOverride), which [ChannelEntity.epgId] resolves for every
 * guide/panel/playback EPG lookup; the flag survives playlist refreshes
 * with the rest of [ChannelFlags][com.johncorser.telly.features.playlist.db.ChannelFlags].
 */
class AssignEpgSession(
    private val channel: ChannelEntity,
    epgIds: StateFlow<List<String>>,
    private val update: suspend (ChannelEntity) -> Unit,
    private val onDone: () -> Unit,
    private val scope: CoroutineScope,
) : GroupToolSession {
    override val ui: StateFlow<GroupToolUi> =
        epgIds.map(::uiOf).sessionState(scope, uiOf(epgIds.value))

    override fun activate(rowId: String) {
        val override =
            when {
                rowId == AUTO -> null
                rowId.startsWith("epg:") -> rowId.removePrefix("epg:")
                else -> return
            }
        scope.launch {
            update(channel.copy(flags = channel.flags.copy(epgOverride = override)))
            onDone()
        }
    }

    override fun submitText(text: String) = Unit

    override fun submitPin(pin: String) = Unit

    private fun uiOf(ids: List<String>): GroupToolUi =
        GroupToolUi(
            title = "Assign EPG",
            rows =
                listOf<SettingsRow>(
                    SettingsRow.Value(
                        id = AUTO,
                        title = "Auto (tvg-id)",
                        summary = channel.source.tvgId,
                        selected = channel.flags.epgOverride == null,
                    ),
                ) +
                    ids.map { id ->
                        SettingsRow.Value(id = "epg:$id", title = id, selected = channel.flags.epgOverride == id)
                    },
        )

    private companion object {
        /** The Auto row's id; real ids ride behind the "epg:" prefix. */
        const val AUTO = "auto"
    }
}
