package com.johncorser.telly.features.groups

import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.settings.SettingsRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Which per-channel flag a bulk editor manages. */
enum class BulkFlagKind(
    val title: String,
) {
    BLOCKING("Manage blocking"),
    VISIBILITY("Manage visibility"),
}

/**
 * "Manage blocking" / "Manage visibility": EVERY channel with its
 * blocked/hidden state, OK toggles and persists immediately. Entering the
 * blocking editor is gated behind the parental PIN (once per entry) when
 * parental controls are enabled with a PIN set — the PanelLock precedent.
 */
class BulkFlagSession(
    private val kind: BulkFlagKind,
    channels: StateFlow<List<ChannelEntity>>,
    private val update: suspend (ChannelEntity) -> Unit,
    private val parental: ParentalControls?,
    private val scope: CoroutineScope,
) : GroupToolSession {
    private val locked = MutableStateFlow(gated())
    private val all = channels

    override val ui: StateFlow<GroupToolUi> =
        combine(locked, channels) { gate, list -> uiOf(gate, list) }
            .sessionState(scope, uiOf(locked.value, channels.value))

    override fun activate(rowId: String) {
        if (locked.value) return
        val id = rowId.substringAfter("channel:").toLongOrNull() ?: return
        val channel = all.value.firstOrNull { it.id == id } ?: return
        scope.launch { update(toggled(channel)) }
    }

    override fun submitText(text: String) = Unit

    override fun submitPin(pin: String) {
        if (parental?.verifyPin(pin) == true) locked.value = false
    }

    private fun gated(): Boolean = kind == BulkFlagKind.BLOCKING && parental?.isEnabled == true && parental.hasPin

    private fun uiOf(
        gate: Boolean,
        list: List<ChannelEntity>,
    ): GroupToolUi =
        if (gate) {
            GroupToolUi(title = kind.title, pin = true)
        } else {
            GroupToolUi(
                title = kind.title,
                rows =
                    list.map { channel ->
                        SettingsRow.Toggle(
                            id = "channel:${channel.id}",
                            title = channel.source.name,
                            checked = flagOf(channel),
                        )
                    },
            )
        }

    private fun flagOf(channel: ChannelEntity): Boolean =
        if (kind == BulkFlagKind.BLOCKING) channel.flags.blocked else channel.flags.hidden

    private fun toggled(channel: ChannelEntity): ChannelEntity =
        channel.copy(
            flags =
                if (kind == BulkFlagKind.BLOCKING) {
                    channel.flags.copy(blocked = !channel.flags.blocked)
                } else {
                    channel.flags.copy(hidden = !channel.flags.hidden)
                },
        )
}
