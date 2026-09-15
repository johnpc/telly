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
    /** Re-anchors the info-overlay instant at the injected clock's "now". */
    private val refreshInstant: () -> Unit,
    private val exitToGuide: () -> Unit,
    /** Every live tune leaves catch-up mode (zap keys, panel rows, recents). */
    private val onLiveTune: () -> Unit = {},
) {
    fun execute(command: PlaybackCommand) {
        when (command) {
            PlaybackCommand.ShowInfo -> showInfo()
            PlaybackCommand.ShowTransport ->
                overlays.showAutoHiding(PlaybackOverlay.InfoTransport, PlaybackViewModel.INFO_OVERLAY_TIMEOUT_MS)
            PlaybackCommand.ExitToGuide -> exitToGuide()
            is PlaybackCommand.Zap -> zap(command.delta)
            PlaybackCommand.OpenQuickBar ->
                overlays.showAutoHiding(PlaybackOverlay.QuickBar, PlaybackViewModel.QUICK_BAR_TIMEOUT_MS)
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
        onLiveTune()
        refreshInstant()
        overlays.showAutoHiding(PlaybackOverlay.ZapInfo, PlaybackViewModel.ZAP_OVERLAY_TIMEOUT_MS)
    }

    private fun showInfo() {
        refreshInstant()
        overlays.showAutoHiding(PlaybackOverlay.Info, PlaybackViewModel.INFO_OVERLAY_TIMEOUT_MS)
    }

    private fun zap(delta: Int) {
        if (tuner.zap(delta)) showZapInfo()
    }
}
