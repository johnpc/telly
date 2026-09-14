package com.johncorser.telly.features.guide

import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.playback.ChannelActions
import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.playback.ProgramTimes
import com.johncorser.telly.features.playback.TuneController
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The TV-guide screen's state machine: grid rows over the visible window,
 * focus/scroll, the active layer, and the preview tuner. A plain class —
 * everything injected, unit-tested on the JVM. "Now" is sampled once per
 * open (like the panel); no wall-clock reads in logic.
 */
class GuideController(
    env: PlaybackEnv,
    private val pastDays: () -> Int,
    scope: CoroutineScope,
    private val callbacks: GuideCallbacks,
) {
    val zone = env.zone
    val nowMs = env.clock()
    val originMs = GuideGeometry.halfHourFloor(nowMs, zone)

    /** "Sun, Sep 13, 2:44 PM" in blue at the header's left (uidump 24). */
    val clockText: String = ProgramTimes.clock(nowMs, zone)

    private val tuner = TuneController(env.engine, env.store, scope, env.channelDao)
    private val selected = MutableStateFlow(PanelViewModel.ALL_CHANNELS)
    private val focusEngine = GuideFocusEngine(originMs, pastFloorDp = { GuideWindowMath.scrollFloorDp(pastDays()) })
    private val feed =
        GuideRowsFeed(
            tuner.channels,
            selected.asStateFlow(),
            focusEngine.scrollX,
            env.epgRepository::programsFor,
            originMs,
            scope,
        )

    val rows: StateFlow<List<GuideRow>> = feed.rows
    val groups: StateFlow<List<String>> = feed.groups
    val selectedGroup: StateFlow<String> = selected.asStateFlow()
    val focus: StateFlow<GuideFocus?> = focusEngine.focus
    val scrollX: StateFlow<Float> = focusEngine.scrollX
    val firstVisibleRow: StateFlow<Int> = focusEngine.firstVisibleRow
    val preview: StateFlow<ChannelEntity?> = tuner.current

    val hint: StateFlow<Boolean> = GuideHint(env.store).startIn(scope)

    val info: StateFlow<GuideInfoData?> =
        combine(rows, focusEngine.focus) { list, focused -> GuideInfoBuilder.buildFor(list, focused, nowMs, zone) }
            .stateIn(scope, SharingStarted.Eagerly, null)

    /** Layers + the long-OK row context sheet (catalogue §3 38-42). */
    val menu =
        GuideMenuController(
            actions = ChannelActions(env.channelDao, scope),
            zapAway = tuner::zapAwayFrom,
            focusedRow = ::focusedRow,
            info = { info.value },
            callbacks = callbacks,
        )

    val layer: StateFlow<GuideLayer> = menu.layer

    init {
        scope.launch { rows.collect { focusEngine.ensureFocus(it, nowMs) } }
        // The guide is reached from playback (BACK / the TV-guide card), where
        // the last channel keeps playing in the preview window; cold starts
        // land on fullscreen playback instead, so nothing double-tunes.
        tuner.resumeStored()
    }

    /** Routes a key through the layer map; true = consumed. */
    fun onKey(key: GuideKey): Boolean {
        val command = GuideKeyPolicy.commandFor(layer.value, key) ?: return false
        execute(command)
        return true
    }

    /** OK on a group filters the grid and renumbers from 1 (capture 74). */
    fun selectGroup(group: String) {
        if (group != selected.value) {
            focusEngine.reset()
            selected.value = group
            focusEngine.ensureFocus(rows.value, nowMs)
        }
        menu.reset()
    }

    fun close() = tuner.release()

    private fun execute(command: GuideCommand) {
        when (command) {
            GuideCommand.FocusLeft -> if (!focusEngine.moveLeft(rows.value)) menu.show(GuideLayer.Groups)
            GuideCommand.FocusRight -> focusEngine.moveRight(rows.value)
            GuideCommand.FocusUp -> focusEngine.moveVertical(rows.value, -1)
            GuideCommand.FocusDown -> focusEngine.moveVertical(rows.value, +1)
            is GuideCommand.DayJump -> focusEngine.dayJump(command.days, pastDays())
            GuideCommand.Activate -> activate()
            GuideCommand.OpenRowMenu -> menu.openRowMenu()
            GuideCommand.CloseLayer -> menu.close()
        }
    }

    private fun focusedRow(): GuideRow? = focus.value?.let { rows.value.getOrNull(it.rowIndex) }

    private fun activate() {
        val focused = focus.value ?: return
        val row = focusedRow() ?: return
        when (val action = GuideActivation.activate(row, focused.cell, nowMs, tuner.current.value?.id)) {
            is GuideAction.TunePreview -> tuner.tune(action.channel)
            GuideAction.GoFullscreen -> callbacks.onFullscreen()
            is GuideAction.OpenCellMenu -> menu.show(GuideLayer.CellMenu(action.cell))
        }
    }
}
