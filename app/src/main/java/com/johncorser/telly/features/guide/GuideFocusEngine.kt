package com.johncorser.telly.features.guide

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The grid's focus + scroll state machine. Horizontal moves update the
 * time anchor and pan the window; vertical moves keep the anchor and
 * scroll by rows. Plain navigation never pans left of "now" (the free
 * reference stays at the live edge, capture 25); only a day jump does,
 * bounded by the "Past days to keep EPG" setting.
 */
class GuideFocusEngine(
    private val originMs: Long,
    private val pastFloorDp: () -> Float,
    private val viewportDp: Float = GuideGeometry.TIME_VIEWPORT_DP,
) {
    private val mutableFocus = MutableStateFlow<GuideFocus?>(null)
    private val mutableScroll = MutableStateFlow(0f)
    private val mutableFirstRow = MutableStateFlow(0)

    val focus: StateFlow<GuideFocus?> = mutableFocus.asStateFlow()
    val scrollX: StateFlow<Float> = mutableScroll.asStateFlow()
    val firstVisibleRow: StateFlow<Int> = mutableFirstRow.asStateFlow()

    /** Re-resolves focus after every rows emission (loads, group switches). */
    fun ensureFocus(
        rows: List<GuideRow>,
        nowMs: Long,
    ) {
        mutableFocus.value = if (rows.isEmpty()) null else GuideFocusNav.resolve(rows, nowMs, mutableFocus.value)
    }

    fun reset() = apply(GuideFocusState(focus = null, scrollX = 0f, firstRow = 0))

    /** Re-asserts a remembered focus + scroll ([GuideFocusMemory.restore]). */
    fun apply(state: GuideFocusState) {
        mutableFocus.value = state.focus
        mutableScroll.value = state.scrollX
        mutableFirstRow.value = state.firstRow
    }

    /** False = focus already touches the left edge (open the groups column). */
    fun moveLeft(rows: List<GuideRow>): Boolean {
        val current = mutableFocus.value ?: return false
        val cells = rows.getOrNull(current.rowIndex)?.cells.orEmpty()
        val windowStartMs = GuideGeometry.timeAt(mutableScroll.value, originMs)
        val target = GuideFocusNav.leftOf(cells, current.cell, windowStartMs) ?: return false
        moveHorizontal(current, target)
        return true
    }

    /** At the materialized edge the window pans on so more cells load. */
    fun moveRight(rows: List<GuideRow>) {
        val current = mutableFocus.value ?: return
        val cells = rows.getOrNull(current.rowIndex)?.cells.orEmpty()
        val target = GuideFocusNav.rightOf(cells, current.cell)
        if (target == null) {
            mutableScroll.value = clampScroll(mutableScroll.value + GuideGeometry.DP_PER_30_MIN)
        } else {
            moveHorizontal(current, target)
        }
    }

    fun moveVertical(
        rows: List<GuideRow>,
        delta: Int,
    ) {
        val current = mutableFocus.value ?: return
        val rowIndex = (current.rowIndex + delta).coerceIn(0, rows.lastIndex)
        if (rowIndex == current.rowIndex) return
        val cell = GuideFocusNav.cellAt(rows[rowIndex].cells, current.anchorMs) ?: return
        mutableFocus.value = GuideFocus(rowIndex, cell, current.anchorMs)
        mutableFirstRow.value =
            GuideScrollPlanner.rowWindow(mutableFirstRow.value, rowIndex, GuideGeometry.VISIBLE_ROWS, rows.size)
    }

    /**
     * Long-LEFT/RIGHT: jump the anchor a whole day and re-align the window.
     * The anchor moves first; the scroll change re-materializes the rows and
     * [ensureFocus] then resolves the cell at the new anchor.
     */
    fun dayJump(
        days: Int,
        maxPastDays: Int,
    ) {
        val current = mutableFocus.value ?: return
        val floorDp = GuideWindowMath.scrollFloorDp(maxPastDays)
        val floorMs = GuideGeometry.timeAt(floorDp, originMs)
        val ceilMs = GuideGeometry.timeAt(GuideWindowMath.scrollCeilDp(), originMs)
        val targetMs = (current.anchorMs + days * GuideGeometry.DAY_MS).coerceIn(floorMs, ceilMs)
        mutableFocus.value = current.copy(anchorMs = targetMs)
        val alignedScroll = GuideGeometry.xOf(GuideWindowMath.quantizeDown(targetMs, originMs), originMs)
        mutableScroll.value = alignedScroll.coerceIn(floorDp, GuideWindowMath.scrollCeilDp())
    }

    /**
     * LEFT/RIGHT in the "Move by page" remap: pan a whole viewport, keeping
     * the anchor's relative position. Clamped exactly like plain moves, so
     * false = already at the edge (left edge → the groups column).
     */
    fun pageJump(
        rows: List<GuideRow>,
        direction: Int,
    ): Boolean {
        val current = mutableFocus.value ?: return false
        val from = mutableScroll.value
        val target = clampScroll(from + direction * viewportDp)
        if (target == from) return false
        mutableScroll.value = target
        val anchorMs = current.anchorMs + GuideGeometry.timeAt(target, 0L) - GuideGeometry.timeAt(from, 0L)
        val cell = GuideFocusNav.cellAt(rows.getOrNull(current.rowIndex)?.cells.orEmpty(), anchorMs)
        mutableFocus.value = current.copy(cell = cell ?: current.cell, anchorMs = anchorMs)
        return true
    }

    private fun moveHorizontal(
        current: GuideFocus,
        target: GuideCell,
    ) {
        val scroll =
            clampScroll(GuideScrollPlanner.horizontalTarget(target, originMs, mutableScroll.value, viewportDp))
        mutableScroll.value = scroll
        val anchorMs = maxOf(target.startMs, GuideGeometry.timeAt(scroll, originMs))
        mutableFocus.value = current.copy(cell = target, anchorMs = anchorMs)
    }

    /** Plain moves stop at "now" unless a day jump already went past it. */
    private fun clampScroll(target: Float): Float {
        val floor = if (mutableScroll.value < 0f) pastFloorDp() else 0f
        return target.coerceIn(floor, GuideWindowMath.scrollCeilDp())
    }
}
