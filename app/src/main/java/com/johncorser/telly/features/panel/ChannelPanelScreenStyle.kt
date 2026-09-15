package com.johncorser.telly.features.panel

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Appearance -> Groups, consumed by the guide and panel group columns.
 * Default shows both synthetic groups = today's rendering; bound at the
 * theme level by ProvideAppearanceSettings.
 */
val LocalGroupVisibility = staticCompositionLocalOf { GroupVisibility.DEFAULT }
