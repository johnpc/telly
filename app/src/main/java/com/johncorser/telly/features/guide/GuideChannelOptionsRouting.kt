package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * Routes a Channel-options pane row through the shared
 * [ChannelOptionsController]: value rows act in place (rename dialog,
 * pickers, the external toggle) and the effect rows reuse the sheet's
 * flows verbatim — Block opens the PIN dialog (returning to the pane on
 * cancel), Hide zaps away first and lands on the grid, and the names
 * editor pushes its route with the chrome cleared, like Search.
 *
 * [channel] is the FRESH entity the pane observes, so an effect written
 * back (block's flag flip) never clobbers an in-pane rename.
 */
fun GuideMenuController.onChannelOption(
    channel: ChannelEntity,
    rowId: String,
) {
    when (channelActions.options.onRow(channel, rowId)) {
        ChannelOptionsEffect.BLOCK ->
            show(GuideLayer.BlockPin(channel, channelActions.blocker.mode(), back = layer.value))
        ChannelOptionsEffect.HIDE -> {
            channelActions.hide(channel)
            reset()
        }
        ChannelOptionsEffect.NAMES_EDITOR -> {
            reset()
            callbacks.onOpenNamesEditor()
        }
        null -> Unit
    }
}
