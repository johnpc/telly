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
 * the live-TV section lit, settings gear at the bottom. Search, the DVR
 * icon and the gear are live targets (LEFT from the groups column reaches
 * the gear, OK opens Search / the Recordings library / the settings sheet;
 * UP/DOWN move between them and RIGHT returns to the groups column); the
 * tv/My-list sections are their own future slices and stay decorative.
 */
@Composable
internal fun GuideScreenRail(
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecordings: () -> Unit,
    searchFocus: FocusRequester = remember { FocusRequester() },
    gearFocus: FocusRequester = remember { FocusRequester() },
    groupsFocus: FocusRequester = remember { FocusRequester() },
    dvrFocus: FocusRequester = remember { FocusRequester() },
) {
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
            modifier =
                Modifier
                    .padding(top = 24.dp)
                    .focusRequester(searchFocus)
                    .focusProperties {
                        down = dvrFocus
                        right = groupsFocus
                    },
        )
        GuideScreenRailIcon(R.drawable.ic_rail_tv, Color.White, Modifier.padding(top = 24.dp))
        GuideScreenRailButton(
            icon = R.drawable.ic_rail_dvr,
            onClick = onOpenRecordings,
            contentDescription = "Recordings",
            modifier =
                Modifier
                    .padding(top = 16.dp)
                    .focusRequester(dvrFocus)
                    .focusProperties {
                        up = searchFocus
                        down = gearFocus
                        right = groupsFocus
                    },
        )
        GuideScreenRailIcon(R.drawable.ic_rail_bookmark, muted, Modifier.padding(top = 16.dp))
        Spacer(Modifier.weight(1f))
        GuideScreenRailButton(
            icon = R.drawable.ic_menu_settings,
            onClick = onOpenSettings,
            modifier =
                Modifier
                    .focusRequester(gearFocus)
                    .focusProperties {
                        up = dvrFocus
                        right = groupsFocus
                    },
        )
        Spacer(Modifier.height(16.dp))
    }
}
