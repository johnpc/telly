package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * Playback pane rows (catalogue 60–62). Buffer size and the decoder picks
 * are live (the player slice reads them); AFR, external player and Skip
 * steps have no telly behavior yet and stay locked like the reference.
 */
fun playbackRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Value(
            id = RowIds.PLAYBACK_BUFFER_SIZE,
            title = "Buffer size",
            summary = s.get(TellySettings.BUFFER_SIZE),
        ),
        SettingsRow.Value(
            id = RowIds.PLAYBACK_AUDIO_DECODER,
            title = "Audio decoder",
            summary = s.get(TellySettings.AUDIO_DECODER),
        ),
        SettingsRow.Value(
            id = RowIds.PLAYBACK_VIDEO_DECODER,
            title = "Video decoder",
            summary = s.get(TellySettings.VIDEO_DECODER),
        ),
        SettingsRow.Value(
            id = RowIds.PLAYBACK_AFR,
            title = "Auto frame rate (AFR)",
            summary = s.get(TellySettings.AUTO_FRAME_RATE),
            locked = true,
        ),
        SettingsRow.Toggle(
            id = RowIds.PLAYBACK_SURROUND,
            title = "Select surround audio track by default",
            checked = s.get(TellySettings.SURROUND_BY_DEFAULT),
        ),
        SettingsRow.Toggle(
            id = RowIds.PLAYBACK_PASSTHROUGH,
            title = "Audio passthrough",
            checked = s.get(TellySettings.AUDIO_PASSTHROUGH),
        ),
        SettingsRow.Value(
            id = RowIds.PLAYBACK_EXTERNAL_PLAYER,
            title = "Use external player",
            summary = s.get(TellySettings.USE_EXTERNAL_PLAYER),
            locked = true,
        ),
        SettingsRow.Value(id = RowIds.PLAYBACK_SKIP_STEPS, title = "Skip steps", locked = true),
    )
