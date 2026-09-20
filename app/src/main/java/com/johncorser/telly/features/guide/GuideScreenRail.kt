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
import com.johncorser.telly.core.design.TELLY_TEXT_PRIMARY

/** One live rail icon: what it opens plus its focus node in the chain. */
private class RailTarget(
    val icon: Int,
    val onClick: () -> Unit,
    val focus: FocusRequester,
    val contentDescription: String? = null,
    val testTag: String? = null,
    val restingTint: Color? = null,
)

/**
 * The 56 dp nav rail at the far left of the guide+groups view (capture
 * 25): logo on top, search / live-TV / My-list / Movies / DVR icons
 * mid-rail with the live-TV section lit white (it IS the current section),
 * settings gear at the bottom. Every icon is a live target (LEFT from the
 * groups column reaches the gear, OK opens Search / My List / Movies /
 * Recordings / the settings sheet; OK on the live-TV icon returns focus to
 * the guide, mirroring RIGHT; UP/DOWN traverse search ↔ tv ↔ bookmark ↔
 * Movies ↔ DVR ↔ gear and RIGHT returns to the groups column).
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
    val liveTvFocus = remember { FocusRequester() }
    val bookmarkFocus = remember { FocusRequester() }
    val moviesFocus = remember { FocusRequester() }
    val dvrFocus = remember { FocusRequester() }
    val targets =
        listOf(
            RailTarget(R.drawable.ic_menu_search, onOpenSearch, searchFocus),
            RailTarget(
                R.drawable.ic_rail_tv,
                { groupsFocus.requestFocus() },
                liveTvFocus,
                "Live TV",
                restingTint = Color.White,
            ),
            RailTarget(R.drawable.ic_rail_bookmark, onOpenMyList, bookmarkFocus, "My list"),
            RailTarget(R.drawable.ic_rail_movie, onOpenVod, moviesFocus, testTag = "rail-movies"),
            RailTarget(R.drawable.ic_rail_dvr, onOpenRecordings, dvrFocus, "Recordings"),
            RailTarget(R.drawable.ic_menu_settings, onOpenSettings, gearFocus, "Settings"),
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
            // The gear parks at the very bottom, below the flexible gap.
            val gear = index == targets.lastIndex
            if (gear) Spacer(Modifier.weight(1f))
            GuideScreenRailButton(
                icon = target.icon,
                onClick = target.onClick,
                contentDescription = target.contentDescription,
                restingTint = target.restingTint ?: Color(TELLY_TEXT_PRIMARY),
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
