package com.johncorser.telly.features.playback

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.panel.PanelLock
import com.johncorser.telly.features.player.PlayerEngine
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.recording.RecordingCenter

/**
 * Cross-slice hooks the playback surface plugs into: nav callbacks,
 * parental lock, and the DVR facade behind the sheet's Record rows
 * (null in JVM tests that don't exercise recording).
 */
class PlaybackHooks(
    val panelLock: PanelLock = PanelLock(),
    val onOpenSettings: () -> Unit = {},
    val onOpenMultiview: () -> Unit = {},
    val onOpenRecordings: () -> Unit = {},
    val recording: RecordingCenter? = null,
)

/** Everything [PlaybackViewModel] needs injected, bundled for readability. */
class PlaybackEnv(
    val channelDao: ChannelDao,
    val epgRepository: EpgRepository,
    val engine: PlayerEngine,
    val store: KeyValueStore,
    val time: PlaybackTime,
    val hooks: PlaybackHooks = PlaybackHooks(),
)
