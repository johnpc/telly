package com.johncorser.telly.features.playback

import com.johncorser.telly.features.catchup.CatchupPlayback
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.mylist.MyListMenu
import com.johncorser.telly.features.mylist.panelMyListHost
import com.johncorser.telly.features.pip.PipEnterAction
import com.johncorser.telly.features.playback.tracks.TrackPickerController
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
    /** BACK at bare playback + the overlay's TV-guide card both leave here. */
    val exitToGuide: () -> Unit = {},
    /** The overlay's History card pushes the History screen (history-round2 §3). */
    val openHistory: () -> Unit = {},
    /** Quick-bar Search slot + the sheet's Search row both leave here. */
    val openSearch: () -> Unit = {},
) {
    private val clock = env.time.clock
    private val hooks = env.hooks

    /**
     * Opens the multiview grid. The reference reaches it ONLY from the
     * long-OK/MENU quick-bar; telly also surfaces it as an info-overlay
     * card (deliberate deviation — Shield remotes have no MENU button and
     * long-OK is undiscoverable, so the DOWN overlay carries it too).
     */
    val openMultiview: () -> Unit = env.hooks.onOpenMultiview

    /** The shared factory behind the sheet's six group/bulk tool rows. */
    private val groupTools = env.groupTools(scope)

    val panel = playbackPanel(env, groupTools, scope)

    private val tuner = gatedTuner(env, history, scope, external = hooks.platform.external)
    private val overlays = OverlayState(scope)
    private val instant = MutableStateFlow(clock())

    /** My-list toggle state; the sheet labels flip on its keys (mylist). */
    val myList = MyListMenu(env.hooks.myList.store, clock, scope)

    /** Executes context-menu rows; also resolves the channel they act on. */
    val menu: PlaybackMenuHandler =
        PlaybackMenuHandler(
            actions =
                sheetActionsFor(env, scope, panelMyListHost(myList, panel, env.hooks), groupTools) {
                    panel.selectedGroup.value
                },
            overlays = overlays,
            tuner = tuner,
            hooks = hooks.copy(onOpenSearch = openSearch),
            rowOf = { id -> panel.rows.value.firstOrNull { it.channel.id == id } },
            recording = { record.menu },
        )

    /** DVR surface: the sheet's Record rows + the transport record dot. */
    val record: PlaybackRecord =
        PlaybackRecord(env.hooks.recording, tuner, scope, clock, { menu.onRecordingPrompt(it) }, ::showComingSoon)

    /** The blocked-channel tune gate (any path); see [panelBlockPrompt]. */
    val blockPrompt = panelBlockPrompt(tuner, overlays, panel) { commands.showZapInfo() }

    private val video = env.engine.video

    val current: StateFlow<ChannelEntity?> = tuner.current
    val overlay: StateFlow<PlaybackOverlay> = overlays.overlay
    val playerState: StateFlow<PlayerState> = env.engine.state

    /** Catch-up mode: pending guide request, seek keys, pause, programme hops. */
    val catchup: CatchupPlayback = catchupPlayback(env, tuner, { commands.execute(it) }, scope, exitToGuide)

    /** During catch-up the overlay swaps to the archived programme + position. */
    val info: StateFlow<PlaybackInfoData?> = catchupInfoFeed(env, tuner, instant, catchup, scope)

    private val commands =
        PlaybackCommands(
            tuner = tuner,
            overlays = overlays,
            panel = panel,
            seams = commandSeams(env, { instant.value = clock() }, exitToGuide, catchup),
        )

    init {
        // The guide's pending catch-up request owns the tune when present.
        if (!catchup.resumePending()) tuner.start()
        // AFR: the engine's detected/estimated frame rate feeds the display
        // mode switch through the hook (MainActivity applies it).
        feedFrameRates(video, scope, hooks.platform.onFrameRateChanged)
    }

    /** Quick-bar PIP: clear the chrome first, then the activity swaps windows. */
    val enterPip = PipEnterAction(clearChrome = { overlays.set(PlaybackOverlay.None) }, enter = env.hooks.onEnterPip)

    /** The info-row recent-channel cards + Clear (history-round2 §1). */
    val recents =
        PlaybackRecents(
            feed = recentRowFeed(env, tuner, history, instant, scope),
            history = history,
            scope = scope,
            tuneAndZap = { card -> tuneFromPanel(card.channel) },
            keepAlive = overlays::keepAlive,
        )

    /**
     * Resume after a background stop leaves fullscreen for the guide, like the
     * reference (round7 P2). PIP-aware: while the PIP window is up the video
     * keeps playing, so a STOP there must not kill the stream; closing the
     * window flips PIP off before its stop lands, which then passes normally.
     */
    val lifecycle =
        PlaybackLifecycle(tuner, recover = exitToGuide, shouldStop = env.hooks.pip::allowsBackgroundStop)

    private val playerKeymap = env.hooks.playerKeymap

    /**
     * Routes a key through the catalogue's key-by-context map; true =
     * consumed. The catch-up context pre-routes seek keys (delegation, not
     * a fork — everything it declines falls through to the live keymap).
     */
    fun onKey(key: PlaybackKey): Boolean {
        // Catch-up seeks first, then a pane dialog's BACK, then the key map.
        if (catchup.keys.onKey(overlays.value, key) || menu.interceptBack(overlays.value, key)) return true
        return PlaybackKeyPolicy.commandFor(overlays.value, key, playerKeymap())?.also(commands::execute) != null
    }

    /** OK on a panel row tunes it and shows the compact zap overlay (round3-ref 10). */
    fun tuneFromPanel(channel: ChannelEntity) {
        tuner.tune(channel)
        // A blocked channel prompts for the PIN instead; the overlay follows
        // a verified PIN (blockPrompt.submit), never a cancelled one.
        if (blockPrompt.channel.value == null) commands.showZapInfo()
    }

    fun showChannelMenu(channel: ChannelEntity) = menu.openChannelMenu(channel.id)

    fun showComingSoon(feature: String) = overlays.set(PlaybackOverlay.ComingSoon(feature))

    /** The quick-bar's Recordings slot opens the DVR library route. */
    val openRecordings: () -> Unit = env.hooks.onOpenRecordings

    /** Quick-bar track pickers: video / audio / audio-sync / CC dialogs (ux-spec §3.14). */
    val trackPickers = TrackPickerController(env.engine.tracks, overlays)

    /** The nine quick-bar slots with live stream labels (round3-ref 07). */
    fun quickBarItems(): List<QuickBarItem> =
        QuickBar.items(video.value, trackPickers.syncLabel(), trackPickers.subtitleLabel())

    fun onOverlayInteraction() = overlays.keepAlive()

    // The screen releases its engine lease; the engine itself is shared
    // with the guide preview and must survive the route hand-over.
    fun close() = hooks.platform.onPlaybackStopped()

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
