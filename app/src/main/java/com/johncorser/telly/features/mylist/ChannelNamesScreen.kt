package com.johncorser.telly.features.mylist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playlist.db.displayName
import com.johncorser.telly.features.settings.SettingsScreenDims
import com.johncorser.telly.features.settings.SettingsScreenSheet
import com.johncorser.telly.features.settings.SettingsScreenTextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * "Channel names editor" (Channel options §41): the channel-edit list idiom
 * with OK opening a rename dialog instead of a favorite toggle; renames
 * persist like the pane's Channel-name row and BACK closes the dialog
 * first, then pops the route.
 */
@Composable
fun ChannelNamesEditorScreen(deps: PlaybackDeps) {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val viewModel = remember { ChannelNamesViewModel(deps.sources.channelDao, scope) }
    DisposableEffect(Unit) { onDispose { scope.cancel() } }
    val rows by viewModel.rows.collectAsState()
    val editing by viewModel.editing.collectAsState()
    BackHandler(enabled = editing != null) { viewModel.closeDialog() }
    ChannelEditScreenList(
        title = stringResource(R.string.channel_names_editor_title),
        rows = rows,
        showStars = false,
        onToggle = viewModel::edit,
        onMove = { _, _ -> },
        hint = stringResource(R.string.channel_names_editor_hint),
    )
    editing?.let { channel ->
        SettingsScreenSheet(title = "Channel name") {
            Column(Modifier.padding(SettingsScreenDims.panePadding)) {
                SettingsScreenTextField(initial = channel.displayName, onCommit = viewModel::rename)
            }
        }
    }
}
