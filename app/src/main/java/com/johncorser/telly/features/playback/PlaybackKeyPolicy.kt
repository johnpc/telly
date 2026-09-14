package com.johncorser.telly.features.playback

/** What a key press should do given the active overlay. */
sealed interface PlaybackCommand {
    data object ShowInfo : PlaybackCommand

    data object OpenPanelAtPrevious : PlaybackCommand

    data object ExitToGuide : PlaybackCommand

    data class Zap(
        val delta: Int,
    ) : PlaybackCommand

    data object OpenMenu : PlaybackCommand

    data object Dismiss : PlaybackCommand

    data object BackToPanel : PlaybackCommand
}

/**
 * Key-by-context map cloned from the TiviMate 5.2.0 capture catalogue (§3):
 * OK/DOWN open the info overlay, UP opens the channel panel focused on the
 * previous channel (wraps), long-OK/MENU open the context menu, LEFT/RIGHT
 * do nothing at bare playback (device-verified). CH+/CH- zap directly (the
 * catalogue flags its no-zap observation as an emulator artifact). BACK at
 * bare playback returns to the TV guide (device-verified BACK chain:
 * playback → guide → app exit).
 */
object PlaybackKeyPolicy {
    fun commandFor(
        overlay: PlaybackOverlay,
        key: PlaybackKey,
    ): PlaybackCommand? =
        when (overlay) {
            PlaybackOverlay.None -> atBarePlayback(key)
            PlaybackOverlay.Info -> withinInfoOverlay(key)
            is PlaybackOverlay.ChannelMenu -> dismissalOnly(key, PlaybackCommand.BackToPanel)
            else -> dismissalOnly(key, PlaybackCommand.Dismiss)
        }

    private fun atBarePlayback(key: PlaybackKey): PlaybackCommand? =
        when (key) {
            PlaybackKey.OK, PlaybackKey.DOWN -> PlaybackCommand.ShowInfo
            PlaybackKey.UP -> PlaybackCommand.OpenPanelAtPrevious
            PlaybackKey.CHANNEL_UP -> PlaybackCommand.Zap(+1)
            PlaybackKey.CHANNEL_DOWN -> PlaybackCommand.Zap(-1)
            PlaybackKey.LONG_OK, PlaybackKey.MENU -> PlaybackCommand.OpenMenu
            PlaybackKey.BACK -> PlaybackCommand.ExitToGuide
            PlaybackKey.LEFT, PlaybackKey.RIGHT -> null
        }

    private fun withinInfoOverlay(key: PlaybackKey): PlaybackCommand? =
        when (key) {
            PlaybackKey.BACK -> PlaybackCommand.Dismiss
            PlaybackKey.LONG_OK, PlaybackKey.MENU -> PlaybackCommand.OpenMenu
            PlaybackKey.CHANNEL_UP -> PlaybackCommand.Zap(+1)
            PlaybackKey.CHANNEL_DOWN -> PlaybackCommand.Zap(-1)
            else -> null
        }

    private fun dismissalOnly(
        key: PlaybackKey,
        onBack: PlaybackCommand,
    ): PlaybackCommand? = if (key == PlaybackKey.BACK) onBack else null
}
