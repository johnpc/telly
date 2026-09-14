package com.johncorser.telly.features.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.LocalAccentColor

/**
 * The 56 dp nav rail at the far left of the guide+groups view (capture
 * 25): logo on top, search / live-TV / DVR / My-list icons mid-rail with
 * the live-TV section lit, settings gear at the bottom. Decorative only —
 * the nav sections are their own slices; D-pad focus stays in the groups
 * column.
 */
@Composable
internal fun GuideScreenRail() {
    Column(
        Modifier
            .width(56.dp)
            .fillMaxHeight()
            .background(Color(TELLY_GUIDANCE_PANE)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val muted = Color(TELLY_TEXT_MUTED)
        Text(
            text =
                buildAnnotatedString {
                    withStyle(SpanStyle(color = LocalAccentColor.current)) { append("t") }
                    append("v")
                },
            modifier = Modifier.padding(top = 8.dp),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(123.dp))
        GuideScreenRailIcon(R.drawable.ic_menu_search, muted)
        GuideScreenRailIcon(R.drawable.ic_rail_tv, Color.White)
        GuideScreenRailIcon(R.drawable.ic_rail_dvr, muted)
        GuideScreenRailIcon(R.drawable.ic_rail_bookmark, muted)
        Spacer(Modifier.weight(1f))
        GuideScreenRailIcon(R.drawable.ic_menu_settings, muted)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GuideScreenRailIcon(
    icon: Int,
    tint: Color,
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = Modifier.padding(top = 24.dp),
        tint = tint,
    )
}
