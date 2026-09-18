package com.johncorser.telly.core.ui

import androidx.compose.foundation.basicMarquee
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED

/**
 * Semibold single-line programme title with TiviMate's fallback: 17 sp on
 * the overlay/panel cards, 23 sp in the guide's info pane (uidump 24).
 */
@Composable
fun TellyScreenProgramTitle(
    title: String?,
    fontSize: TextUnit = 17.sp,
) {
    Text(
        text = title ?: "No information",
        color = Color.White,
        fontSize = fontSize,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Plain white single-line text (group labels, glyph markers). */
@Composable
fun TellyScreenWhiteText(
    text: String,
    fontSize: TextUnit = 14.sp,
) {
    Text(
        text = text,
        color = Color.White,
        fontSize = fontSize,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
    )
}

/** Single-line ellipsized row label with a caller-picked color. */
@Composable
fun TellyScreenRowLabel(
    text: String,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    marquee: Boolean = false,
) {
    Text(
        text = text,
        // basicMarquee measures the text at its intrinsic width, so an
        // overflowing label scrolls instead of ellipsizing while [marquee].
        modifier = if (marquee) modifier.basicMarquee(iterations = Int.MAX_VALUE) else modifier,
        color = color,
        fontSize = fontSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Grey supporting text (times, descriptions, next-programme lines). */
@Composable
fun TellyScreenMutedText(
    text: String,
    fontSize: TextUnit = 15.sp,
    maxLines: Int = 1,
) {
    Text(
        text = text,
        color = Color(TELLY_TEXT_MUTED),
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
