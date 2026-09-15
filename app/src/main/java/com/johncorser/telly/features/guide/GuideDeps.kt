package com.johncorser.telly.features.guide

import com.johncorser.telly.features.mylist.MyListStore
import com.johncorser.telly.features.playback.PlaybackDeps

/**
 * Everything the guide screen needs from the composition root: the
 * playback bundle (channels, EPG, engine factory, store, clock) plus the
 * settings the guide honors.
 */
class GuideDeps(
    val playback: PlaybackDeps,
    val pastDays: () -> Int,
) {
    /** Saved My-list programmes; the guide shares the playback store. */
    val myList: MyListStore get() = playback.myList
}

/** Navigation the guide triggers: fullscreen playback, search, settings. */
class GuideCallbacks(
    val onFullscreen: () -> Unit,
    val onOpenSearch: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenManageFavorites: () -> Unit = {},
    val onOpenReorderChannels: (String) -> Unit = {},
)
