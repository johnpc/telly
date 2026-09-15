package com.johncorser.telly.features.guide

import com.johncorser.telly.features.mylist.MyListMenuHost
import com.johncorser.telly.features.playback.ChannelActions
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope

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
    /** The host's My-list context; the sheet's My-list rows act through it. */
    val myList: MyListMenuHost? get() = actions.myList

    fun toggleFavorite(channel: ChannelEntity) = actions.toggleFavorite(channel)

    fun hide(channel: ChannelEntity) {
        zapAway(channel)
        actions.hide(channel)
    }
}

/** The guide controller's bundle assembly (kept here for its file gate). */
internal fun guideSheetChannelActions(
    dao: ChannelDao,
    scope: CoroutineScope,
    myList: MyListMenuHost?,
    zapAway: (ChannelEntity) -> Unit,
): GuideSheetChannelActions = GuideSheetChannelActions(ChannelActions(dao, scope, myList), zapAway)
