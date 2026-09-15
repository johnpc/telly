package com.johncorser.telly.features.playback

import com.johncorser.telly.features.mylist.MyListHooks
import com.johncorser.telly.features.panel.PanelLock
import com.johncorser.telly.features.pip.PipState
import com.johncorser.telly.features.player.PlayerPlatformHooks

/**
 * Cross-slice hooks the playback surface plugs into (nav + parental + PIP +
 * My list + platform glue). MainActivity supplies the platform bundle (AFR
 * display-mode switching, the external-player chooser); the playback screen
 * copies its own navigation lambdas in on top. Every slice that adds a
 * playback hook lands here, keeping the ViewModel under the file-length gate.
 */
data class PlaybackHooks(
    val panelLock: PanelLock = PanelLock(),
    val onOpenSettings: () -> Unit = {},
    val onOpenMultiview: () -> Unit = {},
    /** The activity's real enterPictureInPictureMode call. */
    val onEnterPip: () -> Unit = {},
    /** Shared PIP mode; gates the background stop while the window is up. */
    val pip: PipState = PipState(),
    /** My-list store + the management screens behind the sheet rows. */
    val myList: MyListHooks = MyListHooks(),
    /** MainActivity's platform glue: AFR + the external player. */
    val platform: PlayerPlatformHooks = PlayerPlatformHooks(),
    /** Settings → Remote control → Player key remaps, read per key press. */
    val playerKeymap: () -> PlayerKeymap = { PlayerKeymap() },
)
