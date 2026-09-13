package com.johncorser.telly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.johncorser.telly.core.design.AccentPalette
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * The user-selectable accent (Settings -> Appearance -> Color theme),
 * consumed by focus pills, switches, progress fills and section headers.
 * Default is the sampled TiviMate accent #2196F3 (Material Blue 500).
 */
val LocalAccentColor = staticCompositionLocalOf { Color(AccentPalette.DEFAULT.second) }

/** Binds [LocalAccentColor] to the persisted accent setting, live. */
@Composable
fun ProvideAccentColor(
    settings: SettingsRepository,
    content: @Composable (Color) -> Unit,
) {
    val accentName by settings
        .flow(TellySettings.ACCENT_COLOR)
        .collectAsState(initial = TellySettings.ACCENT_COLOR.default)
    val accent = Color(AccentPalette.argbFor(accentName))
    CompositionLocalProvider(LocalAccentColor provides accent) { content(accent) }
}
