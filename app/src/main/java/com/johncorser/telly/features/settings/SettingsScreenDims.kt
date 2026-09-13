package com.johncorser.telly.features.settings

import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Settings geometry from the 1920x1080 uidumps (2 px = 1 dp): panel width
 * 720 px = 360 dp, header strip 144 px = 72 dp with a 34 px = 17 sp title,
 * rows on a 90 px = 45 dp pitch with 48 px = 24 dp icons at a 48 px inset,
 * titles 38 px line boxes (14 sp / 19 sp Roboto Condensed, same as the
 * wizard rows), summaries 33 px boxes (12 sp), switches 94x54 px = 47x27 dp.
 */
internal object SettingsScreenDims {
    val leftPaneWidth = 360.dp
    val headerHeight = 72.dp
    val panePadding = 24.dp
    val rowMinHeight = 45.dp
    val iconSize = 24.dp
    val iconTextGap = 16.dp
    val switchWidth = 47.dp
    val switchHeight = 27.dp
    val switchTrackWidth = 34.dp
    val switchTrackHeight = 14.dp
    val switchThumb = 20.dp

    val headerTitleSize = 17.sp
    val titleSize = 14.sp
    val titleLineHeight = 19.sp
    val summarySize = 12.sp
    val noteSize = 12.sp

    /** Settings rows render in Roboto Condensed like the wizard's actions. */
    val fontFamily = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed")))
}
