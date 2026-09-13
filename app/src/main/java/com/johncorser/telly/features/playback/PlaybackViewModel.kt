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
 * covers the video, and how D-pad keys move between them (catalogue §3).
 * A plain class — everything injected, unit-tested on the JVM.
 */
class PlaybackViewModel(
    env: PlaybackEnv,
    scope: CoroutineScope,
    overlayTimeoutMs: Long = INFO_OVERLAY_TIMEOUT_MS,
) {
    private val clock = env.clock

    val panel = PanelViewModel(env.channelDao, env.epgRepository, clock, scope, env.zone)

    private val tuner = TuneController(env.engine, env.store, scope, env.channelDao)
    private val overlays = OverlayState(scope, overlayTimeoutMs)
    private val instant = MutableStateFlow(clock())

    /** Executes context-menu rows; also resolves the channel they act on. */
    val menu = PlaybackMenuHandler(ChannelActions(env.channelDao, scope), overlays, tuner)

    val current: StateFlow<ChannelEntity?> = tuner.current
    val overlay: StateFlow<PlaybackOverlay> = overlays.overlay
    val playerState: StateFlow<PlayerState> = env.engine.state
    val info: StateFlow<PlaybackInfoData?> =
        PlaybackInfoFeed(tuner.current, instant, env.engine.video, env.epgRepository, env.zone, scope).info

    init {
        tuner.start()
    }

    /** Routes a key through the catalogue's key-by-context map; true = consumed. */
    fun onKey(key: PlaybackKey): Boolean {
        val command = PlaybackKeyPolicy.commandFor(overlays.value, key) ?: return false
        execute(command)
        return true
    }

    /** OK on a panel row tunes it and dismisses the panel (catalogue §3). */
    fun tuneFromPanel(channel: ChannelEntity) {
        tuner.tune(channel)
        overlays.set(PlaybackOverlay.None)
    }

    fun showChannelMenu(channel: ChannelEntity) = overlays.set(PlaybackOverlay.ChannelMenu(channel.id))

    fun showComingSoon(feature: String) = overlays.set(PlaybackOverlay.ComingSoon(feature))

    fun onOverlayInteraction() {
        if (overlays.value == PlaybackOverlay.Info) overlays.keepInfoAlive()
    }

    fun close() = tuner.release()

    private fun execute(command: PlaybackCommand) {
        when (command) {
            PlaybackCommand.ShowInfo -> showInfo()
            PlaybackCommand.OpenPanelAtPrevious -> openPanel(offset = -1)
            PlaybackCommand.OpenPanelAtCurrent -> openPanel(offset = 0)
            is PlaybackCommand.Zap -> zap(command.delta)
            PlaybackCommand.OpenMenu -> overlays.set(PlaybackOverlay.Menu)
            PlaybackCommand.Dismiss -> overlays.set(PlaybackOverlay.None)
            PlaybackCommand.BackToPanel -> overlays.set(PlaybackOverlay.Panel)
        }
    }

    private fun showInfo() {
        instant.value = clock()
        overlays.showInfoAutoHiding()
    }

    private fun zap(delta: Int) {
        if (tuner.zap(delta)) showInfo()
    }

    /** The overlay's "TV guide" card opens the panel at the tuned row. */
    fun openPanel(offset: Int = 0) {
        panel.openFocusedOn(tuner.neighbour(offset)?.id)
        overlays.set(PlaybackOverlay.Panel)
    }

    companion object {
        /** TiviMate's default panel timeout (~5 s; premium-tunable, not captured). */
        const val INFO_OVERLAY_TIMEOUT_MS = 5_000L
    }
}
