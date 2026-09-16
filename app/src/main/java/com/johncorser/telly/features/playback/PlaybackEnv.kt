package com.johncorser.telly.features.playback

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.groups.CustomGroupStore
import com.johncorser.telly.features.groups.InMemoryCustomGroupStore
import com.johncorser.telly.features.panel.PanelLock
import com.johncorser.telly.features.player.PlayerEngine
import com.johncorser.telly.features.playlist.db.ChannelDao

/** Cross-slice hooks the playback surface plugs into (nav + parental + groups). */
class PlaybackHooks(
    val panelLock: PanelLock = PanelLock(),
    val onOpenSettings: () -> Unit = {},
    val onOpenMultiview: () -> Unit = {},
    val parental: ParentalControls? = null,
    val customGroups: CustomGroupStore = InMemoryCustomGroupStore(),
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
