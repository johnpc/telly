package com.johncorser.telly.features.search

import androidx.compose.ui.focus.FocusRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * DOWN-from-the-query-bar focus memory (round7 §C4,
 * 10-tm-down-scrolled-shelf-focus-memory): after visiting a results node
 * the reference restores the LAST-focused card — leanback focus memory —
 * not card 1. telly remembers the last results-area node that held D-pad
 * focus. A new result batch clears the memory (the reference's behavior
 * after the results change underneath it is uncaptured), falling back to
 * the round-7-verified fresh-batch landing: first channel card, else
 * first airing row.
 */
class SearchFocusMemory {
    /** Identity of one focusable results node, stable across recomposition. */
    sealed interface Node {
        /** A Channels-shelf card (capture 50). */
        data class ChannelCard(
            val channelId: Long,
        ) : Node

        /** A Programs master-lane card (ref-round6 §D). */
        data class MasterCard(
            val channelId: Long,
        ) : Node

        /** One airing row of the selected channel's rows pane. */
        data class AiringRow(
            val channelId: Long,
            val startMs: Long,
        ) : Node
    }

    private val mutable = MutableStateFlow<Node?>(null)

    /** The node DOWN from the query bar restores; null = fresh landing. */
    val node: StateFlow<Node?> = mutable.asStateFlow()

    fun onFocused(visited: Node) {
        mutable.value = visited
    }

    fun clear() {
        mutable.value = null
    }

    companion object {
        /**
         * The requesters the query bar's DOWN tries in order: the
         * remembered node first (skipped when nothing was visited), then
         * the fresh-batch landing; a node that left composition throws on
         * request and falls through. An empty list keeps Compose's plain
         * spatial move (the history landing).
         */
        fun targets(
            remembered: Node?,
            restore: FocusRequester,
            first: FocusRequester?,
        ): List<FocusRequester> = listOfNotNull(restore.takeIf { remembered != null }, first)
    }
}
