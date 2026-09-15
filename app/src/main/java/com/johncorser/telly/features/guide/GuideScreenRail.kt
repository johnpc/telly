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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE

/** One live rail icon: what it opens plus its focus node in the chain. */
private class RailTarget(
    val icon: Int,
    val onClick: () -> Unit,
    val focus: FocusRequester,
    val contentDescription: String? = null,
    val testTag: String? = null,
)

/**
 * The 56 dp nav rail at the far left of the guide+groups view (capture
 * 25): logo on top, search / live-TV / My-list / Movies / DVR icons
 * mid-rail with the live-TV section lit, settings gear at the bottom.
 * Search, the My-list bookmark, the Movies film icon (telly's VOD section
 * — the reference sells VOD as premium), the DVR icon (the Recordings
 * library) and the gear are live targets (LEFT from the groups column
 * reaches the gear, OK opens Search / My List / Movies / Recordings / the
 * settings sheet; UP/DOWN traverse search ↔ bookmark ↔ Movies ↔ DVR ↔ gear
 * and RIGHT returns to the groups column); the tv section is its own
 * future slice and stays decorative.
 */
@Composable
internal fun GuideScreenRail(
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMyList: () -> Unit = {},
    onOpenVod: () -> Unit = {},
    onOpenRecordings: () -> Unit = {},
    searchFocus: FocusRequester = remember { FocusRequester() },
    gearFocus: FocusRequester = remember { FocusRequester() },
    groupsFocus: FocusRequester = remember { FocusRequester() },
) {
    val bookmarkFocus = remember { FocusRequester() }
    val moviesFocus = remember { FocusRequester() }
    val dvrFocus = remember { FocusRequester() }
    val targets =
        listOf(
            RailTarget(R.drawable.ic_menu_search, onOpenSearch, searchFocus),
            RailTarget(R.drawable.ic_rail_bookmark, onOpenMyList, bookmarkFocus, "My list"),
            RailTarget(R.drawable.ic_rail_movie, onOpenVod, moviesFocus, testTag = "rail-movies"),
            RailTarget(R.drawable.ic_rail_dvr, onOpenRecordings, dvrFocus, "Recordings"),
            RailTarget(R.drawable.ic_menu_settings, onOpenSettings, gearFocus),
        )
    Column(
        Modifier
            .width(56.dp)
            .fillMaxHeight()
            .background(Color(TELLY_GUIDANCE_PANE)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GuideScreenRailLogo(Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(123.dp))
        targets.forEachIndexed { index, target ->
            // The lit live-TV glyph sits between search and the bookmark;
            // the gear parks at the very bottom, below the flexible gap.
            if (index == 1) GuideScreenRailIcon(R.drawable.ic_rail_tv, Color.White, Modifier.padding(top = 24.dp))
            val gear = index == targets.lastIndex
            if (gear) Spacer(Modifier.weight(1f))
            GuideScreenRailButton(
                icon = target.icon,
                onClick = target.onClick,
                contentDescription = target.contentDescription,
                modifier =
                    (if (gear) Modifier else Modifier.padding(top = 24.dp))
                        .let { m -> target.testTag?.let(m::testTag) ?: m }
                        .focusRequester(target.focus)
                        .focusProperties {
                            targets.getOrNull(index - 1)?.let { up = it.focus }
                            targets.getOrNull(index + 1)?.let { down = it.focus }
                            right = groupsFocus
                        },
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}
