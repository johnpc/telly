package com.johncorser.telly.features.playback

import com.johncorser.telly.features.panel.PanelViewModel

/** The command executor's outward seams (nav + clock re-seed + catch-up). */
data class PlaybackCommandSeams(
    /** Re-seeds the overlay's "now" instant from the injected clock. */
    val refreshInstant: () -> Unit = {},
    val exitToGuide: () -> Unit = {},
    /** Appearance -> Player -> Panels timeout, sec (default = today's constants). */
    val timeouts: () -> PanelTimeouts = { PanelTimeouts.DEFAULT },
    /** Every live tune leaves catch-up mode (zap keys, panel rows, recents). */
    val onLiveTune: () -> Unit = {},
)

/**
 * Executes [PlaybackCommand]s against the tuner and overlay state — the
 * transition half of the playback state machine, split from the ViewModel
 * so each stays small and directly testable.
 */
class PlaybackCommands(
    private val tuner: TuneController,
    private val overlays: OverlayState,
    private val panel: PanelViewModel,
    private val seams: PlaybackCommandSeams = PlaybackCommandSeams(),
) {
    fun execute(command: PlaybackCommand) {
        when (command) {
            PlaybackCommand.ShowInfo -> showInfo()
            PlaybackCommand.ShowTransport ->
                overlays.showAutoHiding(PlaybackOverlay.InfoTransport, seams.timeouts().infoMs)
            // Paused catch-up: sticky transport (the VOD pin-while-paused idiom).
            PlaybackCommand.PinTransport -> overlays.set(PlaybackOverlay.InfoTransport)
            PlaybackCommand.ExitToGuide -> seams.exitToGuide()
            is PlaybackCommand.Zap -> zap(command.delta)
            PlaybackCommand.OpenQuickBar ->
                overlays.showAutoHiding(PlaybackOverlay.QuickBar, seams.timeouts().quickBarMs)
            PlaybackCommand.OpenPanel -> openPanel()
            PlaybackCommand.Dismiss -> overlays.set(PlaybackOverlay.None)
            PlaybackCommand.BackToPanel -> overlays.set(PlaybackOverlay.Panel)
            is PlaybackCommand.PopTo -> overlays.set(command.overlay)
        }
    }

    /** The quick-bar's Channels list opens the panel at the tuned row. */
    fun openPanel() {
        panel.openFocusedOn(tuner.current.value?.id)
        overlays.set(PlaybackOverlay.Panel)
    }

    /** Zap keeps the old frame on screen; the compact overlay identifies the target. */
    fun showZapInfo() {
        seams.onLiveTune()
        seams.refreshInstant()
        overlays.showAutoHiding(PlaybackOverlay.ZapInfo, seams.timeouts().zapMs)
    }

    private fun showInfo() {
        seams.refreshInstant()
        overlays.showAutoHiding(PlaybackOverlay.Info, seams.timeouts().infoMs)
    }

    private fun zap(delta: Int) {
        if (tuner.zap(delta)) showZapInfo()
    }
}
