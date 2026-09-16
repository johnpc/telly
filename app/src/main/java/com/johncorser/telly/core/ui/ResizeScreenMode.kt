package com.johncorser.telly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * The persisted "Resize mode" raw value (store-only key; captured default
 * "Fit"), consumed by the fullscreen playback surface and the multiview
 * panes — the accent-color CompositionLocal precedent. The guide's small
 * preview window deliberately stays letterboxed (Fit).
 */
val LocalResizeModeRaw = staticCompositionLocalOf { TellySettings.RESIZE_MODE.default }

/** Binds [LocalResizeModeRaw] to the persisted setting, live. */
@Composable
fun ProvideResizeMode(
    settings: SettingsRepository,
    content: @Composable () -> Unit,
) {
    val raw by settings
        .flow(TellySettings.RESIZE_MODE)
        .collectAsState(initial = TellySettings.RESIZE_MODE.default)
    CompositionLocalProvider(LocalResizeModeRaw provides raw) { content() }
}
