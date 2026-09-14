package com.johncorser.telly.features.playback

import com.johncorser.telly.core.kv.KeyValueStore
import com.johncorser.telly.features.epg.EpgRepository
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.player.PlayerEngine
import com.johncorser.telly.features.player.PlayerState
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.TimeZone

/** Everything [PlaybackViewModel] needs injected, bundled for readability. */
class PlaybackEnv(
    val channelDao: ChannelDao,
    val epgRepository: EpgRepository,
    val engine: PlayerEngine,
    val store: KeyValueStore,
    val clock: () -> Long,
    val zone: TimeZone = TimeZone.getDefault(),
)

/**
 * Fullscreen-playback state machine: which channel is tuned, which overlay
 * covers the video, and how D-pad keys move between them (catalogue §3 as
 * corrected by the round3 device verification).
 * A plain class — everything injected, unit-tested on the JVM.
 */
class PlaybackViewModel(
    env: PlaybackEnv,
    scope: CoroutineScope,
    private val openSearch: () -> Unit = {},
) {
    private val clock = env.clock

    val panel = PanelViewModel(env.channelDao, env.epgRepository, clock, scope, env.zone)

    private val tuner = TuneController(env.engine, env.store, scope, env.channelDao)
    private val overlays = OverlayState(scope)
    private val instant = MutableStateFlow(clock())

    /** Executes context-menu rows; also resolves the channel they act on. */
    val menu = PlaybackMenuHandler(ChannelActions(env.channelDao, scope), overlays, tuner)

    private val video = env.engine.video

    val current: StateFlow<ChannelEntity?> = tuner.current
    val overlay: StateFlow<PlaybackOverlay> = overlays.overlay
    val playerState: StateFlow<PlayerState> = env.engine.state
    val info: StateFlow<PlaybackInfoData?> =
        PlaybackInfoFeed(tuner.current, instant, env.engine.video, env.epgRepository, env.zone, scope).info

    init {
        tuner.start()
    }

    private val commands = PlaybackCommands(tuner, overlays, panel, instant, clock)

    /** Routes a key through the catalogue's key-by-context map; true = consumed. */
    fun onKey(key: PlaybackKey): Boolean {
        val command = PlaybackKeyPolicy.commandFor(overlays.value, key) ?: return false
        commands.execute(command)
        return true
    }

    /** OK on a panel row tunes it and shows the compact zap overlay (round3-ref 10). */
    fun tuneFromPanel(channel: ChannelEntity) {
        tuner.tune(channel)
        commands.showZapInfo()
    }

    fun showChannelMenu(channel: ChannelEntity) = overlays.set(PlaybackOverlay.ChannelMenu(channel.id))

    fun showComingSoon(feature: String) = overlays.set(PlaybackOverlay.ComingSoon(feature))

    /** Quick-bar OK: Search and Channels list are real, the rest later slices. */
    fun onQuickBarItem(action: QuickBarAction) {
        when (action) {
            QuickBarAction.CHANNELS_LIST -> openPanel()
            QuickBarAction.SEARCH -> openSearch()
            else -> showComingSoon(action.feature)
        }
    }

    /** The nine quick-bar slots with live stream labels (round3-ref 07). */
    fun quickBarItems(): List<QuickBarItem> = QuickBar.items(video.value)

    fun onOverlayInteraction() = overlays.keepAlive()

    fun close() = tuner.release()

    /** The overlay's "TV guide" card opens the panel at the tuned row. */
    fun openPanel() = commands.openPanel()

    companion object {
        /**
         * Tuned so the overlay is VISIBLE ~5.13 s like TiviMate's
         * (tm-ov4.webm): the ~250 ms key-to-first-frame latency plus the
         * 350 ms fade-in mean the hide must start a bit after TiviMate's
         * ≈5.1 s-after-keypress mark (measured in round4/ty4-ov.webm).
         */
        const val INFO_OVERLAY_TIMEOUT_MS = 5_350L

        /** The compact zap overlay stays ~5.5 s after the zap (tm-zap.webm). */
        const val ZAP_OVERLAY_TIMEOUT_MS = 5_500L

        /** The quick-bar auto-hides ~5 s after opening (round3-ref 07). */
        const val QUICK_BAR_TIMEOUT_MS = 5_000L
    }
}
