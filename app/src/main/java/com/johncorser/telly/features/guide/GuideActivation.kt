package com.johncorser.telly.features.guide

import com.johncorser.telly.features.playlist.db.ChannelEntity

/** What OK on a cell does (device-verified two-stage tune, capture 32/27). */
sealed interface GuideAction {
    /** Stage 1: tune the airing programme's channel in the preview window. */
    data class TunePreview(
        val channel: ChannelEntity,
    ) : GuideAction

    /** Stage 2: the previewed channel goes fullscreen. */
    data object GoFullscreen : GuideAction

    /** Non-airing programme: the anchored premium-action dropdown. */
    data class OpenCellMenu(
        val cell: GuideCell,
    ) : GuideAction
}

object GuideActivation {
    /**
     * OK #1 on an airing programme tunes the preview; OK #2 (same channel
     * already previewed) goes fullscreen. OK on any non-airing programme —
     * future per capture 27, past by extension (catch-up is premium) —
     * opens the dropdown instead.
     */
    fun activate(
        row: GuideRow,
        cell: GuideCell,
        nowMs: Long,
        previewChannelId: Long?,
    ): GuideAction =
        when {
            !cell.contains(nowMs) -> GuideAction.OpenCellMenu(cell)
            row.channel.id == previewChannelId -> GuideAction.GoFullscreen
            else -> GuideAction.TunePreview(row.channel)
        }
}
