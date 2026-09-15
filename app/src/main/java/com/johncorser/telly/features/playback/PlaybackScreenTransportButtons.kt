package com.johncorser.telly.features.playback

import com.johncorser.telly.R

/**
 * The five center transport buttons and their routing: coming-soon feature
 * labels during live playback (live pause = timeshift, a later slice),
 * real transport actions during catch-up.
 */
internal const val PREVIOUS_INDEX = 0
internal const val REWIND_INDEX = 1
internal const val PAUSE_INDEX = 2
internal const val FORWARD_INDEX = 3

internal data class TransportButton(
    val icon: Int,
    val feature: String,
)

internal val transportButtons =
    listOf(
        TransportButton(R.drawable.ic_tr_prev, "Previous programme"),
        TransportButton(R.drawable.ic_tr_rewind, "Timeshift"),
        TransportButton(R.drawable.ic_tr_pause, "Timeshift"),
        TransportButton(R.drawable.ic_tr_forward, "Timeshift"),
        TransportButton(R.drawable.ic_tr_next, "Next programme"),
    )

/** ⏸ swaps to a play glyph while catch-up playback is paused. */
internal fun transportIcon(
    index: Int,
    icon: Int,
    catchup: TransportCatchup?,
): Int = if (index == PAUSE_INDEX && catchup?.paused == true) R.drawable.ic_tr_play else icon

/** During catch-up ⏮/⏭ hop programmes, RW/FF seek, ⏸ toggles pause. */
internal fun transportAction(
    index: Int,
    feature: String,
    onFeature: (String) -> Unit,
    catchup: TransportCatchup?,
) = when {
    catchup == null -> onFeature(feature)
    index == PREVIOUS_INDEX -> catchup.onPrevious()
    index == REWIND_INDEX -> catchup.onSeek(-catchup.skip.backMs)
    index == PAUSE_INDEX -> catchup.onPause()
    index == FORWARD_INDEX -> catchup.onSeek(catchup.skip.forwardMs)
    else -> catchup.onNext()
}

/** The ⏸ slot's semantics flip with the paused state (catch-up only). */
internal fun transportLabel(
    index: Int,
    feature: String,
    catchup: TransportCatchup?,
): String =
    when {
        index != PAUSE_INDEX || catchup == null -> feature
        catchup.paused -> "Resume"
        else -> "Pause"
    }
