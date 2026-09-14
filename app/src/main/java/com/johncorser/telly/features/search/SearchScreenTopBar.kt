package com.johncorser.telly.features.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceColors
import androidx.tv.material3.ClickableSurfaceDefaults
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_FOCUS_FILL
import com.johncorser.telly.core.ui.TellyScreenIconCircle
import com.johncorser.telly.core.ui.rememberAutoFocus
import com.johncorser.telly.features.search.SearchScreenDims as Dims

/**
 * Search top bar (capture 49): the voice orb takes default focus, RIGHT
 * reaches the light-grey query bar (system IME on device), gear at the far
 * right. Voice input and the gear's premium search settings are placeholders.
 */
@Composable
internal fun SearchScreenTopBar(viewModel: SearchViewModel) {
    val query by viewModel.query.collectAsState()
    val orbFocus = rememberAutoFocus()
    val voiceLabel = stringResource(R.string.search_voice)
    val settingsLabel = stringResource(R.string.search_settings)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = Dims.orbStart, top = Dims.topBarTop, end = Dims.edgePad),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TellyScreenIconCircle(
            icon = R.drawable.ic_search_mic,
            onClick = { viewModel.showComingSoon(voiceLabel) },
            modifier = Modifier.focusRequester(orbFocus),
            size = Dims.orbSize,
            iconSize = 24.dp,
            colors = orbColors(),
        )
        Spacer(Modifier.width(Dims.barGap))
        SearchScreenQueryField(query, viewModel, Modifier.width(Dims.barWidth))
        Spacer(Modifier.weight(1f))
        TellyScreenIconCircle(
            icon = R.drawable.ic_menu_settings,
            onClick = { viewModel.showComingSoon(settingsLabel) },
            size = Dims.gearSize,
        )
    }
}

/** The orb stays a light disc even unfocused (captures 49/50). */
@Composable
private fun orbColors(): ClickableSurfaceColors =
    ClickableSurfaceDefaults.colors(
        containerColor = Color(TELLY_FOCUS_FILL),
        contentColor = Color.Black,
        focusedContainerColor = Color.White,
        focusedContentColor = Color.Black,
        pressedContainerColor = Color.White,
        pressedContentColor = Color.Black,
        disabledContainerColor = Color(TELLY_FOCUS_FILL),
        disabledContentColor = Color.Black,
    )
