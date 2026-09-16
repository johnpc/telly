package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.playlist.db.displayName
import com.johncorser.telly.features.settings.SettingsScreenChoiceList
import com.johncorser.telly.features.settings.SettingsScreenDims
import com.johncorser.telly.features.settings.SettingsScreenRows
import com.johncorser.telly.features.settings.SettingsScreenSheet
import com.johncorser.telly.features.settings.SettingsScreenTextField

/**
 * The §41 "Channel options" pane (captures 41-42): a settings-shell sheet
 * titled with the channel's display name, rows live off the FRESH Room row
 * (a rename updates the title in place). Shared verbatim between the guide
 * and the playback panel hosts; in-pane dialogs (rename editor, the
 * decoder/EPG-offset pickers) swap the sheet's content and BACK closes
 * them first via the shared [ChannelOptionsController].
 */
@Composable
internal fun GuideScreenChannelOptionsPane(
    channel: ChannelEntity,
    options: ChannelOptionsController,
    onRow: (ChannelEntity, String) -> Unit,
) {
    val live by options.store.observe(channel.id).collectAsState(initial = channel)
    val current = live ?: channel
    val dialog by options.dialog.collectAsState()
    when (val active = dialog) {
        null ->
            SettingsScreenSheet(title = current.displayName) {
                SettingsScreenRows(rows = options.rows(current), onActivate = { rowId -> onRow(current, rowId) })
            }
        is ChannelOptionsDialog.Rename ->
            SettingsScreenSheet(title = "Channel name") {
                Column(Modifier.padding(SettingsScreenDims.panePadding)) {
                    SettingsScreenTextField(initial = active.initial, onCommit = options::submitRename)
                }
            }
        is ChannelOptionsDialog.Picker ->
            SettingsScreenSheet(title = active.kind.title) {
                SettingsScreenChoiceList(
                    options = options.pickerOptions(active.kind),
                    current = options.pickerCurrent(active.kind, current),
                    onChoose = options::choose,
                )
            }
    }
}
