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
import androidx.compose.ui.unit.dp
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED

/**
 * The 56 dp nav rail at the far left of the guide+groups view (capture
 * 25): logo on top, search / live-TV / DVR / My-list icons mid-rail with
 * the live-TV section lit, settings gear at the bottom. Search, the lit
 * live-TV section and the gear are live targets (the [GuideRailStop]
 * vertical chain; RIGHT returns to the groups column). OK on the live-TV
 * icon also returns focus to the guide — it IS the current section —
 * mirroring RIGHT; the DVR/My-list sections are their own future slices
 * and stay decorative.
 */
@Composable
internal fun GuideScreenRail(
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    searchFocus: FocusRequester = remember { FocusRequester() },
    gearFocus: FocusRequester = remember { FocusRequester() },
    groupsFocus: FocusRequester = remember { FocusRequester() },
) {
    val stops =
        mapOf(
            GuideRailStop.SEARCH to searchFocus,
            GuideRailStop.LIVE_TV to remember { FocusRequester() },
            GuideRailStop.GEAR to gearFocus,
        )
    Column(
        Modifier
            .width(56.dp)
            .fillMaxHeight()
            .background(Color(TELLY_GUIDANCE_PANE)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val muted = Color(TELLY_TEXT_MUTED)
        GuideScreenRailLogo(Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(123.dp))
        GuideScreenRailButton(
            icon = R.drawable.ic_menu_search,
            onClick = onOpenSearch,
            modifier = Modifier.padding(top = 24.dp).railStop(GuideRailStop.SEARCH, stops, groupsFocus),
        )
        GuideScreenRailButton(
            icon = R.drawable.ic_rail_tv,
            onClick = { groupsFocus.requestFocus() },
            modifier = Modifier.padding(top = 16.dp).railStop(GuideRailStop.LIVE_TV, stops, groupsFocus),
            restingTint = Color.White,
            contentDescription = "Live TV",
        )
        GuideScreenRailIcon(R.drawable.ic_rail_dvr, muted, Modifier.padding(top = 16.dp))
        GuideScreenRailIcon(R.drawable.ic_rail_bookmark, muted, Modifier.padding(top = 24.dp))
        Spacer(Modifier.weight(1f))
        GuideScreenRailButton(
            icon = R.drawable.ic_menu_settings,
            onClick = onOpenSettings,
            modifier = Modifier.railStop(GuideRailStop.GEAR, stops, groupsFocus),
        )
        Spacer(Modifier.height(16.dp))
    }
}

/**
 * One rail focus stop: its requester plus the [GuideRailStop] chain —
 * UP/DOWN walk the stops, RIGHT leaves for the groups column (capture 25).
 */
private fun Modifier.railStop(
    stop: GuideRailStop,
    stops: Map<GuideRailStop, FocusRequester>,
    groupsFocus: FocusRequester,
): Modifier =
    focusRequester(stops.getValue(stop)).focusProperties {
        stop.above()?.let { up = stops.getValue(it) }
        stop.below()?.let { down = stops.getValue(it) }
        right = groupsFocus
    }
