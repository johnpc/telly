package com.johncorser.telly.features.guide

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Appearance -> TV guide, live over the whole app (the guide keeps
 * rendering under the settings sheet, so changes apply as they are made).
 * The default is exactly today's guide rendering; bound at the theme
 * level by ProvideAppearanceSettings.
 */
val LocalGuideStyle = staticCompositionLocalOf { GuideStyle.DEFAULT }
