package com.johncorser.telly.features.guide

import com.johncorser.telly.features.mylist.MyListProgramme

/**
 * The long-OK cell dropdown's row dispatch (capture 27), split off
 * [GuideMenuController] to keep it under the file-length budget. Play channel
 * tunes + goes fullscreen (controller seam), Remind toggles a reminder,
 * "Add to My list" toggles the cell's programme and the Record rows act
 * through the DVR menu; every other dropdown row is unbuilt → coming-soon.
 */
internal fun GuideMenuController.onCellAction(action: GuideCellAction) {
    when (action) {
        GuideCellAction.PLAY_CHANNEL -> playFocusedChannel()
        GuideCellAction.REMIND -> if (remind.toggle()) reset() else comingSoon(action)
        GuideCellAction.ADD_TO_MY_LIST -> addFocusedToMyList(action)
        GuideCellAction.RECORD -> recordingActions.recordCell(layer.value)
        GuideCellAction.CUSTOM_RECORDING -> recordingActions.customRecording()
        else -> comingSoon(action)
    }
}

/** "Play channel": tune the focused row's channel and go fullscreen. */
private fun GuideMenuController.playFocusedChannel() {
    val channel = focusedRow()?.channel
    reset()
    channel?.let(onPlayChannel)
}

/** "Add to My list": toggle the focused cell's programme, else coming-soon. */
private fun GuideMenuController.addFocusedToMyList(action: GuideCellAction) {
    val programme = (layer.value as? GuideLayer.CellMenu)?.cell?.program?.let(MyListProgramme::of)
    val row = focusedRow()
    if (programme != null && row != null) {
        channelActions.myList?.menu?.toggle(row.channel, programme)
        reset()
    } else {
        comingSoon(action)
    }
}

private fun GuideMenuController.comingSoon(action: GuideCellAction) = show(GuideLayer.ComingSoon(action.label))
