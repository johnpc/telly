package com.johncorser.telly.features.playback

import com.johncorser.telly.features.guide.ChannelOptionsEffect
import com.johncorser.telly.features.playlist.db.ChannelEntity

// The panel host's Channel-options pane routing, split from
// PlaybackMenuHandler like the other sheet legs (PlaybackMenuActions).

/**
 * A pane row activation, routed through the shared controller. Value rows
 * act in place; Block reuses the sheet's PIN dialog (BACK cancels back to
 * the pane), Hide reuses the sheet's zap-away flow verbatim, and the names
 * editor pushes its route over bare playback like Search. [channel] is the
 * FRESH entity the pane observes, never the stale sheet snapshot.
 */
fun PlaybackMenuHandler.onChannelOption(
    channel: ChannelEntity,
    rowId: String,
) {
    when (actions.options.onRow(channel, rowId)) {
        ChannelOptionsEffect.BLOCK ->
            push { back -> PlaybackOverlay.BlockPin(channel, actions.blocker.mode(), back) }
        ChannelOptionsEffect.HIDE -> hide(channel)
        ChannelOptionsEffect.NAMES_EDITOR -> openScreen(hooks.onOpenNamesEditor)
        null -> Unit
    }
}

/** BACK inside the pane closes an open rename/picker dialog first. */
fun PlaybackMenuHandler.interceptBack(
    overlay: PlaybackOverlay,
    key: PlaybackKey,
): Boolean =
    key == PlaybackKey.BACK &&
        overlay is PlaybackOverlay.ChannelOptions &&
        actions.options.closeDialog()
