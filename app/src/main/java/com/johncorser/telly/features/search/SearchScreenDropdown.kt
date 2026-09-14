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
 * description. Rendered at the detail-card anchor; every row leads to the
 * shared Unlock Premium screen, matching the free reference (capture 28).
 */
@Composable
internal fun SearchScreenDropdown(viewModel: SearchViewModel) {
    val firstFocus = rememberAutoFocus()
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
                    text = action.label,
                    onClick = { viewModel.overlays.show(SearchOverlay.Paywall) },
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
