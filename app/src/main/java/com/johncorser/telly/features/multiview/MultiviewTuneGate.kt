package com.johncorser.telly.features.multiview

import com.johncorser.telly.features.playback.BlockGate
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.StateFlow

/**
 * The blocked-channel PIN gate over EVERY multiview tune path (entry pane,
 * picker pick, CH+/- zap): a gated tune stashes its pane action behind the
 * shared [BlockGate] prompt, the verified PIN replays it exactly once, and
 * a wrong or cancelled PIN never tunes. The shared BlockSession relock
 * semantics ("Until app restart") ride along inside the gate.
 */
class MultiviewTuneGate(
    private val gate: BlockGate,
) {
    private var pending: ((ChannelEntity) -> Unit)? = null

    /** The blocked channel awaiting a PIN (drives the PIN card layer). */
    val pinPrompt: StateFlow<ChannelEntity?> = gate.pinPrompt

    val promptOpen: Boolean get() = gate.pinPrompt.value != null

    /** The persisted "PIN input method" choice, honored by the PIN card. */
    val keyboardPin: Boolean get() = gate.keyboardPin

    /** Runs [tune] now, or stashes it behind the PIN prompt when gated. */
    fun tune(
        channel: ChannelEntity,
        tune: (ChannelEntity) -> Unit,
    ) {
        if (gate.intercept(channel)) pending = tune else tune(channel)
    }

    /**
     * A verified PIN replays the stashed tune once (re-entering [tune]
     * consumes the gate's one-shot pass); wrong PINs keep prompting.
     */
    fun submit(pin: String) {
        val unlocked = gate.unlock(pin) ?: return
        val action = pending ?: return
        pending = null
        tune(unlocked, action)
    }

    /** Cancelling never tunes; the stashed action is dropped. */
    fun dismiss() {
        gate.dismiss()
        pending = null
    }
}
