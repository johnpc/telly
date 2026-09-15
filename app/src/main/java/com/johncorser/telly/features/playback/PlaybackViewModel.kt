package com.johncorser.telly.features.playback

import com.johncorser.telly.features.catchup.CatchupInfo
import com.johncorser.telly.features.catchup.CatchupPlayback
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.player.PlayerState
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fullscreen-playback state machine: which channel is tuned, which overlay
 * covers the video, and how D-pad keys move between them (catalogue §3 as
 * corrected by the round3 device verification).
 * A plain class — everything injected, unit-tested on the JVM.
 */
class PlaybackViewModel(
    env: PlaybackEnv,
    history: WatchHistory,
    scope: CoroutineScope,
    onExitToGuide: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    private val openSearch: () -> Unit = {},
) {
    private val clock = env.time.clock

    /**
     * Opens the multiview grid. The reference reaches it ONLY from the
     * long-OK/MENU quick-bar; telly also surfaces it as an info-overlay
     * card (deliberate deviation — Shield remotes have no MENU button and
     * long-OK is undiscoverable, so the DOWN overlay carries it too).
     */
    val openMultiview: () -> Unit = env.hooks.onOpenMultiview

    val panel = PanelViewModel(env.channelDao, env.epgRepository, clock, scope, env.time.zone, env.hooks.panelLock)

    private val tuner = TuneController(env.engine, env.store, scope, env.channelDao, history)
    private val overlays = OverlayState(scope)
    private val instant = MutableStateFlow(clock())

    /** Executes context-menu rows; also resolves the channel they act on. */
    val menu =
        PlaybackMenuHandler(
            actions = ChannelActions(env.channelDao, scope),
            overlays = overlays,
            tuner = tuner,
            openSettings = env.hooks.onOpenSettings,
            openSearch = openSearch,
            rowOf = { id -> panel.rows.value.firstOrNull { it.channel.id == id } },
        )

    private val video = env.engine.video

    val current: StateFlow<ChannelEntity?> = tuner.current
    val overlay: StateFlow<PlaybackOverlay> = overlays.overlay
    val playerState: StateFlow<PlayerState> = env.engine.state

    /** Catch-up mode: pending guide request, seek keys, transport position. */
    val catchup: CatchupPlayback =
        CatchupPlayback(env, tuner, { commands.execute(PlaybackCommand.ShowTransport) }, scope, onExitToGuide)

    private val liveInfo =
        PlaybackInfoFeed(tuner.current, instant, env.engine.video, env.epgRepository, env.time.zone, scope).info

    /** During catch-up the overlay swaps to the archived programme + position. */
    val info: StateFlow<PlaybackInfoData?> =
        CatchupInfo.merged(liveInfo, catchup.state, catchup.position, env.time.zone, scope)

    private val commands: PlaybackCommands =
        PlaybackCommands(tuner, overlays, panel, { instant.value = clock() }, onExitToGuide, catchup::onLiveTune)

    init {
        if (!catchup.resumePending()) tuner.start()
    }

    /** The info-row recent-channel cards + Clear (history-round2 §1). */
    val recents =
        PlaybackRecents(
            feed = recentRowFeed(env, tuner, history, instant, scope),
            history = history,
            scope = scope,
            tuneAndZap = { card -> tuneFromPanel(card.channel) },
            keepAlive = overlays::keepAlive,
        )

    /** Resume after a background stop leaves fullscreen for the guide, like the reference (round7 P2). */
    val lifecycle = PlaybackLifecycle(tuner, recover = onExitToGuide)

    /** Routes a key through the catalogue's key-by-context map; true = consumed. */
    fun onKey(key: PlaybackKey): Boolean {
        if (catchup.onKey(overlays.value, key)) return true
        val command = PlaybackKeyPolicy.commandFor(overlays.value, key) ?: return false
        commands.execute(command)
        return true
    }

    /** OK on a panel row tunes it and shows the compact zap overlay (round3-ref 10). */
    fun tuneFromPanel(channel: ChannelEntity) {
        tuner.tune(channel)
        commands.showZapInfo()
    }

    fun showChannelMenu(channel: ChannelEntity) = menu.openChannelMenu(channel.id)

    fun showComingSoon(feature: String) = overlays.set(PlaybackOverlay.ComingSoon(feature))

    /** Quick-bar OK: Search, Channels list and Multiview are real, the rest later slices. */
    fun onQuickBarItem(action: QuickBarAction) {
        when (action) {
            QuickBarAction.CHANNELS_LIST -> openPanel()
            QuickBarAction.SEARCH -> openSearch()
            QuickBarAction.MULTIVIEW -> openMultiview()
            else -> showComingSoon(action.feature)
        }
    }

    /** The nine quick-bar slots with live stream labels (round3-ref 07). */
    fun quickBarItems(): List<QuickBarItem> = QuickBar.items(video.value)

    fun onOverlayInteraction() = overlays.keepAlive()

    fun close() = tuner.release()

    /** BACK at bare playback + the overlay's TV-guide card both leave here. */
    val exitToGuide: () -> Unit = onExitToGuide

    /** The overlay's History card pushes the History screen (history-round2 §3). */
    val openHistory: () -> Unit = onOpenHistory

    /** The quick-bar's Channels list opens the panel at the tuned row. */
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
