package com.johncorser.telly.features.playback

/** What a key press should do given the active overlay. */
sealed interface PlaybackCommand {
    data object ShowInfo : PlaybackCommand

    data object ShowTransport : PlaybackCommand

    /** Catch-up pause: the transport overlay stays pinned (no auto-hide). */
    data object PinTransport : PlaybackCommand

    data object ExitToGuide : PlaybackCommand

    data class Zap(
        val delta: Int,
    ) : PlaybackCommand

    data object OpenQuickBar : PlaybackCommand

    data object Dismiss : PlaybackCommand

    data object BackToPanel : PlaybackCommand

    /** One-level BACK from a sheet-pushed screen (coming-soon, description, …). */
    data class PopTo(
        val overlay: PlaybackOverlay,
    ) : PlaybackCommand

    /** Open the channel panel (the "Open channels list" key remap). */
    data object OpenPanel : PlaybackCommand
}

/**
 * Key-by-context map, device-verified round3 (round3-ref 03/07/08 corrected
 * the catalogue §3): OK/DOWN **and UP** open the info overlay; a second UP
 * expands the transport row; long-OK/MENU open the bottom icon quick-bar;
 * LEFT/RIGHT do nothing at bare playback. CH+/CH- zap directly (the
 * catalogue flags its no-zap observation as an emulator artifact). BACK at
 * bare playback returns to the TV guide (device-verified BACK chain:
 * playback → guide → app exit).
 *
 * The Remote control → Player settings remap the bare-playback keys through
 * [PlayerKeymap]; its defaults reproduce this map exactly.
 */
object PlaybackKeyPolicy {
    fun commandFor(
        overlay: PlaybackOverlay,
        key: PlaybackKey,
        keymap: PlayerKeymap = PlayerKeymap(),
    ): PlaybackCommand? =
        when (overlay) {
            PlaybackOverlay.None -> atBarePlayback(key, keymap)
            PlaybackOverlay.Info -> withinInfoOverlay(key, onUp = PlaybackCommand.ShowTransport)
            PlaybackOverlay.InfoTransport -> withinInfoOverlay(key, onUp = null)
            PlaybackOverlay.ZapInfo -> withinZapOverlay(key, keymap)
            is PlaybackOverlay.ChannelMenu -> dismissalOnly(key, PlaybackCommand.BackToPanel)
            is PlaybackOverlay.Pushed -> dismissalOnly(key, PlaybackCommand.PopTo(overlay.back))
            else -> dismissalOnly(key, PlaybackCommand.Dismiss)
        }

    private fun atBarePlayback(
        key: PlaybackKey,
        keymap: PlayerKeymap,
    ): PlaybackCommand? =
        when (key) {
            PlaybackKey.OK -> keymap.ok.command
            PlaybackKey.UP -> keymap.upDown.command(+1)
            PlaybackKey.DOWN -> keymap.upDown.command(-1)
            PlaybackKey.LEFT -> keymap.leftRight.command(-1)
            PlaybackKey.RIGHT -> keymap.leftRight.command(+1)
            PlaybackKey.CHANNEL_UP -> PlaybackCommand.Zap(+1)
            PlaybackKey.CHANNEL_DOWN -> PlaybackCommand.Zap(-1)
            PlaybackKey.LONG_OK -> keymap.longOk.command
            PlaybackKey.MENU -> PlaybackCommand.OpenQuickBar
            PlaybackKey.BACK -> PlaybackCommand.ExitToGuide
            // RW/FF only act through the catch-up context (CatchupKeyPolicy).
            PlaybackKey.REWIND, PlaybackKey.FAST_FORWARD -> null
        }

    private fun withinInfoOverlay(
        key: PlaybackKey,
        onUp: PlaybackCommand?,
    ): PlaybackCommand? =
        when (key) {
            PlaybackKey.BACK -> PlaybackCommand.Dismiss
            PlaybackKey.UP -> onUp
            // The shortcut-card row's down chevron promises "more below": DOWN
            // opens the channel list panel (ux-spec §2.3 "expands to … full
            // channel list"). The reference shows the current channel's
            // now/next programme browser; telly's panel is a superset — the
            // full list plus the focused channel's schedule in its detail card.
            PlaybackKey.DOWN -> PlaybackCommand.OpenPanel
            PlaybackKey.LONG_OK, PlaybackKey.MENU -> PlaybackCommand.OpenQuickBar
            PlaybackKey.CHANNEL_UP -> PlaybackCommand.Zap(+1)
            PlaybackKey.CHANNEL_DOWN -> PlaybackCommand.Zap(-1)
            else -> null
        }

    /**
     * Any info key promotes the zap overlay to the full info overlay; a key
     * remapped to zap/panel keeps its bare-playback meaning so repeated
     * presses keep switching through the transient overlay.
     */
    private fun withinZapOverlay(
        key: PlaybackKey,
        keymap: PlayerKeymap,
    ): PlaybackCommand? =
        when (key) {
            PlaybackKey.OK -> keymap.ok.command ?: PlaybackCommand.ShowInfo
            PlaybackKey.UP -> keymap.upDown.command(+1) ?: PlaybackCommand.ShowInfo
            PlaybackKey.DOWN -> keymap.upDown.command(-1) ?: PlaybackCommand.ShowInfo
            PlaybackKey.LEFT -> keymap.leftRight.command(-1)
            PlaybackKey.RIGHT -> keymap.leftRight.command(+1)
            PlaybackKey.LONG_OK -> keymap.longOk.command
            else -> withinInfoOverlay(key, onUp = null)
        }

    private fun dismissalOnly(
        key: PlaybackKey,
        onBack: PlaybackCommand,
    ): PlaybackCommand? = if (key == PlaybackKey.BACK) onBack else null
}
