package com.johncorser.telly.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.johncorser.telly.core.design.LogoStyle

/**
 * Appearance -> Logos, consumed by [TellyScreenLogoTile] everywhere a
 * channel logo renders. Default = today's #2C5F8A 4 dp-rounded tile;
 * bound at the theme level by ProvideAppearanceSettings.
 */
val LocalLogoStyle = staticCompositionLocalOf { LogoStyle.DEFAULT }
