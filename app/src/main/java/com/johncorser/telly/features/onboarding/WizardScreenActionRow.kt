package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_FOCUS_TEXT
import com.johncorser.telly.core.design.TELLY_TEXT_FAINT
import com.johncorser.telly.core.design.TELLY_TEXT_FAINT_FOCUSED
import com.johncorser.telly.core.ui.FocusScreenDefaults

/**
 * A leanback-style guided action: transparent at rest, light pill when
 * focused. Metrics from the reference uidumps: 44 dp single-line rows,
 * 62.5 dp two-line rows, 14 sp titles over 12 sp descriptions. Disabled
 * actions are skipped when focusing, as leanback does.
 */
@Composable
fun WizardScreenActionRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondaryText: String? = null,
    leadingIcon: Painter? = null,
    trailingIcon: Painter? = null,
) {
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .heightIn(min = WizardScreenDims.actionHeight)
                .focusProperties { canFocus = enabled },
        enabled = enabled,
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = Color.Transparent),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                Icon(painter = it, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    letterSpacing = 0.sp,
                    fontFamily = WizardScreenDims.actionFontFamily,
                )
                secondaryText?.let {
                    Spacer(Modifier.height(4.5.dp))
                    // Reference: #5D5F61 at rest, #6F7071 on the focused pill.
                    val focused = LocalContentColor.current == Color(TELLY_FOCUS_TEXT)
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        letterSpacing = 0.sp,
                        fontFamily = WizardScreenDims.actionFontFamily,
                        color = Color(if (focused) TELLY_TEXT_FAINT_FOCUSED else TELLY_TEXT_FAINT),
                        modifier = Modifier.padding(bottom = 2.5.dp),
                    )
                }
            }
            trailingIcon?.let {
                Spacer(Modifier.width(4.dp))
                Icon(painter = it, contentDescription = null, modifier = Modifier.size(12.dp))
            }
        }
    }
}
