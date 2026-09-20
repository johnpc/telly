package com.johncorser.telly.features.guide

import com.johncorser.telly.features.catchup.CatchupPlayability
import com.johncorser.telly.features.playlist.db.ChannelEntity

/** What a regular OK on a cell does (director round: OK jumps to the player). */
sealed interface GuideAction {
    /** Tune the row's channel and go straight to the fullscreen player. */
    data class PlayChannel(
        val channel: ChannelEntity,
    ) : GuideAction

    /** Past programme on a catch-up channel within horizon: play the archive. */
    data class PlayCatchup(
        val channel: ChannelEntity,
        val cell: GuideCell,
    ) : GuideAction
}

object GuideActivation {
    /**
     * Regular OK plays the focused row's channel in the full player (tune +
     * fullscreen in one press — the director's redesign; the old two-stage
     * preview→fullscreen and the non-airing dropdown moved to long-OK). OK on
     * a PAST programme of a catch-up-capable channel within its `catchup-days`
     * horizon still plays the archived broadcast instead (ux-spec §3.17).
     */
    fun activate(
        row: GuideRow,
        cell: GuideCell,
        nowMs: Long,
    ): GuideAction =
        if (CatchupPlayability.playable(row.channel, cell.startMs, cell.endMs, cell.hasInfo, nowMs)) {
            GuideAction.PlayCatchup(row.channel, cell)
        } else {
            GuideAction.PlayChannel(row.channel)
        }
}
