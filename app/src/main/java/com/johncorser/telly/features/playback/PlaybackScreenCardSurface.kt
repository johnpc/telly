package com.johncorser.telly.features.playback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_OVERLAY_CARD
import com.johncorser.telly.core.design.TELLY_OVERLAY_CARD_FOCUSED
import com.johncorser.telly.core.ui.FocusScreenDefaults

/**
 * One 140×103 shortcut-row tile. Focused = lighter dark-grey fill with
 * WHITE content, not the app-wide white pill (round3 item 8: #252A2D
 * focused / #181E20 resting).
 */
@Composable
internal fun PlaybackScreenCardSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(width = 140.dp, height = 103.dp),
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors =
            ClickableSurfaceDefaults.colors(
                containerColor = Color(TELLY_OVERLAY_CARD),
                contentColor = Color.White,
                focusedContainerColor = Color(TELLY_OVERLAY_CARD_FOCUSED),
                focusedContentColor = Color.White,
                pressedContainerColor = Color(TELLY_OVERLAY_CARD_FOCUSED),
                pressedContentColor = Color.White,
            ),
    ) {
        content()
    }
}

/** The icon + label tile variant (TV guide / History / Clear). */
@Composable
internal fun PlaybackScreenCard(
    label: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaybackScreenCardSurface(onClick = onClick, modifier = modifier) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(10.dp))
            Text(text = label, fontSize = 15.sp)
        }
    }
}
