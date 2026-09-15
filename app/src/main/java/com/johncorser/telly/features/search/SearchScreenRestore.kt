package com.johncorser.telly.features.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

/**
 * The remembered results node plus the single [FocusRequester] the query
 * bar's DOWN uses to restore it; [searchRestoreTarget] pins the requester
 * onto whichever composed node matches the memory (round7 §C4 P2 —
 * leanback-style focus memory below the bar).
 */
internal data class SearchScreenRestore(
    val node: SearchFocusMemory.Node?,
    val requester: FocusRequester,
)

@Composable
internal fun rememberSearchScreenRestore(viewModel: SearchViewModel): SearchScreenRestore {
    val requester = remember { FocusRequester() }
    val node by viewModel.focusMemory.node.collectAsState()
    return SearchScreenRestore(node, requester)
}

/** The query bar's DOWN chain: remembered node first, then [first]. */
internal fun SearchScreenRestore.downTargets(first: FocusRequester?): List<FocusRequester> =
    SearchFocusMemory.targets(node, requester, first)

/** Attaches the restore requester when [node] is the remembered one. */
internal fun Modifier.searchRestoreTarget(
    restore: SearchScreenRestore,
    node: SearchFocusMemory.Node,
): Modifier = if (restore.node == node) focusRequester(restore.requester) else this

/**
 * One results-area node: records itself into the focus memory when it
 * takes real D-pad focus and carries the restore requester while it is
 * the remembered node.
 */
internal fun Modifier.searchResultsNode(
    memory: SearchFocusMemory,
    restore: SearchScreenRestore,
    node: SearchFocusMemory.Node,
): Modifier =
    searchRestoreTarget(restore, node)
        .onFocusChanged { if (it.isFocused) memory.onFocused(node) }
