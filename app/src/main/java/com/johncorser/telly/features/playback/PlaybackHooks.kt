package com.johncorser.telly.features.playback

import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.mylist.MyListHooks
import com.johncorser.telly.features.panel.PanelLock
import com.johncorser.telly.features.pip.PipState
import com.johncorser.telly.features.player.PlayerPlatformHooks
import com.johncorser.telly.features.recording.RecordingCenter

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
    /** Opens the search route (the sheet's Search row + the quick-bar slot). */
    val onOpenSearch: () -> Unit = {},
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
    /** Opens the Recordings library (quick-bar RECORDINGS slot + guide rail). */
    val onOpenRecordings: () -> Unit = {},
    /** The DVR facade behind the sheet's Record rows (null in JVM tests). */
    val recording: RecordingCenter? = null,
    /** Parental PIN policy behind Block channel + the tune gate. */
    val parental: ParentalControls? = null,
    /** Shared by the guide's and playback's gates ("Until app restart"). */
    val blockSession: BlockSession = BlockSession(),
)
