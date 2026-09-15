package com.johncorser.telly.features.guide

import com.johncorser.telly.features.catchup.CatchupPlayability
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

    /** Past programme on a catch-up channel within horizon: play the archive. */
    data class PlayCatchup(
        val channel: ChannelEntity,
        val cell: GuideCell,
    ) : GuideAction
}

object GuideActivation {
    /**
     * OK #1 on an airing programme tunes the preview; OK #2 (same channel
     * already previewed) goes fullscreen. OK on a PAST programme of a
     * catch-up-capable channel within its `catchup-days` horizon plays the
     * archived broadcast (ux-spec §3.17); OK on any other non-airing
     * programme opens the dropdown instead.
     */
    fun activate(
        row: GuideRow,
        cell: GuideCell,
        nowMs: Long,
        previewChannelId: Long?,
    ): GuideAction =
        when {
            CatchupPlayability.playable(row.channel, cell.startMs, cell.endMs, cell.hasInfo, nowMs) ->
                GuideAction.PlayCatchup(row.channel, cell)
            !cell.contains(nowMs) -> GuideAction.OpenCellMenu(cell)
            row.channel.id == previewChannelId -> GuideAction.GoFullscreen
            else -> GuideAction.TunePreview(row.channel)
        }
}
