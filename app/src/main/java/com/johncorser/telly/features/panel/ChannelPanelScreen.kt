package com.johncorser.telly.features.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_CLOCK_BLUE
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND
import com.johncorser.telly.features.playback.LocalPanelStyle
import com.johncorser.telly.features.playlist.db.ChannelEntity

/**
 * Channel-list panel over the dimmed video (captures 25/36/47): groups
 * column left, channel rows right with now-programme + progress; the focused
 * row expands inline into the detail card at its list position (round3 item
 * 13). OK tunes, long-OK opens the channel menu. The scrim lets the video
 * show through like TiviMate's (item 17).
 */
@Composable
fun ChannelPanelScreen(
    panel: PanelViewModel,
    playingChannelId: Long?,
    onTune: (ChannelEntity) -> Unit,
    onChannelMenu: (ChannelEntity) -> Unit,
) {
    val groups by panel.groups.collectAsState()
    val selected by panel.selectedGroup.collectAsState()
    val clockText by panel.clockText.collectAsState()
    val pinPrompt by panel.pinPrompt.collectAsState()
    DisposableEffect(Unit) { onDispose { panel.dismissPinPrompt() } }
    Row(
        Modifier
            .fillMaxSize()
            // Appearance -> Player -> Panels transparency scales today's 0.68 scrim (0% = exactly today).
            .background(Color(TELLY_ONBOARDING_BACKGROUND).copy(alpha = LocalPanelStyle.current.scale(0.68f))),
    ) {
        // Appearance -> Groups: hidden synthetic groups drop out of the list.
        ChannelPanelScreenGroups(LocalGroupVisibility.current.filter(groups), selected, panel::selectGroup)
        Column(Modifier.weight(1f).padding(top = 8.dp, end = 16.dp)) {
            Text(
                text = clockText,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
                color = Color(TELLY_CLOCK_BLUE),
                fontSize = 14.sp,
            )
            ChannelPanelScreenList(panel, playingChannelId, onTune, onChannelMenu)
        }
    }
    if (pinPrompt != null) {
        ChannelPanelScreenPin(onSubmit = panel::submitPin, keyboard = panel.keyboardPin)
    }
}
