package com.johncorser.telly.features.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.LocalAccentColor

/**
 * The 56 dp nav rail at the far left of the guide+groups view (capture
 * 25): logo on top, search / live-TV / DVR / My-list icons mid-rail with
 * the live-TV section lit, settings gear at the bottom. The gear is the
 * one live target (LEFT from the groups column reaches it, OK opens the
 * settings sheet, exactly TiviMate's path); the other sections are their
 * own future slices and stay decorative.
 */
@Composable
internal fun GuideScreenRail(
    onOpenSettings: () -> Unit,
    gearFocus: FocusRequester = remember { FocusRequester() },
    groupsFocus: FocusRequester = remember { FocusRequester() },
) {
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
        GuideScreenRailIcon(R.drawable.ic_menu_search, muted, Modifier.padding(top = 24.dp))
        GuideScreenRailIcon(R.drawable.ic_rail_tv, Color.White, Modifier.padding(top = 24.dp))
        GuideScreenRailIcon(R.drawable.ic_rail_dvr, muted, Modifier.padding(top = 24.dp))
        GuideScreenRailIcon(R.drawable.ic_rail_bookmark, muted, Modifier.padding(top = 24.dp))
        Spacer(Modifier.weight(1f))
        Surface(
            onClick = onOpenSettings,
            modifier =
                Modifier
                    .focusRequester(gearFocus)
                    .focusProperties { right = groupsFocus },
            shape = FocusScreenDefaults.shape(),
            scale = FocusScreenDefaults.scale(),
            colors = FocusScreenDefaults.colors(restingContainer = Color.Transparent),
        ) {
            GuideScreenRailIcon(R.drawable.ic_menu_settings, Color.Unspecified, Modifier.padding(8.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun GuideScreenRailIcon(
    icon: Int,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = modifier,
        tint = if (tint == Color.Unspecified) LocalContentColor.current else tint,
    )
}
