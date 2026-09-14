package com.johncorser.telly.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * The right-side detail card of the focused programme result (captures
 * 50/51): title, air time, then the description in muted grey.
 */
@Composable
internal fun SearchScreenDetail(hit: SearchProgramHit) {
    Column(
        Modifier
            .padding(top = Dims.detailTop, end = Dims.edgePad)
            .width(Dims.detailWidth)
            .clip(RoundedCornerShape(Dims.barCorner))
            .background(Dims.detailFill)
            .padding(Dims.detailPad),
    ) {
        Text(
            text = hit.title,
            color = Color(TELLY_TEXT_PRIMARY),
            fontSize = 20.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        Text(text = hit.timeText, color = Color(TELLY_TEXT_MUTED), fontSize = 15.sp, maxLines = 1)
        hit.program.details.description?.let { description ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                color = Color(TELLY_TEXT_MUTED),
                fontSize = 14.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
