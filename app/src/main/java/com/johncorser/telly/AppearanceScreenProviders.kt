package com.johncorser.telly

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import com.johncorser.telly.core.design.LogoStyle
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.core.ui.LocalLogoStyle
import com.johncorser.telly.core.ui.ProvideAppLocale
import com.johncorser.telly.core.ui.ProvideFontScale
import com.johncorser.telly.core.ui.settingState
import com.johncorser.telly.features.guide.GuideStyle
import com.johncorser.telly.features.guide.LocalGuideStyle
import com.johncorser.telly.features.panel.GroupVisibility
import com.johncorser.telly.features.panel.LocalGroupVisibility
import com.johncorser.telly.features.playback.LocalPanelStyle
import com.johncorser.telly.features.playback.PanelStyle

/**
 * Binds every Appearance setting to its CompositionLocal at the theme
 * level (the [com.johncorser.telly.core.ui.ProvideAccentColor] precedent):
 * guide density/transparency/numbers, player panel style, group
 * visibility, logo tiles, font scale and the app language. Every default
 * reproduces telly's current rendering exactly.
 */
@Composable
fun ProvideAppearanceSettings(
    settings: SettingsRepository,
    content: @Composable () -> Unit,
) {
    val guideVisible by settingState(settings, TellySettings.GUIDE_VISIBLE_CHANNELS)
    val guideTransparency by settingState(settings, TellySettings.GUIDE_TRANSPARENCY)
    val channelNumbers by settingState(settings, TellySettings.SHOW_CHANNEL_NUMBERS)
    val panelTransparency by settingState(settings, TellySettings.PLAYER_TRANSPARENCY)
    val showClock by settingState(settings, TellySettings.PLAYER_SHOW_CLOCK)
    val allChannels by settingState(settings, TellySettings.SHOW_ALL_CHANNELS_GROUP)
    val favorites by settingState(settings, TellySettings.SHOW_FAVORITES_GROUP)
    val logoBackground by settingState(settings, TellySettings.LOGO_BACKGROUND)
    val logoRounded by settingState(settings, TellySettings.LOGO_ROUNDED_CORNERS)
    CompositionLocalProvider(
        LocalGuideStyle provides GuideStyle.from(guideVisible, guideTransparency, channelNumbers),
        LocalPanelStyle provides PanelStyle.from(panelTransparency, showClock),
        LocalGroupVisibility provides GroupVisibility(allChannels = allChannels, favorites = favorites),
        LocalLogoStyle provides LogoStyle.from(logoBackground, logoRounded),
    ) {
        ProvideFontScale(settings) {
            ProvideAppLocale(settings, content)
        }
    }
}
