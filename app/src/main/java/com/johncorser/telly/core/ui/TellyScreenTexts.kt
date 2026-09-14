package com.johncorser.telly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED

/** 17 sp semibold single-line programme title with TiviMate's fallback. */
@Composable
fun TellyScreenProgramTitle(title: String?) {
    Text(
        text = title ?: "No information",
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Plain white body text (group labels, glyph markers). */
@Composable
fun TellyScreenWhiteText(
    text: String,
    fontSize: TextUnit = 14.sp,
) {
    Text(text = text, color = Color.White, fontSize = fontSize)
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
