package com.johncorser.telly.features.playback

import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.StateFlow

/**
 * The screen-facing half of the blocked-channel tune gate: exposes the
 * pending prompt, commits PIN entries (a verified PIN releases the pending
 * tune and fires [onUnlocked]) and cancels (never tunes, fires
 * [onDismissed] so the host can restore focus).
 */
class TuneBlockPrompt(
    private val tuner: TuneController,
    private val onUnlocked: () -> Unit = {},
    private val onDismissed: () -> Unit = {},
) {
    /** The blocked channel awaiting the PIN, or null when no prompt is open. */
    val channel: StateFlow<ChannelEntity?> = tuner.gate.pinPrompt

    /** Wrong PINs keep prompting; the right one tunes the pending channel. */
    fun submit(pin: String) {
        tuner.gate.unlock(pin)?.let {
            tuner.tune(it)
            onUnlocked()
        }
    }

    fun dismiss() {
        tuner.gate.dismiss()
        onDismissed()
    }
}
