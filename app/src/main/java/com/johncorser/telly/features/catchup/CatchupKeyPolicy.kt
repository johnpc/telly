package com.johncorser.telly.features.catchup

import com.johncorser.telly.features.playback.PlaybackKey
import com.johncorser.telly.features.playback.PlaybackOverlay

/** What a catch-up-aware key press should do. */
sealed interface CatchupCommand {
    data class Seek(
        val deltaMs: Long,
    ) : CatchupCommand

    /** Jump into catch-up of the airing programme at live edge − [deltaMs]. */
    data class RewindLive(
        val deltaMs: Long,
    ) : CatchupCommand

    /** BACK at bare catch-up playback: return to live / the guide. */
    data object Back : CatchupCommand
}

/** How catch-up relates to what is playing right now. */
enum class CatchupMode { NONE, LIVE_CAPABLE, PLAYING }

/** Which seek keys the Remote-control settings currently enable. */
data class CatchupSeekKeys(
    val rwFf: Boolean = true,
    val leftRight: Boolean = false,
    val downUp: Boolean = false,
    val rwLive: Boolean = false,
    val leftLive: Boolean = false,
    val downLive: Boolean = false,
)

/**
 * The seek amounts, mapped from Settings -> Playback -> "Skip steps": back
 * = the first configured step, forward = the second (the default preset's
 * 10 s / 30 s reproduce the reference's fixed catch-up steps exactly).
 */
data class CatchupSkip(
    val backMs: Long = DEFAULT_BACK_MS,
    val forwardMs: Long = DEFAULT_FORWARD_MS,
) {
    companion object {
        const val DEFAULT_BACK_MS = 10_000L
        const val DEFAULT_FORWARD_MS = 30_000L

        fun of(steps: List<Long>): CatchupSkip =
            steps.firstOrNull()?.let { back -> CatchupSkip(back, steps.getOrElse(1) { back }) } ?: CatchupSkip()
    }
}

/**
 * The catch-up context EXTENDING [com.johncorser.telly.features.playback.PlaybackKeyPolicy]:
 * consulted first, and only for the keys the Remote-control toggles hand to
 * seeking; everything it declines falls through to the live key map
 * unchanged. RW/FF work over the transient overlays too; LEFT/RIGHT and
 * DOWN/UP seeks apply at bare playback only, where the live map has those
 * keys unbound (the info overlay needs them for card focus).
 */
object CatchupKeyPolicy {
    fun commandFor(
        overlay: PlaybackOverlay,
        key: PlaybackKey,
        mode: CatchupMode,
        keys: CatchupSeekKeys,
        skip: CatchupSkip = CatchupSkip(),
    ): CatchupCommand? =
        when (mode) {
            CatchupMode.NONE -> null
            CatchupMode.LIVE_CAPABLE -> duringLive(overlay, key, keys, skip)
            CatchupMode.PLAYING -> duringCatchup(overlay, key, keys, skip)
        }

    /** The "rewind live with catch-up" toggles (RW anywhere transient, LEFT/DOWN at bare playback). */
    private fun duringLive(
        overlay: PlaybackOverlay,
        key: PlaybackKey,
        keys: CatchupSeekKeys,
        skip: CatchupSkip,
    ): CatchupCommand? =
        when {
            !transient(overlay) -> null
            key == PlaybackKey.REWIND && keys.rwLive -> CatchupCommand.RewindLive(skip.backMs)
            overlay != PlaybackOverlay.None -> null
            key == PlaybackKey.LEFT && keys.leftLive -> CatchupCommand.RewindLive(skip.backMs)
            key == PlaybackKey.DOWN && keys.downLive -> CatchupCommand.RewindLive(skip.backMs)
            else -> null
        }

    private fun duringCatchup(
        overlay: PlaybackOverlay,
        key: PlaybackKey,
        keys: CatchupSeekKeys,
        skip: CatchupSkip,
    ): CatchupCommand? =
        when {
            !transient(overlay) -> null
            key == PlaybackKey.REWIND && keys.rwFf -> CatchupCommand.Seek(-skip.backMs)
            key == PlaybackKey.FAST_FORWARD && keys.rwFf -> CatchupCommand.Seek(skip.forwardMs)
            overlay != PlaybackOverlay.None -> null
            else -> atBareCatchup(key, keys, skip)
        }

    /** D-pad seeks + BACK apply at bare playback only (overlays own those keys). */
    private fun atBareCatchup(
        key: PlaybackKey,
        keys: CatchupSeekKeys,
        skip: CatchupSkip,
    ): CatchupCommand? =
        when {
            key == PlaybackKey.LEFT && keys.leftRight -> CatchupCommand.Seek(-skip.backMs)
            key == PlaybackKey.RIGHT && keys.leftRight -> CatchupCommand.Seek(skip.forwardMs)
            key == PlaybackKey.DOWN && keys.downUp -> CatchupCommand.Seek(-skip.backMs)
            key == PlaybackKey.UP && keys.downUp -> CatchupCommand.Seek(skip.forwardMs)
            key == PlaybackKey.BACK -> CatchupCommand.Back
            else -> null
        }

    /** Bare playback + the transient overlays; sticky layers keep their own keys. */
    private fun transient(overlay: PlaybackOverlay): Boolean =
        overlay == PlaybackOverlay.None ||
            overlay == PlaybackOverlay.Info ||
            overlay == PlaybackOverlay.InfoTransport ||
            overlay == PlaybackOverlay.ZapInfo
}
