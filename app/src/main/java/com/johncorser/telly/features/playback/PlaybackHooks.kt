package com.johncorser.telly.features.playback

import com.johncorser.telly.features.mylist.InMemoryMyListStore
import com.johncorser.telly.features.mylist.MyListStore
import com.johncorser.telly.features.panel.PanelLock

/** Cross-slice hooks the playback surface plugs into (nav + parental). */
class PlaybackHooks(
    val panelLock: PanelLock = PanelLock(),
    val onOpenSettings: () -> Unit = {},
    val onOpenMultiview: () -> Unit = {},
    val onOpenManageFavorites: () -> Unit = {},
    val onOpenReorderChannels: (String) -> Unit = {},
    /** Saved My-list programmes behind the sheet's "Add to My list". */
    val myListStore: MyListStore = InMemoryMyListStore(),
)
