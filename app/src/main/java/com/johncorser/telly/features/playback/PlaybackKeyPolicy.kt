package com.johncorser.telly.features.playback

/** What a key press should do given the active overlay. */
sealed interface PlaybackCommand {
    data object ShowInfo : PlaybackCommand

    data object ShowTransport : PlaybackCommand

    data object OpenPanelAtCurrent : PlaybackCommand

    data class Zap(
        val delta: Int,
    ) : PlaybackCommand

    data object OpenQuickBar : PlaybackCommand

    data object Dismiss : PlaybackCommand

    data object BackToPanel : PlaybackCommand
}

/**
 * Key-by-context map, device-verified round3 (round3-ref 03/07/08 corrected
 * the catalogue §3): OK/DOWN **and UP** open the info overlay; a second UP
 * expands the transport row; long-OK/MENU open the bottom icon quick-bar;
 * LEFT/RIGHT do nothing at bare playback. CH+/CH- zap directly (the
 * catalogue flags its no-zap observation as an emulator artifact). BACK at
 * bare playback stands in for "return to the TV guide" by opening the panel
 * until the guide slice exists.
 */
object PlaybackKeyPolicy {
    fun commandFor(
        overlay: PlaybackOverlay,
        key: PlaybackKey,
    ): PlaybackCommand? =
        when (overlay) {
            PlaybackOverlay.None -> atBarePlayback(key)
            PlaybackOverlay.Info -> withinInfoOverlay(key, onUp = PlaybackCommand.ShowTransport)
            PlaybackOverlay.InfoTransport -> withinInfoOverlay(key, onUp = null)
            PlaybackOverlay.ZapInfo -> withinZapOverlay(key)
            is PlaybackOverlay.ChannelMenu -> dismissalOnly(key, PlaybackCommand.BackToPanel)
            else -> dismissalOnly(key, PlaybackCommand.Dismiss)
        }

    private fun atBarePlayback(key: PlaybackKey): PlaybackCommand? =
        when (key) {
            PlaybackKey.OK, PlaybackKey.DOWN, PlaybackKey.UP -> PlaybackCommand.ShowInfo
            PlaybackKey.CHANNEL_UP -> PlaybackCommand.Zap(+1)
            PlaybackKey.CHANNEL_DOWN -> PlaybackCommand.Zap(-1)
            PlaybackKey.LONG_OK, PlaybackKey.MENU -> PlaybackCommand.OpenQuickBar
            PlaybackKey.BACK -> PlaybackCommand.OpenPanelAtCurrent
            PlaybackKey.LEFT, PlaybackKey.RIGHT -> null
        }

    private fun withinInfoOverlay(
        key: PlaybackKey,
        onUp: PlaybackCommand?,
    ): PlaybackCommand? =
        when (key) {
            PlaybackKey.BACK -> PlaybackCommand.Dismiss
            PlaybackKey.UP -> onUp
            PlaybackKey.LONG_OK, PlaybackKey.MENU -> PlaybackCommand.OpenQuickBar
            PlaybackKey.CHANNEL_UP -> PlaybackCommand.Zap(+1)
            PlaybackKey.CHANNEL_DOWN -> PlaybackCommand.Zap(-1)
            else -> null
        }

    /** Any info key promotes the zap overlay to the full info overlay. */
    private fun withinZapOverlay(key: PlaybackKey): PlaybackCommand? =
        when (key) {
            PlaybackKey.OK, PlaybackKey.DOWN, PlaybackKey.UP -> PlaybackCommand.ShowInfo
            else -> withinInfoOverlay(key, onUp = null)
        }

    private fun dismissalOnly(
        key: PlaybackKey,
        onBack: PlaybackCommand,
    ): PlaybackCommand? = if (key == PlaybackKey.BACK) onBack else null
}
