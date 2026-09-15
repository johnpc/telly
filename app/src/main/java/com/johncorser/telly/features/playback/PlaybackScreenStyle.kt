package com.johncorser.telly.features.playback

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Appearance -> Player panel styling (extra transparency + show clock),
 * consumed by the info-overlay scrims and the channel panel. Default =
 * today's sampled alphas with the clock shown; bound at the theme level
 * by ProvideAppearanceSettings.
 */
val LocalPanelStyle = staticCompositionLocalOf { PanelStyle.DEFAULT }
