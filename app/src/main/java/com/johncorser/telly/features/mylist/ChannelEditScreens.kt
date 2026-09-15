package com.johncorser.telly.features.mylist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.johncorser.telly.R
import com.johncorser.telly.features.playback.PlaybackDeps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * "Manage Favorites" (context sheet, All-channels section): every channel
 * with its favorite state, favorites first in their managed order. OK
 * toggles, LEFT/RIGHT moves a favorite within the order — the documented
 * TiviMate premium favorites management, uncapturable in the free tier.
 */
@Composable
fun ManageFavoritesScreen(deps: PlaybackDeps) {
    ChannelEditScreenHost(
        title = stringResource(R.string.manage_favorites_title),
        deps = deps,
        group = null,
        showStars = true,
    )
}

/**
 * "Reorder channels" (context sheet, All-channels section): the sheet's
 * current group in its live order; LEFT/RIGHT moves the focused channel,
 * persisted through the per-channel sort index the guide/panel queries
 * honor (the Favorites group reorders the favorites order instead).
 */
@Composable
fun ReorderChannelsScreen(
    deps: PlaybackDeps,
    group: String,
) {
    ChannelEditScreenHost(
        title = stringResource(R.string.reorder_channels_title),
        deps = deps,
        group = group,
        showStars = false,
    )
}

@Composable
private fun ChannelEditScreenHost(
    title: String,
    deps: PlaybackDeps,
    group: String?,
    showStars: Boolean,
) {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val viewModel = remember { ChannelEditViewModel(deps.sources.channelDao, scope, group) }
    DisposableEffect(Unit) { onDispose { scope.cancel() } }
    val rows by viewModel.rows.collectAsState()
    ChannelEditScreenList(
        title = title,
        rows = rows,
        showStars = showStars,
        onToggle = viewModel::toggle,
        onMove = viewModel::move,
    )
}
