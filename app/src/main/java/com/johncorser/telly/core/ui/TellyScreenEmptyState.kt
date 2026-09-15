package com.johncorser.telly.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

/**
 * The centered muted "empty list" message shared by the full-screen list
 * surfaces (History's "No history", Recordings' "No recordings"). One
 * primitive so the empty-state chrome is never duplicated (jscpd 0).
 */
@Composable
fun TellyScreenEmptyState(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TellyScreenMutedText(text = text, fontSize = 18.sp)
    }
}
