package com.johncorser.telly.features.guide

/** What a key press should do given the active guide layer. */
sealed interface GuideCommand {
    data object FocusLeft : GuideCommand

    data object FocusRight : GuideCommand

    data object FocusUp : GuideCommand

    data object FocusDown : GuideCommand

    data object Activate : GuideCommand

    data class DayJump(
        val days: Int,
    ) : GuideCommand

    /** LEFT/RIGHT in the "Move by page" remap: pan a whole viewport. */
    data class PageJump(
        val direction: Int,
    ) : GuideCommand

    /** CH+/CH− in the "Page the channel list" remap: a screenful of rows. */
    data class PageRows(
        val direction: Int,
    ) : GuideCommand

    data object OpenRowMenu : GuideCommand

    data object CloseLayer : GuideCommand
}

/**
 * Key-by-layer map for the guide (catalogue §2): D-pad moves the grid
 * focus, OK activates the focused cell, long-OK / MENU open the row
 * context sheet (catalogue §3 38-40 + round3-ref 05), long-LEFT/RIGHT
 * jump a day (the hint toast's "navigate to past programs"). BACK on the
 * grid is unconsumed on purpose: at guide root TiviMate free exits the
 * app with no confirmation (device-verified BACK chain). Overlaid layers
 * own their own focus; the policy only closes them (RIGHT also leaves
 * the groups column back to the grid, capture 25) and the close pops one
 * level at a time (description/coming-soon → sheet → grid; the Channel
 * options pane replaced the sheet, so it pops straight to the grid —
 * ref-round6 §A).
 */
object GuideKeyPolicy {
    private const val DAY = 1

    fun commandFor(
        layer: GuideLayer,
        key: GuideKey,
        keymap: GuideKeymap = GuideKeymap(),
    ): GuideCommand? =
        when (layer) {
            GuideLayer.Grid -> onGrid(key, keymap)
            GuideLayer.Groups -> closeOn(key, GuideKey.BACK, GuideKey.RIGHT)
            else -> closeOn(key, GuideKey.BACK)
        }

    private fun onGrid(
        key: GuideKey,
        keymap: GuideKeymap,
    ): GuideCommand? =
        when (key) {
            GuideKey.LEFT -> horizontal(keymap.leftRight, GuideCommand.FocusLeft, -1)
            GuideKey.RIGHT -> horizontal(keymap.leftRight, GuideCommand.FocusRight, +1)
            GuideKey.UP -> GuideCommand.FocusUp
            GuideKey.DOWN -> GuideCommand.FocusDown
            GuideKey.OK -> GuideCommand.Activate
            GuideKey.LONG_OK -> longOk(keymap.longOk)
            GuideKey.MENU -> GuideCommand.OpenRowMenu
            GuideKey.LONG_LEFT -> GuideCommand.DayJump(-DAY)
            GuideKey.LONG_RIGHT -> GuideCommand.DayJump(DAY)
            GuideKey.CHANNEL_UP -> channelKeys(keymap.channelUpDown, up = true)
            GuideKey.CHANNEL_DOWN -> channelKeys(keymap.channelUpDown, up = false)
            GuideKey.BACK -> null
        }

    private fun horizontal(
        action: GuideLeftRightAction,
        focusMove: GuideCommand,
        direction: Int,
    ): GuideCommand = if (action == GuideLeftRightAction.BY_PAGE) GuideCommand.PageJump(direction) else focusMove

    private fun longOk(action: GuideLongOkAction): GuideCommand =
        if (action == GuideLongOkAction.PLAY_CHANNEL) GuideCommand.Activate else GuideCommand.OpenRowMenu

    /** CH+ pages up the list / jumps a day forward; CH− mirrors it. */
    private fun channelKeys(
        action: GuideChannelKeysAction,
        up: Boolean,
    ): GuideCommand? =
        when (action) {
            GuideChannelKeysAction.NOTHING -> null
            GuideChannelKeysAction.PAGE_CHANNELS -> GuideCommand.PageRows(if (up) -1 else +1)
            GuideChannelKeysAction.MOVE_BY_DAY -> GuideCommand.DayJump(if (up) DAY else -DAY)
        }

    private fun closeOn(
        key: GuideKey,
        vararg closers: GuideKey,
    ): GuideCommand? = if (key in closers) GuideCommand.CloseLayer else null
}
