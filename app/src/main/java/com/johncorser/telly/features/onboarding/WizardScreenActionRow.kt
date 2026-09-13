package com.johncorser.telly.features.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.ui.FocusScreenDefaults

/**
 * A leanback-style guided action: transparent at rest, light pill when
 * focused. Used for wizard rows and the Next/Back/Cancel buttons.
 */
@Composable
fun WizardScreenActionRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondaryText: String? = null,
    trailingIcon: Painter? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = WizardScreenDims.actionHeight),
        enabled = enabled,
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = Color.Transparent),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = text, fontSize = 16.sp)
                secondaryText?.let {
                    Text(text = it, fontSize = 16.sp, color = LocalContentColor.current.copy(alpha = 0.55f))
                }
            }
            trailingIcon?.let {
                Spacer(Modifier.width(8.dp))
                Icon(painter = it, contentDescription = null, modifier = Modifier.size(12.dp))
            }
        }
    }
}
