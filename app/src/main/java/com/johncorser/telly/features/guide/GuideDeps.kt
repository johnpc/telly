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
    /** Appearance -> TV guide -> Number of visible channels (7 = today). */
    val visibleRows: () -> Int = { GuideGeometry.VISIBLE_ROWS },
    /** Settings → Remote control → TV guide key remaps, read per key press. */
    val keymap: () -> GuideKeymap = { GuideKeymap() },
) {
    /** Saved My-list programmes; the guide shares the playback store. */
    val myList: MyListStore get() = playback.myList
}

/** Actions the guide triggers outside itself: navigation + external player. */
data class GuideCallbacks(
    val onFullscreen: () -> Unit,
    val onOpenSearch: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenManageFavorites: () -> Unit = {},
    val onOpenReorderChannels: (String) -> Unit = {},
    /** The Channel-options pane's "Channel names editor" route. */
    val onOpenNamesEditor: () -> Unit = {},
    val external: ExternalPlayer = ExternalPlayer.OFF,
)
