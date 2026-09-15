package com.johncorser.telly.features.guide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.johncorser.telly.features.playback.PlaybackScreenMenuScrim
import kotlinx.coroutines.delay

/** The scrim's ~300 ms fade-out still needs the hole after the sheet cuts. */
private const val SCRIM_HOLE_LINGER_MS = 300L

/**
 * The row-sheet backdrop scrim with the originating row punched out at
 * full brightness (round7 P2): TiviMate dims every grid row EXCEPT the
 * long-OK row, whose white focus outline stays visible under the sheet.
 * The hole follows the saved channel's CURRENT row and lingers through
 * the scrim's fade-out so the row never dims on close either.
 */
@Composable
internal fun GuideScreenMenuScrim(
    controller: GuideController,
    layer: GuideLayer,
) {
    val rows by controller.rows.collectAsState()
    val firstRow by controller.firstVisibleRow.collectAsState()
    val holeActive = rememberScrimHoleActive(layer)
    val bandTop =
        if (holeActive) GuideDimExemption.bandTopDp(controller.menu.sheetChannelId, rows, firstRow) else null
    Box(
        Modifier
            .fillMaxSize()
            .guideScrimRowHole(bandTop),
    ) {
        PlaybackScreenMenuScrim(visible = layer == GuideLayer.RowMenu)
    }
}

/** True while the sheet scrim is up or still fading back out. */
@Composable
private fun rememberScrimHoleActive(layer: GuideLayer): Boolean {
    var active by remember { mutableStateOf(false) }
    LaunchedEffect(layer) {
        if (layer == GuideLayer.RowMenu) {
            active = true
        } else if (active) {
            delay(SCRIM_HOLE_LINGER_MS)
            active = false
        }
    }
    return active
}

/** Clears the undimmed row band out of everything drawn inside. */
private fun Modifier.guideScrimRowHole(bandTopDp: Float?): Modifier {
    if (bandTopDp == null) return this
    return graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, bandTopDp.dp.toPx()),
                size = Size(size.width, GuideGeometry.ROW_HEIGHT_DP.dp.toPx()),
                blendMode = BlendMode.Clear,
            )
        }
}
