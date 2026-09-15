package com.johncorser.telly.features.playback

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/** One picker choice of a remote-control keymap option (stored raw = label). */
interface KeymapChoice {
    val label: String
}

/** Parses a persisted picker label; unknown raws fall back to [default]. */
fun <T : KeymapChoice> keymapChoice(
    raw: String,
    options: List<T>,
    default: T,
): T = options.firstOrNull { it.label == raw } ?: default

/** What OK does at bare fullscreen playback (Remote control → Player). */
enum class PlayerOkAction(
    override val label: String,
    internal val command: PlaybackCommand?,
) : KeymapChoice {
    SHOW_INFO("Show info panel", PlaybackCommand.ShowInfo),
    CHANNELS_LIST("Open channels list", PlaybackCommand.OpenPanel),
    NOTHING("Nothing", null),
}

/** What UP/DOWN do at bare fullscreen playback. */
enum class PlayerUpDownAction(
    override val label: String,
) : KeymapChoice {
    SHOW_INFO("Show info panel"),
    SWITCH_CHANNELS("Switch channels"),
    NOTHING("Nothing"),
}

/** What LEFT/RIGHT do at bare fullscreen playback (no-ops by default). */
enum class PlayerLeftRightAction(
    override val label: String,
) : KeymapChoice {
    NOTHING("Nothing"),
    SWITCH_CHANNELS("Switch channels"),
}

/** What a held OK does at bare fullscreen playback (MENU stays quick-menu). */
enum class PlayerLongOkAction(
    override val label: String,
    internal val command: PlaybackCommand,
) : KeymapChoice {
    QUICK_MENU("Open quick menu", PlaybackCommand.OpenQuickBar),
    CHANNELS_LIST("Open channels list", PlaybackCommand.OpenPanel),
}

/** UP zaps forward, DOWN back, mirroring CH+/CH− (null = key does nothing). */
internal fun PlayerUpDownAction.command(delta: Int): PlaybackCommand? =
    when (this) {
        PlayerUpDownAction.SHOW_INFO -> PlaybackCommand.ShowInfo
        PlayerUpDownAction.SWITCH_CHANNELS -> PlaybackCommand.Zap(delta)
        PlayerUpDownAction.NOTHING -> null
    }

/** RIGHT zaps forward, LEFT back (null = key does nothing). */
internal fun PlayerLeftRightAction.command(delta: Int): PlaybackCommand? =
    if (this == PlayerLeftRightAction.SWITCH_CHANNELS) PlaybackCommand.Zap(delta) else null

/**
 * The remappable fullscreen-playback keys (Settings → Remote control →
 * Player). Every default equals the device-verified key map exactly; the
 * remap options only ADD alternatives. Seek options are absent on purpose:
 * telly has no seek plumbing yet, so only remaps the player can honor are
 * offered.
 */
data class PlayerKeymap(
    val ok: PlayerOkAction = PlayerOkAction.SHOW_INFO,
    val upDown: PlayerUpDownAction = PlayerUpDownAction.SHOW_INFO,
    val leftRight: PlayerLeftRightAction = PlayerLeftRightAction.NOTHING,
    val longOk: PlayerLongOkAction = PlayerLongOkAction.QUICK_MENU,
) {
    companion object {
        /** Reads the four persisted picker labels from [settings]. */
        fun from(settings: SettingsRepository): PlayerKeymap =
            PlayerKeymap(
                ok =
                    keymapChoice(
                        settings.get(TellySettings.REMOTE_PLAYER_OK),
                        PlayerOkAction.entries,
                        PlayerOkAction.SHOW_INFO,
                    ),
                upDown =
                    keymapChoice(
                        settings.get(TellySettings.REMOTE_PLAYER_UP_DOWN),
                        PlayerUpDownAction.entries,
                        PlayerUpDownAction.SHOW_INFO,
                    ),
                leftRight =
                    keymapChoice(
                        settings.get(TellySettings.REMOTE_PLAYER_LEFT_RIGHT),
                        PlayerLeftRightAction.entries,
                        PlayerLeftRightAction.NOTHING,
                    ),
                longOk =
                    keymapChoice(
                        settings.get(TellySettings.REMOTE_PLAYER_LONG_OK),
                        PlayerLongOkAction.entries,
                        PlayerLongOkAction.QUICK_MENU,
                    ),
            )
    }
}
