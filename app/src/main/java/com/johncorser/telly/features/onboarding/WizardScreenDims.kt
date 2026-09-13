package com.johncorser.telly.features.onboarding

import androidx.compose.ui.unit.dp

/**
 * Wizard geometry from the 1920x1080 uidumps (2 px = 1 dp): guidance pane
 * 876 px, action rows 595x88 px starting at y=378, button column 304 px wide.
 */
internal object WizardScreenDims {
    val guidanceWidth = 438.dp
    val paneTop = 189.dp
    val panePadding = 20.dp
    val rowSpacing = 8.dp
    val actionWidth = 298.dp
    val actionHeight = 44.dp
    val buttonWidth = 112.dp
    val buttonPaneWidth = 151.dp
}
