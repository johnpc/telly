package com.johncorser.telly.features.guide

import com.johncorser.telly.features.mylist.MyListMenuHost
import com.johncorser.telly.features.playback.ChannelActions
import com.johncorser.telly.features.playback.ChannelBlocker
import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope

/**
 * The favorites/hide/block mutations the guide's long-OK sheet performs,
 * bundled so [GuideMenuController] stays under the constructor-parameter
 * budget. Hiding the previewed channel zaps away from it first (same as the
 * panel sheet); the controller resets to the grid afterwards.
 */
class GuideSheetChannelActions(
    private val actions: ChannelActions,
    private val zapAway: (ChannelEntity) -> Unit,
    /** The PIN-gated Block/Unblock policy behind the sheet's block row. */
    val blocker: ChannelBlocker = ChannelBlocker(actions),
    /** The §41 Channel-options pane's live rows/dialogs (shared machine). */
    val options: ChannelOptionsController,
) {
    /** The host's My-list context; the sheet's My-list rows act through it. */
    val myList: MyListMenuHost? get() = actions.myList

    fun toggleFavorite(channel: ChannelEntity) = actions.toggleFavorite(channel)

    fun hide(channel: ChannelEntity) {
        zapAway(channel)
        actions.hide(channel)
    }
}

/** The guide's bundle over a [PlaybackEnv]: dao + policies from its hooks. */
internal fun guideSheetActions(
    env: PlaybackEnv,
    scope: CoroutineScope,
    myList: MyListMenuHost?,
    zapAway: (ChannelEntity) -> Unit,
): GuideSheetChannelActions {
    val actions = ChannelActions(env.channelDao, scope, myList)
    val external = env.hooks.platform.external
    return GuideSheetChannelActions(
        actions = actions,
        zapAway = zapAway,
        blocker = ChannelBlocker(actions, env.hooks.parental),
        options =
            ChannelOptionsController(ChannelOptionsStore(env.channelDao, scope)) {
                external.enabledByDefault
            },
    )
}

/**
 * The guide sheet's Block/Unblock PIN commit: a verified (or freshly set)
 * PIN flips the flag and returns to the grid like favorite/hide; a wrong
 * PIN keeps the dialog up.
 */
fun GuideMenuController.submitBlockPin(pin: String) {
    val dialog = layer.value as? GuideLayer.BlockPin ?: return
    if (channelActions.blocker.submit(pin, dialog.mode, dialog.channel)) reset()
}
