package com.johncorser.telly.features.playback

import com.johncorser.telly.features.mylist.MyListHooks
import com.johncorser.telly.features.panel.PanelLock
import com.johncorser.telly.features.pip.PipState

/**
 * Cross-slice hooks the playback surface plugs into (nav + parental + PIP +
 * My list). Moved out of PlaybackViewModel.kt: every slice that adds a
 * playback hook lands here, keeping the ViewModel under the file-length gate.
 */
class PlaybackHooks(
    val panelLock: PanelLock = PanelLock(),
    val onOpenSettings: () -> Unit = {},
    val onOpenMultiview: () -> Unit = {},
    /** The activity's real enterPictureInPictureMode call. */
    val onEnterPip: () -> Unit = {},
    /** Shared PIP mode; gates the background stop while the window is up. */
    val pip: PipState = PipState(),
    /** My-list store + the management screens behind the sheet rows. */
    val myList: MyListHooks = MyListHooks(),
)
