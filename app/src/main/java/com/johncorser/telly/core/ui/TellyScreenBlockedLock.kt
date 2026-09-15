package com.johncorser.telly.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import com.johncorser.telly.R
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED

/**
 * The small padlock marking a blocked channel in the guide and panel
 * channel lists (ux-spec: blocked channels stay listed with a lock and
 * PIN-gate tuning).
 */
@Composable
fun TellyScreenBlockedLock(
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
) {
    Icon(
        painter = painterResource(R.drawable.ic_settings_lock),
        contentDescription = "Blocked",
        modifier = modifier.size(size),
        tint = Color(TELLY_TEXT_MUTED),
    )
}
