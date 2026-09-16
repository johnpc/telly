package com.johncorser.telly.features.guide

import com.johncorser.telly.features.catchup.GuideCatchup
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.mylist.MyListMenu
import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.playback.PlaybackEnv
import com.johncorser.telly.features.playback.PlaybackLifecycle
import com.johncorser.telly.features.playback.TuneBlockPrompt
import com.johncorser.telly.features.playback.gatedTuner
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.recording.RecordingMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The TV-guide screen's state machine: grid rows over the visible window,
 * focus/scroll, the active layer, and the preview tuner. A plain class —
 * everything injected, unit-tested on the JVM. "Now" ticks per minute and
 * re-seeds on foreground resume ([GuideNow]); no wall-clock reads in logic.
 */
class GuideController(
    env: PlaybackEnv,
    history: WatchHistory,
    private val pastDays: () -> Int,
    scope: CoroutineScope,
    private val callbacks: GuideCallbacks,
    seams: GuideSeams = GuideSeams(),
) {
    /** 12/24-hour rendering (+ zone) for the header clock and timeline ticks. */
    val clockStyle = env.time.style

    /** OK on a playable past cell hands the archive to fullscreen playback. */
    private val catchup = GuideCatchup(env.hooks.catchup.session, env.time.clock, callbacks.onFullscreen)

    /** Settings → Remote control → TV guide key remaps, read per key press. */
    private val keymap = seams.keymap

    /** Minute-ticked "now" (header clock, now-line); origin stays anchored. */
    private val ticker = GuideNow(env.time.clock, scope, env.time.minuteTicks)
    val now: StateFlow<Long> = ticker.now
    val originMs = GuideGeometry.halfHourFloor(now.value, clockStyle.zone)

    private val tuner = gatedTuner(env, history, scope, external = callbacks.external)

    /** The blocked-channel tune gate's prompt (guide OK / preview restore). */
    val blockPrompt = TuneBlockPrompt(tuner)

    /** Exit-confirm state + the persisted PIN input method (screen chrome). */
    val chrome = GuideChrome(env, scope)

    /** Background stop + foreground re-seed/re-tune (round7 resume P2). */
    val lifecycle = PlaybackLifecycle(tuner, onForegrounded = ticker::reseed, recover = tuner::retune)
    private val selected = MutableStateFlow(PanelViewModel.ALL_CHANNELS)
    private val focusEngine =
        GuideFocusEngine(originMs, { GuideWindowMath.scrollFloorDp(pastDays()) }, visibleRows = seams.visibleRows)
    private val feed =
        GuideRowsFeed(
            GuideRowsSources(tuner.channels, selected.asStateFlow()),
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

    val info: StateFlow<GuideInfoData?> = GuideInfoBuilder.feed(rows, focusEngine.focus, now, clockStyle, scope)

    /** My-list toggle state: the dropdown/sheet labels flip on its keys. */
    val myList = MyListMenu(seams.myList, env.time.clock, scope)

    private val sheetMyList = guideMyListHost(myList, focus, { selected.value }, callbacks)

    /** Layers + the long-OK row context sheet (catalogue §3 38-42). */
    val menu =
        GuideMenuController(
            channelActions = guideSheetActions(env, scope, sheetMyList, tuner::zapAwayFrom),
            focusedRow = ::focusedRow,
            info = { info.value },
            callbacks = callbacks,
            focusMemory = GuideFocusMemory(focusEngine, seams.visibleRows) { rows.value },
            recording = { recordingMenu },
        )

    /** The sheet/cell Record rows act through this (null while no DVR wired). */
    val recordingMenu: RecordingMenu? =
        env.hooks.recording?.let { center ->
            RecordingMenu(center, scope, env.time.clock) { prompt -> menu.onRecordingPrompt(prompt) }
        }

    val layer: StateFlow<GuideLayer> = menu.layer

    /** Transition half of the state machine: commands over engine + layers. */
    private val commands =
        GuideCommands(focusEngine, menu, { rows.value }, pastDays, seams.visibleRows, ::activate)

    init {
        menu.remind.reminders = seams.reminders
        scope.launch { rows.collect { focusEngine.ensureFocus(it, now.value) } }
        // The guide is reached from playback (BACK / the TV-guide card), where
        // the last channel keeps playing in the preview window; a cold start
        // with "Turn on last channel on app start" OFF instead lands here
        // untuned — the preview stays dark until OK tunes a cell.
        if (seams.resumePreview()) tuner.resumeStored()
    }

    /** Routes a key through the layer map; true = consumed. */
    fun onKey(key: GuideKey): Boolean =
        GuideKeyPolicy.commandFor(layer.value, key, keymap())?.also(commands::execute) != null

    /** OK on a group filters the grid and renumbers from 1 (capture 74). */
    fun selectGroup(group: String) {
        if (group != selected.value) {
            focusEngine.reset()
            selected.value = group
            focusEngine.ensureFocus(rows.value, now.value)
        }
        menu.reset()
    }

    fun close() = tuner.release()

    private fun focusedRow(): GuideRow? = focus.value?.let { rows.value.getOrNull(it.rowIndex) }

    private fun activate() {
        val focused = focus.value ?: return
        val row = focusedRow() ?: return
        when (val action = GuideActivation.activate(row, focused.cell, now.value, tuner.current.value?.id)) {
            is GuideAction.TunePreview -> tuner.tune(action.channel)
            GuideAction.GoFullscreen -> callbacks.onFullscreen()
            is GuideAction.OpenCellMenu -> menu.show(GuideLayer.CellMenu(action.cell))
            is GuideAction.PlayCatchup -> catchup.play(action.channel, action.cell)
        }
    }
}
