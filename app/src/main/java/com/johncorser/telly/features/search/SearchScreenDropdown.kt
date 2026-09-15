package com.johncorser.telly.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.ui.rememberAutoFocus
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * OK on a programme result opens the guide-cell action dropdown (capture
 * 27): Remind / Record / Custom recording / Add to My list / Program
 * description. Rendered at the detail-card anchor; the rows act through
 * [SearchProgramMenu] (the guide's stores) and the Remind / My-list labels
 * flip live exactly like the guide's cells.
 */
@Composable
internal fun SearchScreenDropdown(
    viewModel: SearchViewModel,
    hit: SearchProgramHit,
) {
    val firstFocus = rememberAutoFocus()
    val reminderKeys by viewModel.programMenu.reminderKeys.collectAsState()
    val myListKeys by viewModel.programMenu.myListKeys.collectAsState()
    Box(
        Modifier
            .fillMaxSize()
            .padding(top = Dims.detailTop, end = Dims.edgePad),
        contentAlignment = Alignment.TopEnd,
    ) {
        Column(
            Modifier
                .width(Dims.detailWidth)
                .clip(RoundedCornerShape(Dims.barCorner))
                .background(Dims.detailFill)
                .padding(vertical = 8.dp),
        ) {
            SearchProgramAction.entries.forEachIndexed { index, action ->
                SearchScreenTextRow(
                    text = viewModel.programMenu.label(action, hit, reminderKeys, myListKeys),
                    onClick = { viewModel.programMenu.onAction(action, hit) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .let { if (index == 0) it.focusRequester(firstFocus) else it },
                )
            }
        }
    }
}
