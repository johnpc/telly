package com.johncorser.telly.features.panel

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.johncorser.telly.core.design.TELLY_GUIDANCE_PANE
import com.johncorser.telly.core.ui.TellyScreenProgramTitle
import com.johncorser.telly.features.settings.SettingsScreenPinEntry

/**
 * The parental prompt over the panel when a locked group is selected —
 * also reused by the blocked-channel gates: centered card with the shared
 * PIN entry (wheels or masked keyboard per the persisted "PIN input
 * method"). The reference dialog is premium-locked and uncapturable —
 * minimal matching style. A non-null [onDismiss] lets BACK cancel.
 */
@Composable
internal fun ChannelPanelScreenPin(
    onSubmit: (String) -> Unit,
    title: String = "Enter PIN",
    onDismiss: (() -> Unit)? = null,
    keyboard: Boolean = false,
) {
    onDismiss?.let { dismiss -> BackHandler { dismiss() } }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(PIN_SCRIM)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .background(Color(TELLY_GUIDANCE_PANE), RoundedCornerShape(4.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TellyScreenProgramTitle(title)
            SettingsScreenPinEntry(keyboard = keyboard, onSubmit = onSubmit)
        }
    }
}

private const val PIN_SCRIM = 0xB3000000
