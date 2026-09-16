package com.johncorser.telly.features.playback

import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.guide.ChannelOptionsController
import com.johncorser.telly.features.guide.ChannelOptionsStore
import com.johncorser.telly.features.mylist.MyListMenuHost
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope

/** Which PIN dialog the block/unblock row needs before it may act. */
enum class BlockPinMode(
    val title: String,
) {
    /** No parental PIN exists yet: set one first (reused setup wheel). */
    SETUP("Set a PIN"),

    /** A PIN is configured: confirm it before flipping the flag. */
    CONFIRM("Enter PIN"),
}

/**
 * The context sheet's "Block channel" / "Unblock channel" policy, shared by
 * the guide and panel hosts: both directions are PIN-gated — confirming the
 * configured PIN, or setting one first when none exists (ux-spec §3.9).
 */
class ChannelBlocker(
    private val actions: ChannelActions,
    private val parental: ParentalControls? = null,
) {
    /** The dialog the next block/unblock needs, decided at open time. */
    fun mode(): BlockPinMode = if (parental?.hasPin == true) BlockPinMode.CONFIRM else BlockPinMode.SETUP

    /** Sets/verifies the PIN; flips [channel]'s blocked flag on success. */
    fun submit(
        pin: String,
        mode: BlockPinMode,
        channel: ChannelEntity,
    ): Boolean {
        val accepted =
            when (mode) {
                BlockPinMode.SETUP -> parental != null && setUp(pin)
                BlockPinMode.CONFIRM -> parental?.verifyPin(pin) == true
            }
        if (accepted) actions.toggleBlocked(channel)
        return accepted
    }

    private fun setUp(pin: String): Boolean {
        parental?.setPin(pin)
        return true
    }
}

/**
 * The channel-flag actions a context sheet executes, bundled for both
 * hosts: the plain favorite/hide/block persistence plus the PIN-gated
 * block/unblock policy over it.
 */
class SheetActions(
    val channels: ChannelActions,
    val blocker: ChannelBlocker = ChannelBlocker(channels),
    /** The §41 Channel-options pane's live rows/dialogs (shared machine). */
    val options: ChannelOptionsController,
) {
    /** The host's My-list context; the sheet's My-list/management rows. */
    val myList: MyListMenuHost? get() = channels.myList

    companion object {
        /** Both hosts assemble the bundle from their [PlaybackEnv] this way. */
        fun over(
            env: PlaybackEnv,
            scope: CoroutineScope,
            myList: MyListMenuHost? = null,
        ): SheetActions {
            val actions = ChannelActions(env.channelDao, scope, myList)
            val external = env.hooks.platform.external
            return SheetActions(
                channels = actions,
                blocker = ChannelBlocker(actions, env.hooks.parental),
                options =
                    ChannelOptionsController(ChannelOptionsStore(env.channelDao, scope)) {
                        external.enabledByDefault
                    },
            )
        }
    }
}
