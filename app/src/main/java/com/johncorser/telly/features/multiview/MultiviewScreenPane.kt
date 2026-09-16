package com.johncorser.telly.features.multiview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_ERROR_TEXT
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.LocalResizeModeRaw
import com.johncorser.telly.core.ui.TellyScreenWhiteText
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.player.PlayerScreenSurface
import com.johncorser.telly.features.player.PlayerState
import com.johncorser.telly.features.player.ResizeModes
import com.johncorser.telly.features.playlist.db.displayName

/**
 * One 16:9 pane of the multiview grid: its own engine's video letterboxed
 * inside the cell, a white focus border (the focused pane owns audio) and
 * per-pane error text when the stream fails. Semantics carry the pane's
 * channel + audio state for the acceptance suite (no actual audio asserts).
 */
@Composable
internal fun MultiviewScreenPane(
    pane: MultiviewPane,
    focused: Boolean,
    onFocused: () -> Unit,
    onOk: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val state by pane.engine.state.collectAsState()
    val audioState = if (focused) "audio" else "muted"
    Surface(
        onClick = onOk,
        modifier =
            modifier
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onFocused() }
                .testTag("multiview-pane")
                .semantics { contentDescription = "Screen ${pane.id}: ${pane.channel.displayName}, $audioState" },
        // Slightly rounded pane corners, same radius as the pane menu (round8 P3).
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        border =
            ClickableSurfaceDefaults.border(
                focusedBorder = Border(BorderStroke(2.dp, Color.White)),
            ),
        colors =
            ClickableSurfaceDefaults.colors(
                containerColor = Color.Black,
                focusedContainerColor = Color.Black,
                pressedContainerColor = Color.Black,
            ),
    ) {
        Box(Modifier.fillMaxSize()) {
            (pane.engine as? Media3PlayerEngine)?.let {
                // The panes honor the persisted resize mode like fullscreen
                // playback does (default Fit = the previous hardcoded value).
                PlayerScreenSurface(it, Modifier.fillMaxSize(), resizeMode = ResizeModes.of(LocalResizeModeRaw.current))
            }
            (state as? PlayerState.Error)?.let { MultiviewScreenPaneError(pane.channel.displayName, it.message) }
        }
    }
}

/** Per-pane stream failure, in the app's error text treatment. */
@Composable
private fun MultiviewScreenPaneError(
    channelName: String,
    message: String,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TellyScreenWhiteText(channelName, fontSize = 15.sp)
            Text(
                text = "Playback error — $message",
                color = Color(TELLY_ERROR_TEXT),
                fontSize = 13.sp,
            )
        }
    }
}
