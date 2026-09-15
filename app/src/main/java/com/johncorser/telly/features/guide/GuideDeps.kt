package com.johncorser.telly.features.guide

import com.johncorser.telly.features.mylist.MyListStore
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.player.external.ExternalPlayer
import com.johncorser.telly.features.reminders.GuideReminders

/**
 * Everything the guide screen needs from the composition root: the
 * playback bundle (channels, EPG, engine factory, store, clock) plus the
 * settings the guide honors and the reminders seam behind the dropdown.
 */
class GuideDeps(
    val playback: PlaybackDeps,
    val pastDays: () -> Int,
    val reminders: GuideReminders? = null,
) {
    /** Saved My-list programmes; the guide shares the playback store. */
    val myList: MyListStore get() = playback.myList
}

/** Actions the guide triggers outside itself: navigation + external player. */
class GuideCallbacks(
    val onFullscreen: () -> Unit,
    val onOpenSearch: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenManageFavorites: () -> Unit = {},
    val onOpenReorderChannels: (String) -> Unit = {},
    val external: ExternalPlayer = ExternalPlayer.OFF,
)
