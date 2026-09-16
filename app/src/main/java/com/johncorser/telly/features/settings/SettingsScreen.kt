package com.johncorser.telly.features.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties

/**
 * The settings surface, rebuilt to the device-verified model (2026-09-13):
 * ONE 360 dp right sheet over the dimmed underlying screen. The root sheet
 * lists the sections; OK replaces it in place with the section's sheet;
 * BACK pops one sheet, and from the root [onClose] leaves settings. Focus
 * lands on the first focusable row of a fresh sheet and is restored to the
 * row you came from when popping back, like the reference.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    model: SettingsViewModel,
    onClose: () -> Unit,
) {
    val state by model.state.collectAsState()
    val rows by model.rows.collectAsState()
    val playlists by model.playlistItems.collectAsState()
    val epgSources by model.epgSourceItems.collectAsState()
    BackHandler(enabled = true) { if (!model.back()) onClose() }
    val focusMemory = remember { mutableMapOf<SettingsPane?, String>() }
    // One trap around the whole surface: focus roams freely between the
    // sheet and any overlay, but never crosses to the dimmed underlay —
    // BACK is the only way out (device-verified).
    // EVERY overlay owns the screen: the picker / text-edit overlays are
    // themselves 360 dp sheets ON the section sheet, the GuidedStep
    // confirms are fullscreen and the PIN dialogs are centered cards —
    // rendering the section sheet underneath any of them keeps a second
    // focusable subtree that the overlay's initial-focus grab loses to
    // (on-device: the reminders delete-confirm pill could never take
    // focus from the pane's still-composed rows), so the sheet is
    // dropped whenever an overlay is up and its focus memory restores
    // the row when the overlay closes.
    Box(Modifier.fillMaxSize().focusProperties { exit = { FocusRequester.Cancel } }) {
        if (state.overlay == null) {
            // Pushing/popping a section cross-fades the sheet content in
            // place over ~300 ms while the frame stays static (settings
            // punch list: frame-scan of the reference screenrecord). The
            // pane+rows pair keeps the outgoing sheet rendering its own
            // rows; only pane changes animate.
            AnimatedContent(
                targetState = state.activePane to rows,
                transitionSpec = { fadeIn(tween(PANE_FADE_MS)) togetherWith fadeOut(tween(PANE_FADE_MS)) },
                contentKey = { it.first },
            ) { (pane, paneRows) ->
                SettingsScreenSheet(title = paneTitle(pane, playlists, epgSources)) {
                    SettingsScreenRows(
                        rows = paneRows,
                        onActivate = model::activate,
                        initialFocusId = focusMemory[pane] ?: paneRows.firstFocusableId(),
                        onRowFocused = { focusMemory[pane] = it },
                    )
                }
            }
        }
        SettingsScreenOverlay(model = model, overlay = state.overlay)
    }
}

private const val PANE_FADE_MS = 300

/** The row a fresh sheet should focus: its first focusable row. */
internal fun List<SettingsRow>.firstFocusableId(): String? =
    firstOrNull { it !is SettingsRow.Header && it !is SettingsRow.Note && !it.isLocked() }?.id
