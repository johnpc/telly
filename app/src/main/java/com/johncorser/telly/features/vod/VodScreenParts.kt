package com.johncorser.telly.features.vod

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.core.ui.TellyScreenWhiteText

/** The browser's "Movies" heading (History-header idiom, left-aligned). */
@Composable
internal fun VodScreenTitle() {
    Box(Modifier.padding(start = 40.dp, top = 20.dp)) {
        TellyScreenWhiteText(text = stringResource(R.string.vod_title), fontSize = 28.sp)
    }
}

/** Centered empty state when the playlists carry no VOD entries. */
@Composable
internal fun VodScreenEmpty(modifier: Modifier = Modifier) {
    Box(modifier) {
        TellyScreenMutedText(text = stringResource(R.string.vod_empty), fontSize = 18.sp)
    }
}
