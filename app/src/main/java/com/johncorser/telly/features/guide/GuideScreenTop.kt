package com.johncorser.telly.features.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.player.PlayerScreenSurface

/**
 * Top strip of the guide (uidump 24): a 320×180 dp live preview window at
 * the left, the focused programme's info pane at the right.
 */
@Composable
internal fun GuideScreenTop(
    controller: GuideController,
    engine: Media3PlayerEngine,
) {
    val info by controller.info.collectAsState()
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 12.dp)
            .height(180.dp),
    ) {
        PlayerScreenSurface(
            engine = engine,
            modifier =
                Modifier
                    .size(width = 320.dp, height = 180.dp)
                    .background(Color.Black),
        )
        Spacer(Modifier.width(22.dp))
        GuideScreenInfoPane(info, Modifier.weight(1f))
    }
}
