package com.johncorser.telly.features.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester

/**
 * Shared inline-edit state for wizard rows: tracks whether the editor is
 * open and, when it closes, returns focus to the row — or jumps to Next
 * after an ENTER commit, matching reference screen 10.
 */
class WizardScreenEditState internal constructor() {
    val rowFocus = FocusRequester()
    val nextFocus = FocusRequester()
    var editing by mutableStateOf(false)
        private set
    internal var committed by mutableStateOf(false)

    fun open() {
        editing = true
    }

    fun commit() {
        committed = true
        editing = false
    }
}

/** Remembers the edit state and drives the focus hand-off on editor close. */
@Composable
fun rememberWizardScreenEditState(canFocusNext: () -> Boolean): WizardScreenEditState {
    val state = remember { WizardScreenEditState() }
    LaunchedEffect(state.editing) {
        if (!state.editing) {
            if (state.committed && canFocusNext()) {
                state.nextFocus.requestFocus()
            } else {
                state.rowFocus.requestFocus()
            }
            state.committed = false
        }
    }
    return state
}
