package com.johncorser.telly.features.onboarding

import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Wizard geometry from the 1920x1080 reference captures (2 px = 1 dp): the
 * painted guidance pane is 940 px wide (the uidump RelativeLayout stops at
 * 876 px, but the paint extends to 940 — see punch-list item 2), action rows
 * are 595x88 px starting at y=378, and the button column is 304 px wide.
 */
internal object WizardScreenDims {
    val guidanceWidth = 470.dp
    val paneTop = 189.dp
    val panePadding = 20.dp
    val rowSpacing = 8.dp
    val actionWidth = 298.dp
    val actionHeight = 44.dp
    val buttonWidth = 112.dp
    val buttonPaneWidth = 152.dp

    /** Inline editor (reference 08/09): expanded field area below the label.
     * The EditText value renders at 12 sp with 6 dp above / 2.5 dp below so
     * its glyphs and our underline land on the reference rows exactly. */
    val editorWidth = 270.dp
    val editorUnderlineInset = 4.dp
    const val EDITOR_TEXT_SIZE_SP = 12f
    const val EDITOR_TEXT_PAD_TOP_DP = 6f
    const val EDITOR_TEXT_PAD_BOTTOM_DP = 2.5f

    /** Leanback guided-action labels render in Roboto Condensed (measured:
     * "Xtream Codes" is 153 px in the reference vs 175 px in regular). */
    val actionFontFamily = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed")))
}
