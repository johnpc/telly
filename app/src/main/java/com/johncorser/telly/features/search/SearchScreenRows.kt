package com.johncorser.telly.features.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    // 14 sp in a 19 sp line box: the reference header box is 38 px tall
    // with a 21 px glyph band (tm-01/tm-02 re-measured round5 — the old
    // 19 sp read came from the box height, not the glyphs), and its white
    // sits at the results' 42% resting alpha.
    Text(
        text = stringResource(textRes),
        color = Color(TELLY_TEXT_PRIMARY).copy(alpha = Dims.RESTING_ALPHA),
        style =
            TextStyle(
                fontSize = 14.sp,
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
    fontSize: TextUnit = 14.sp,
) {
    if (hit.remaining == null) {
        Text(text = hit.timeText, color = Color(TELLY_TEXT_MUTED), fontSize = fontSize, maxLines = 1)
    } else {
        TellyScreenTimesLine(range = hit.timeText, permille = hit.progressPermille, remaining = hit.remaining)
    }
}

/**
 * TiviMate's universal focus pill around arbitrary row/card content. On the
 * search screen, resting results sit at 42% alpha (every tm-01/02/03 text,
 * logo and card fill samples as its full color x 0.42 over the background);
 * focus restores full opacity.
 */
@Composable
internal fun SearchScreenFocusRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    restingContainer: Color = Color.Transparent,
    dimWhenResting: Boolean = false,
    restingOutline: Boolean = false,
    content: @Composable () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .onFocusChanged { focused = it.isFocused }
                // The outline sits before the alpha layer so it renders at
                // its sampled on-screen color, not the 42% resting dim.
                .then(if (restingOutline && !focused) Modifier.searchSelectedCardBorder() else Modifier)
                .graphicsLayer { if (dimWhenResting) alpha = if (focused) 1f else Dims.RESTING_ALPHA },
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
