package com.johncorser.telly.features.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.TellyScreenTimesLine
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * Shared search-screen primitives: the grey-white section header
 * ("Channels" / "Programs" / "Search history", captures 49/50) and the
 * focus-pill rows every list on this screen is built from (jscpd 0).
 */
@Composable
internal fun SearchScreenHeader(
    textRes: Int,
    modifier: Modifier = Modifier.padding(start = Dims.edgePad, top = Dims.headerTop),
) {
    // 19 sp with the line box trimmed to the em: the reference header is
    // 38 px tall (tm-02 uidump); Compose's default font padding inflated
    // the same 19 sp to a 44 px (~22 sp) box.
    Text(
        text = stringResource(textRes),
        color = Color(TELLY_TEXT_PRIMARY),
        style =
            TextStyle(
                fontSize = 19.sp,
                lineHeight = 19.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
            ),
        modifier = modifier,
    )
}

/** Air-time line: "12:45 — 01:45 AM ▬▬ 50 min" while airing (live tm-03), bare times otherwise. */
@Composable
internal fun SearchScreenAirTime(
    hit: SearchProgramHit,
    fontSize: TextUnit = 15.sp,
) {
    if (hit.remaining == null) {
        Text(text = hit.timeText, color = Color(TELLY_TEXT_MUTED), fontSize = fontSize, maxLines = 1)
    } else {
        TellyScreenTimesLine(range = hit.timeText, permille = hit.progressPermille, remaining = hit.remaining)
    }
}

/** TiviMate's universal focus pill around arbitrary row/card content. */
@Composable
internal fun SearchScreenFocusRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    restingContainer: Color = Color.Transparent,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = restingContainer),
    ) {
        content()
    }
}

/** One plain text row (history entries, dropdown actions). */
@Composable
internal fun SearchScreenTextRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchScreenFocusRow(onClick = onClick, modifier = modifier.height(40.dp)) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(text = text, fontSize = 16.sp, maxLines = 1)
        }
    }
}
