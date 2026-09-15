package com.johncorser.telly.features.recording

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.johncorser.telly.core.design.TELLY_TEXT_MUTED
import com.johncorser.telly.core.ui.FocusScreenDefaults
import com.johncorser.telly.core.ui.focusOnAppear

/** The read-only channel line above the form's adjustable rows. */
@Composable
internal fun RecordingScreenFormValue(
    label: String,
    value: String,
) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = label, color = Color(TELLY_TEXT_MUTED), fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        Text(text = value, color = Color.White, fontSize = 15.sp)
    }
}

/** A focusable label+value row: LEFT/RIGHT step the value, OK steps forward. */
@Composable
internal fun RecordingScreenFormStepperRow(
    label: String,
    value: String,
    onAdjust: (Int) -> Unit,
    requestFocus: Boolean = false,
) {
    Surface(
        onClick = { onAdjust(+1) },
        modifier =
            Modifier
                .focusOnAppear(requestFocus)
                .fillMaxWidth()
                .height(40.dp)
                .onPreviewKeyEvent { event -> onStepperKey(event.type, event.key, onAdjust) },
        shape = FocusScreenDefaults.shape(),
        scale = FocusScreenDefaults.scale(),
        colors = FocusScreenDefaults.colors(restingContainer = Color.Transparent),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, fontSize = 15.sp)
            Spacer(Modifier.weight(1f))
            Text(text = value, fontSize = 15.sp)
        }
    }
}

private fun onStepperKey(
    type: KeyEventType,
    key: Key,
    onAdjust: (Int) -> Unit,
): Boolean {
    if (type != KeyEventType.KeyDown) return false
    return when (key) {
        Key.DirectionLeft -> {
            onAdjust(-1)
            true
        }
        Key.DirectionRight -> {
            onAdjust(+1)
            true
        }
        else -> false
    }
}
