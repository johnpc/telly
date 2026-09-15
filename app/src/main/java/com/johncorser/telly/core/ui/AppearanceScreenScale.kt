package com.johncorser.telly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.johncorser.telly.core.design.FontScale
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.core.settings.withAppLocale

/**
 * Appearance -> Font size: a theme-level fontScale multiplier over the
 * device density ("Medium" = exactly 1.0 = today's rendering).
 */
@Composable
fun ProvideFontScale(
    settings: SettingsRepository,
    content: @Composable () -> Unit,
) {
    val label by settingState(settings, TellySettings.FONT_SIZE)
    val device = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(device.density, device.fontScale * FontScale.factorFor(label)),
        content = content,
    )
}

/**
 * Appearance -> Language, applied LIVE: string resources resolve through
 * a locale-overridden context (no activity recreate needed). "System"
 * leaves the composition untouched. MainActivity's base context applies
 * the same override for anything resolved outside composition.
 */
@Composable
fun ProvideAppLocale(
    settings: SettingsRepository,
    content: @Composable () -> Unit,
) {
    val language by settingState(settings, TellySettings.LANGUAGE)
    val base = LocalContext.current
    val localized = remember(base, language) { base.withAppLocale(language) }
    if (localized === base) {
        content()
    } else {
        CompositionLocalProvider(
            LocalContext provides localized,
            LocalConfiguration provides localized.resources.configuration,
            content = content,
        )
    }
}
