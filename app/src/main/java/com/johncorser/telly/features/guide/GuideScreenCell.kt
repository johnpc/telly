package com.johncorser.telly.features.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_FOCUS_FILL
import com.johncorser.telly.core.design.TELLY_FOCUS_TEXT
import com.johncorser.telly.core.design.TELLY_GUIDE_CELL
import com.johncorser.telly.core.design.TELLY_GUIDE_CELL_SELECTED
import com.johncorser.telly.features.epg.ProgramTitle

/**
 * One programme cell (uidump 24): rounded #1B1E21 pill with a 2 dp gap,
 * single-line ellipsized "Title: Subtitle" text, past programmes keeping
 * the cell fill with only their text dimmed (capture 24), the focused
 * cell the universal white pill — or the grey "selected" pill while the
 * groups column holds the white focus (capture 25, [dimFocus]); a cell
 * that started before the window clamps to its left edge so the label
 * stays visible.
 */
@Composable
internal fun GuideScreenCell(
    cell: GuideCell,
    placement: GuideCellPlacement,
    focused: Boolean,
    dimFocus: Boolean,
    past: Boolean,
) {
    val container =
        when {
            focused && dimFocus -> Color(TELLY_GUIDE_CELL_SELECTED)
            focused -> Color(TELLY_FOCUS_FILL)
            else -> Color(TELLY_GUIDE_CELL)
        }
    val content =
        when {
            focused && dimFocus -> Color.White
            focused -> Color(TELLY_FOCUS_TEXT)
            past -> Color.White.copy(alpha = 0.35f)
            else -> Color.White.copy(alpha = 0.9f)
        }
    Box(
        Modifier
            .offset(x = placement.offsetDp.dp)
            .width(placement.widthDp.dp)
            .fillMaxHeight()
            .padding(end = 2.dp, top = 1.dp, bottom = 1.dp)
            .background(container, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = cell.program?.details?.let(ProgramTitle::of) ?: GuideInfoBuilder.NO_INFORMATION,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = content,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
