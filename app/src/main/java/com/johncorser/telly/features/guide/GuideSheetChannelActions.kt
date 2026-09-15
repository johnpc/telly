package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playback.ChannelActions
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * The favorites/hide mutations the guide's long-OK sheet performs, bundled
 * so [GuideMenuController] stays under the constructor-parameter budget.
 * Hiding the previewed channel zaps away from it first (same as the panel
 * sheet); the controller resets to the grid afterwards.
 */
class GuideSheetChannelActions(
    private val actions: ChannelActions,
    private val zapAway: (ChannelEntity) -> Unit,
) {
    fun toggleFavorite(channel: ChannelEntity) = actions.toggleFavorite(channel)

    fun hide(channel: ChannelEntity) {
        zapAway(channel)
        actions.hide(channel)
    }
}
