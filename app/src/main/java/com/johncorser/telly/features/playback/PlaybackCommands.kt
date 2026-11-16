package com.johncorser.telly.features.playback

import com.johncorser.telly.features.panel.PanelViewModel

/** The command executor's outward seams (nav + clock re-seed + catch-up). */
data class PlaybackCommandSeams(
    /** Re-seeds the overlay's "now" instant from the injected clock. */
    val refreshInstant: () -> Unit = {},
    val exitToGuide: () -> Unit = {},
    /** Appearance -> Player -> Panels timeout, sec (default = today's constants). */
    val timeouts: () -> PanelTimeouts = { PanelTimeouts.DEFAULT },
)

/**
 * Executes [PlaybackCommand]s against the tuner and overlay state — the
 * transition half of the playback state machine, split from the ViewModel
 * so each stays small and directly testable. It is the ONE writer of
 * [OverlayState] (docs/state-machines.md; `scripts/check-state-machines.mjs`
 * fails the gate on any other caller): the menu handler, track pickers,
 * recording prompts and PIP all express their overlay moves as commands.
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
            is PlaybackCommand.Show -> overlays.set(command.overlay)
            PlaybackCommand.KeepAlive -> overlays.keepAlive()
        }
    }

    /** The overlay currently up (readers derive from this; writers go through [execute]). */
    val overlay: PlaybackOverlay get() = overlays.value

    /** The quick-bar's Channels list opens the panel at the tuned row. */
    fun openPanel() {
        panel.openFocusedOn(tuner.current.value?.id)
        overlays.set(PlaybackOverlay.Panel)
    }

    /** Zap keeps the old frame on screen; the compact overlay identifies the target. */
    fun showZapInfo() {
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
