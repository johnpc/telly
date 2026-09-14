package com.johncorser.telly.features.panel

import com.johncorser.telly.core.settings.ParentalControls
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Parental gate for the panel's groups column: selecting a locked group
 * opens a PIN prompt instead of switching, and only a verified PIN releases
 * the pending group. Pure logic; the wheel dialog lives in the UI layer.
 */
class PanelLock(
    private val parental: ParentalControls? = null,
) {
    private val pending = MutableStateFlow<String?>(null)

    /** The group awaiting a PIN, or null when no prompt is open. */
    val pinPrompt: StateFlow<String?> = pending.asStateFlow()

    /** True when [group] is gated; the prompt opens instead of switching. */
    fun intercept(group: String): Boolean {
        val locked = parental?.isGroupLocked(group) == true
        pending.value = if (locked) group else null
        return locked
    }

    /** The pending group when [pin] verifies; keeps prompting otherwise. */
    fun unlock(pin: String): String? {
        val group = pending.value ?: return null
        if (parental?.verifyPin(pin) != true) return null
        pending.value = null
        return group
    }

    fun dismiss() {
        pending.value = null
    }
}
