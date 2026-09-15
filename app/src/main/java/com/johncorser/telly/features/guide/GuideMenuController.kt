package com.johncorser.telly.features.guide

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
    private val channelActions: GuideSheetChannelActions,
    private val focusedRow: () -> GuideRow?,
    private val info: () -> GuideInfoData?,
    private val callbacks: GuideCallbacks,
    private val focusMemory: GuideFocusMemory? = null,
    private val recording: () -> RecordingMenu? = { null },
) {
    private val mutable = MutableStateFlow<GuideLayer>(GuideLayer.Grid)
    val layer: StateFlow<GuideLayer> = mutable.asStateFlow()

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

    /** Record rows are live (recording slice); the rest stay coming-soon. */
    fun onCellAction(action: GuideCellAction) {
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
        when (PlayerMenuRouting.routeOf(item)) {
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
            PlayerMenuRoute.DESCRIPTION -> show(descriptionLayer(info()))
            PlayerMenuRoute.CHANNEL_OPTIONS -> show(GuideLayer.ChannelOptions(row.channel.source.name))
            PlayerMenuRoute.RECORD -> recordingActions.record(row.channel)
            PlayerMenuRoute.CUSTOM_RECORDING -> recordingActions.customRecording()
            PlayerMenuRoute.COMING_SOON -> show(GuideLayer.ComingSoon(item.label, back = GuideLayer.RowMenu))
        }
    }
}

/** The sheet's Program-description layer from the focused programme. */
private fun descriptionLayer(data: GuideInfoData?): GuideLayer.Description =
    GuideLayer.Description(
        title = data?.title ?: GuideInfoBuilder.NO_INFORMATION,
        text = data?.description ?: GuideInfoBuilder.NO_INFORMATION,
    )

/**
 * One BACK level per layer: pushed screens → sheet, everything else → grid.
 * Channel options is NOT a pushed screen: in the reference it replaces the
 * sheet (in-place cross-fade), so one BACK from it lands on the grid with
 * the originating row's focus restored by [GuideFocusMemory] (ref-round6
 * §A — the reference never returns to the sheet).
 */
private fun backOf(layer: GuideLayer): GuideLayer =
    when (layer) {
        is GuideLayer.ComingSoon -> layer.back
        is GuideLayer.Description -> layer.back
        is GuideLayer.RecordingStop -> layer.back
        is GuideLayer.CustomRecording -> layer.back
        else -> GuideLayer.Grid
    }
