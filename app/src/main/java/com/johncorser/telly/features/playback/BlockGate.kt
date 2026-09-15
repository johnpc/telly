package com.johncorser.telly.features.playback

import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * One process-wide "already unlocked" bit shared by every [BlockGate]
 * (guide preview + fullscreen playback build their own tuners), honored
 * only while "Don't require PIN after unlocking" says so.
 */
class BlockSession {
    var unlocked: Boolean = false
}

/**
 * PIN gate in front of every tune of a blocked channel (spec: blocked
 * channels stay listed but are PIN-gated to play). Pure logic — the wheel
 * dialog lives in the UI layer; wrong or cancelled PINs never tune.
 */
class BlockGate(
    private val parental: ParentalControls? = null,
    private val session: BlockSession = BlockSession(),
) {
    private val pending = MutableStateFlow<ChannelEntity?>(null)
    private var passOnceId: Long? = null

    /** The blocked channel awaiting a PIN, or null when no prompt is open. */
    val pinPrompt: StateFlow<ChannelEntity?> = pending.asStateFlow()

    /** True when tuning [channel] is gated; the prompt opens instead. */
    fun intercept(channel: ChannelEntity): Boolean {
        if (passOnceId == channel.id) {
            passOnceId = null
            return false
        }
        val gated = channel.flags.blocked && parental?.hasPin == true && !sessionUnlocked()
        pending.value = if (gated) channel else null
        return gated
    }

    /**
     * The pending channel when [pin] verifies (its next tune passes the
     * gate once); keeps prompting otherwise.
     */
    fun unlock(pin: String): ChannelEntity? {
        val channel = pending.value ?: return null
        if (parental?.verifyPin(pin) != true) return null
        if (parental.relocksAfterUnlock().not()) session.unlocked = true
        pending.value = null
        passOnceId = channel.id
        return channel
    }

    fun dismiss() {
        pending.value = null
    }

    private fun sessionUnlocked(): Boolean = session.unlocked && parental?.relocksAfterUnlock() == false
}
