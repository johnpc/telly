package com.johncorser.telly.features.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.johncorser.telly.core.ui.FocusScreenReclaim
import com.johncorser.telly.core.ui.FocusScreenReclaimEffect

/**
 * Shared inline-edit state for wizard rows: tracks whether the editor is
 * open and, when it closes, returns focus to the row — or jumps to Next
 * after an ENTER commit, matching reference screen 10. Both hand-offs are
 * placement-gated [FocusScreenReclaim] grabs: the row remounts the very frame
 * the editor closes, and a raw requestFocus there can fire before the
 * fresh node is placed.
 */
class WizardScreenEditState internal constructor() {
    val rowFocus = FocusScreenReclaim()
    val nextFocus = FocusScreenReclaim()
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
    FocusScreenReclaimEffect(state.rowFocus)
    FocusScreenReclaimEffect(state.nextFocus)
    LaunchedEffect(state.editing) {
        if (!state.editing) {
            if (state.committed && canFocusNext()) {
                state.nextFocus.reclaim()
            } else {
                state.rowFocus.reclaim()
            }
            state.committed = false
        }
    }
    return state
}
