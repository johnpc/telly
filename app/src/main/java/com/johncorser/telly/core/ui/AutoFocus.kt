package com.johncorser.telly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester

/**
 * A [FocusRequester] that grabs focus when its composable first appears —
 * the "first row/icon takes focus" pattern every TiviMate surface follows.
 */
@Composable
fun rememberAutoFocus(): FocusRequester {
    val requester = remember { FocusRequester() }
    LaunchedEffect(Unit) { requester.requestFocus() }
    return requester
}
