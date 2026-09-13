package com.johncorser.telly.features.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.design.TELLY_ONBOARDING_BACKGROUND

/**
 * The two-pane settings shell: left column lists the captured sections
 * (focus drives the right pane), right pane renders the focused/pushed
 * pane's rows. Panel palette and geometry from uidump 18.
 */
@Composable
fun SettingsScreen(model: SettingsViewModel) {
    val state by model.state.collectAsState()
    val rows by model.rows.collectAsState()
    val playlists by model.playlistItems.collectAsState()
    BackHandler(enabled = state.consumesBack) { model.back() }
    Row(
        Modifier
            .fillMaxSize()
            .background(Color(TELLY_GUIDANCE_PANE)),
    ) {
        SettingsScreenPane(title = "Settings", modifier = Modifier.width(SettingsScreenDims.leftPaneWidth)) {
            SettingsScreenSectionList(model = model)
        }
        Box(
            Modifier
                .width(2.dp)
                .fillMaxSize()
                .background(Color(TELLY_ONBOARDING_BACKGROUND)),
        )
        SettingsScreenPane(title = paneTitle(state.activePane, playlists)) {
            SettingsScreenRows(rows = rows, onActivate = model::activate)
        }
    }
    SettingsScreenOverlay(model = model, overlay = state.overlay)
}

/** Premium note + Unlock Premium + the nine sections, as captured. */
@Composable
private fun SettingsScreenSectionList(model: SettingsViewModel) {
    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
        item { SettingsScreenStaticRow(SettingsRow.Note(PREMIUM_NOTE)) }
        item { SettingsScreenRow(row = unlockPremiumRow(), onActivate = model::activate) }
        items(SettingsSection.entries, key = { it.name }) { section ->
            SettingsScreenRow(
                row = SettingsRow.Value(id = "section:${section.name}", title = section.title),
                onActivate = { model.selectSection(section) },
                onFocused = { model.selectSection(section) },
            )
        }
    }
}
