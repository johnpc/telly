package com.johncorser.telly.features.vod

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.TellyScreenLogoTile
import com.johncorser.telly.core.ui.TellyScreenProgressBar
import com.johncorser.telly.core.ui.focusOnAppear

/**
 * One Movies-browser card: tvg-logo art over the title, plus a thin
 * progress line while a resume position is stored ("Continue watching").
 */
@Composable
internal fun VodScreenCard(
    card: VodCard,
    onClick: () -> Unit,
    requestFocus: Boolean,
    grabYielded: () -> Boolean,
) {
    Surface(
        onClick = onClick,
        modifier =
            Modifier
                .testTag("vod-card")
                .width(156.dp)
                .focusOnAppear(requestFocus, grabYielded),
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = Color.Transparent),
    ) {
        Column(Modifier.padding(6.dp)) {
            TellyScreenLogoTile(
                logoUrl = card.item.logoUrl,
                name = card.item.name,
                size = 84.dp,
                width = 144.dp,
            )
            Text(
                text = card.item.name,
                modifier = Modifier.padding(top = 6.dp),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            card.progressPermille?.let { permille ->
                TellyScreenProgressBar(
                    permille = permille,
                    modifier =
                        Modifier
                            .testTag("vod-card-progress")
                            .fillMaxWidth()
                            .padding(top = 5.dp)
                            .height(2.dp),
                )
            }
        }
    }
}
