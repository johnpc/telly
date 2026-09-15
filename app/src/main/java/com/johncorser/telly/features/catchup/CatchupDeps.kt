package com.johncorser.telly.features.catchup

import com.johncorser.telly.core.settings.Setting
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.player.skip.SkipSteps

/**
 * Catch-up wiring shared by the guide (request producer) and the playback
 * screen (consumer): one session slot per app, live reads of the
 * Remote-control seek toggles, and the configured Playback skip steps.
 */
class CatchupDeps(
    val session: CatchupSession = CatchupSession(),
    val toggles: CatchupToggles = CatchupToggles(),
    /** Settings -> Playback -> "Skip steps", read per key press. */
    val skipSteps: () -> List<Long> = { SkipSteps.parse(SkipSteps.DEFAULT_RAW) },
) {
    /** The seek amounts (back = first configured step, forward = second). */
    fun skip(): CatchupSkip = CatchupSkip.of(skipSteps())
}

/** Live reads of Settings → Remote control; defaults match the catalogue. */
class CatchupToggles(
    private val read: (Setting<Boolean>) -> Boolean = { it.default },
) {
    fun snapshot(): CatchupSeekKeys =
        CatchupSeekKeys(
            rwFf = read(TellySettings.SEEK_RWFF_CATCHUP),
            leftRight = read(TellySettings.SEEK_LEFT_RIGHT),
            downUp = read(TellySettings.SEEK_DOWN_UP),
            rwLive = read(TellySettings.RW_REWINDS_LIVE),
            leftLive = read(TellySettings.LEFT_REWINDS_LIVE),
            downLive = read(TellySettings.DOWN_REWINDS_LIVE),
        )
}
