package com.johncorser.telly.features.playback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_OVERLAY_CARD
import com.johncorser.telly.core.ui.FocusScreenDefaults

/** "TV guide" + "History" shortcut cards; TV guide takes focus (capture 34). */
@Composable
internal fun PlaybackScreenCards(
    onGuide: () -> Unit,
    onHistory: () -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }
    Row(
        Modifier.padding(start = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PlaybackScreenCard(
            label = stringResource(R.string.playback_card_tv_guide),
            icon = R.drawable.ic_card_guide,
            onClick = onGuide,
            modifier = Modifier.focusRequester(firstFocus),
        )
        PlaybackScreenCard(
            label = stringResource(R.string.playback_card_history),
            icon = R.drawable.ic_card_history,
            onClick = onHistory,
        )
    }
}

@Composable
private fun PlaybackScreenCard(
    label: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(width = 140.dp, height = 103.dp),
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = Color(TELLY_OVERLAY_CARD)),
    ) {
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
