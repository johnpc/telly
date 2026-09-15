package com.johncorser.telly.features.playback

import com.johncorser.telly.features.panel.PanelViewModel

/**
 * Executes [PlaybackCommand]s against the tuner and overlay state — the
 * transition half of the playback state machine, split from the ViewModel
 * so each stays small and directly testable.
 */
class PlaybackCommands(
    private val tuner: TuneController,
    private val overlays: OverlayState,
    private val panel: PanelViewModel,
    /** Re-seeds the overlay's "now" instant from the injected clock. */
    private val refreshInstant: () -> Unit,
    private val exitToGuide: () -> Unit,
    /** Appearance -> Player -> Panels timeout, sec (default = today's constants). */
    private val timeouts: () -> PanelTimeouts = { PanelTimeouts.DEFAULT },
) {
    fun execute(command: PlaybackCommand) {
        when (command) {
            PlaybackCommand.ShowInfo -> showInfo()
            PlaybackCommand.ShowTransport ->
                overlays.showAutoHiding(PlaybackOverlay.InfoTransport, timeouts().infoMs)
            PlaybackCommand.ExitToGuide -> exitToGuide()
            is PlaybackCommand.Zap -> zap(command.delta)
            PlaybackCommand.OpenQuickBar ->
                overlays.showAutoHiding(PlaybackOverlay.QuickBar, timeouts().quickBarMs)
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
        refreshInstant()
        overlays.showAutoHiding(PlaybackOverlay.ZapInfo, timeouts().zapMs)
    }

    private fun showInfo() {
        refreshInstant()
        overlays.showAutoHiding(PlaybackOverlay.Info, timeouts().infoMs)
    }

    private fun zap(delta: Int) {
        if (tuner.zap(delta)) showZapInfo()
    }
}
