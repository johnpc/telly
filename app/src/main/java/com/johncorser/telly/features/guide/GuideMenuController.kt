package com.johncorser.telly.features.guide

import com.johncorser.telly.features.mylist.MyListProgramme
import com.johncorser.telly.features.playback.PlayerMenuFocus
import com.johncorser.telly.features.playback.PlayerMenuItem
import com.johncorser.telly.features.playback.PlayerMenuRoute
import com.johncorser.telly.features.playback.PlayerMenuRouting
import com.johncorser.telly.features.recording.RecordingMenu
import com.johncorser.telly.features.recording.RecordingPrompt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The guide's overlay-layer state machine plus the long-OK row context
 * sheet (catalogue §3 38-42 + round3-ref 05), routed through the shared
 * [PlayerMenuRouting] table: genuinely supported rows act (Search,
 * Settings, favorites toggle, hide, the programme description); every
 * unbuilt row lands on the branded coming-soon placeholder until its
 * slice ships.
 */
class GuideMenuController(
    internal val channelActions: GuideSheetChannelActions,
    private val focusedRow: () -> GuideRow?,
    private val info: () -> GuideInfoData?,
    private val callbacks: GuideCallbacks,
    private val focusMemory: GuideFocusMemory? = null,
    private val recording: () -> RecordingMenu? = { null },
) {
    private val mutable = MutableStateFlow<GuideLayer>(GuideLayer.Grid)
    val layer: StateFlow<GuideLayer> = mutable.asStateFlow()

    /** The cell dropdown's live Remind row (reminders slice seam). */
    val remind = GuideRemind(focusedRow) { mutable.value }

    /** Which sheet row BACK from a pushed screen re-focuses. */
    val sheetFocus = PlayerMenuFocus()

    /** The channel the sheet scrim dims AROUND (round7 P2 row exemption). */
    val sheetChannelId: Long? get() = focusMemory?.savedChannelId

    /** Closing back to the grid re-asserts the saved grid focus first. */
    fun show(next: GuideLayer) {
        if (next == GuideLayer.Grid) focusMemory?.restore()
        mutable.value = next
    }

    fun reset() = show(GuideLayer.Grid)

    /**
     * BACK pops one level: pushed screens return to the sheet, the sheet to
     * the grid — except Channel options, which REPLACED the sheet, so its
     * BACK lands directly on the grid (ref-round6 §A).
     */
    fun close() {
        val current = mutable.value
        if (current is GuideLayer.CustomRecording) recording()?.dismissForm()
        show(backOf(current))
    }

    /** Long-OK / MENU with a focused row opens the sheet (round3-ref 05). */
    fun openRowMenu() {
        if (focusedRow() == null) return
        focusMemory?.save()
        sheetFocus.clear()
        show(GuideLayer.RowMenu)
    }

    private val recordingActions = GuideRecordingActions(recording, focusedRow, ::show)

    /**
     * Remind toggles a reminder, "Add to My list" toggles the cell's
     * programme and the Record rows act through the DVR menu; every other
     * dropdown row is an unbuilt feature and lands on coming-soon.
     */
    fun onCellAction(action: GuideCellAction) {
        if (action == GuideCellAction.REMIND && remind.toggle()) {
            reset()
            return
        }
        val programme = (mutable.value as? GuideLayer.CellMenu)?.cell?.program?.let(MyListProgramme::of)
        val row = focusedRow()
        if (action == GuideCellAction.ADD_TO_MY_LIST && programme != null && row != null) {
            channelActions.myList?.menu?.toggle(row.channel, programme)
            reset()
            return
        }
        when (action) {
            GuideCellAction.RECORD -> recordingActions.recordCell(mutable.value)
            GuideCellAction.CUSTOM_RECORDING -> recordingActions.customRecording()
            else -> show(GuideLayer.ComingSoon(action.label))
        }
    }

    /** The DVR menu's outcomes, mapped onto guide layers. */
    fun onRecordingPrompt(prompt: RecordingPrompt) = show(GuideRecordingPrompts.layerFor(prompt, mutable.value))

    /** All §41 pane rows are locked; any activation lands on coming-soon. */
    fun onChannelOption(rowId: String) = show(GuideLayer.ComingSoon(rowId, back = mutable.value))

    fun onMenuItem(item: PlayerMenuItem) {
        val row = focusedRow() ?: return
        sheetFocus.onActivated(item)
        when (val route = PlayerMenuRouting.routeOf(item)) {
            PlayerMenuRoute.SEARCH -> {
                reset()
                callbacks.onOpenSearch()
            }
            PlayerMenuRoute.SETTINGS -> callbacks.onOpenSettings()
            PlayerMenuRoute.TOGGLE_FAVORITE -> {
                channelActions.toggleFavorite(row.channel)
                reset()
            }
            PlayerMenuRoute.HIDE_CHANNEL -> {
                channelActions.hide(row.channel)
                reset()
            }
            // The row fires the chooser regardless of the tune-time setting.
            PlayerMenuRoute.EXTERNAL_PLAYER -> {
                callbacks.external.open(row.channel.source.streamUrl)
                reset()
            }
            PlayerMenuRoute.TOGGLE_BLOCK -> show(GuideLayer.BlockPin(row.channel, channelActions.blocker.mode()))
            PlayerMenuRoute.DESCRIPTION -> show(descriptionLayer(info()))
            PlayerMenuRoute.CHANNEL_OPTIONS -> show(GuideLayer.ChannelOptions(row.channel.source.name))
            // My-list rows act (or push their route) and land on the grid,
            // the management screens closing the sheet first like Search.
            PlayerMenuRoute.MY_LIST_TOGGLE,
            PlayerMenuRoute.MANAGE_FAVORITES,
            PlayerMenuRoute.REORDER_CHANNELS,
            -> {
                reset()
                channelActions.myList?.run(route, row.channel)
            }
            PlayerMenuRoute.RECORD -> recordingActions.record(row.channel)
            PlayerMenuRoute.CUSTOM_RECORDING -> recordingActions.customRecording()
            PlayerMenuRoute.COMING_SOON -> show(GuideLayer.ComingSoon(item.label, back = GuideLayer.RowMenu))
        }
    }
}
