package com.johncorser.telly.features.playback

/**
 * Focus memory of the shared long-OK context sheet: a row that pushes a
 * screen over the sheet (Program description, Channel options, a paywalled
 * or coming-soon row) remembers itself so BACK re-lands sheet focus on it
 * instead of resetting to the first row — standard leanback restore,
 * pending round6 on-device confirmation against TiviMate. Rows that leave
 * the sheet for good (Search, Settings, favorites, hide) clear it.
 */
class PlayerMenuFocus {
    /** The row the reopened sheet should focus; null = the first row. */
    var restore: PlayerMenuItem? = null
        private set

    /** Called for every activated sheet row before it routes. */
    fun onActivated(item: PlayerMenuItem) {
        restore = item.takeIf { PlayerMenuRouting.routeOf(it) in PUSHED }
    }

    /** A freshly opened sheet always lands on the first row again. */
    fun clear() {
        restore = null
    }

    private companion object {
        /** Routes that push a screen whose BACK pops back to the sheet. */
        val PUSHED =
            setOf(
                PlayerMenuRoute.DESCRIPTION,
                PlayerMenuRoute.CHANNEL_OPTIONS,
                PlayerMenuRoute.PAYWALL,
                PlayerMenuRoute.COMING_SOON,
            )
    }
}
