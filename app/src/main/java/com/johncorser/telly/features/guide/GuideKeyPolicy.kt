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
 * level at a time (description/paywall/coming-soon → sheet → grid; the
 * Channel options pane replaced the sheet, so it pops straight to the
 * grid — ref-round6 §A).
 */
object GuideKeyPolicy {
    private const val DAY = 1

    fun commandFor(
        layer: GuideLayer,
        key: GuideKey,
    ): GuideCommand? =
        when (layer) {
            GuideLayer.Grid -> onGrid(key)
            GuideLayer.Groups -> closeOn(key, GuideKey.BACK, GuideKey.RIGHT)
            else -> closeOn(key, GuideKey.BACK)
        }

    private fun onGrid(key: GuideKey): GuideCommand? =
        when (key) {
            GuideKey.LEFT -> GuideCommand.FocusLeft
            GuideKey.RIGHT -> GuideCommand.FocusRight
            GuideKey.UP -> GuideCommand.FocusUp
            GuideKey.DOWN -> GuideCommand.FocusDown
            GuideKey.OK -> GuideCommand.Activate
            GuideKey.LONG_OK, GuideKey.MENU -> GuideCommand.OpenRowMenu
            GuideKey.LONG_LEFT -> GuideCommand.DayJump(-DAY)
            GuideKey.LONG_RIGHT -> GuideCommand.DayJump(DAY)
            GuideKey.BACK -> null
        }

    private fun closeOn(
        key: GuideKey,
        vararg closers: GuideKey,
    ): GuideCommand? = if (key in closers) GuideCommand.CloseLayer else null
}
