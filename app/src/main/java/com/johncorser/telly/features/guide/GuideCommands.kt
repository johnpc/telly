package com.johncorser.telly.features.guide

/**
 * Executes [GuideCommand]s against the focus engine and layer controller —
 * the transition half of the guide state machine, split from
 * [GuideController] so each stays small (the PlaybackCommands precedent).
 */
class GuideCommands(
    private val focusEngine: GuideFocusEngine,
    private val menu: GuideMenuController,
    private val rows: () -> List<GuideRow>,
    /** EPG -> Past days to keep EPG: clamps the long-press day jumps. */
    private val pastDays: () -> Int,
    /** Appearance -> TV guide -> Number of visible channels (CH± page size). */
    private val visibleRows: () -> Int,
    /** OK on the focused cell: play the channel / play a catch-up archive. */
    private val activate: () -> Unit,
) {
    fun execute(command: GuideCommand) {
        when (command) {
            GuideCommand.FocusLeft -> if (!focusEngine.moveLeft(rows())) menu.show(GuideLayer.Groups)
            GuideCommand.FocusRight -> focusEngine.moveRight(rows())
            GuideCommand.FocusUp -> focusEngine.moveVertical(rows(), -1)
            GuideCommand.FocusDown -> focusEngine.moveVertical(rows(), +1)
            is GuideCommand.DayJump -> focusEngine.dayJump(command.days, pastDays())
            is GuideCommand.PageJump -> pageJump(command.direction)
            is GuideCommand.PageRows -> focusEngine.moveVertical(rows(), command.direction * visibleRows())
            GuideCommand.Activate -> activate()
            GuideCommand.OpenRowMenu -> menu.openRowMenu()
            GuideCommand.OpenCellMenu -> menu.openCellMenu(focusEngine.focus.value?.cell)
            GuideCommand.OpenGroups -> menu.show(GuideLayer.Groups)
            GuideCommand.CloseLayer -> menu.close()
        }
    }

    /** Page-left at the live edge opens the groups column like a plain LEFT. */
    private fun pageJump(direction: Int) {
        if (!focusEngine.pageJump(rows(), direction) && direction < 0) menu.show(GuideLayer.Groups)
    }
}
