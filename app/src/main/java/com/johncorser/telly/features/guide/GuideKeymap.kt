package com.johncorser.telly.features.guide

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.playback.KeymapChoice
import com.johncorser.telly.features.playback.keymapChoice

/** What plain LEFT/RIGHT do on the guide grid (Remote control → TV guide). */
enum class GuideLeftRightAction(
    override val label: String,
) : KeymapChoice {
    BY_PROGRAMME("Move by programme"),
    BY_PAGE("Move by page"),
}

/** What CH+/CH− do on the guide grid (they are unmapped by default). */
enum class GuideChannelKeysAction(
    override val label: String,
) : KeymapChoice {
    NOTHING("Nothing"),
    PAGE_CHANNELS("Page the channel list"),
    MOVE_BY_DAY("Move by day"),
}

/** What a held OK does on the grid (MENU keeps the channel menu). */
enum class GuideLongOkAction(
    override val label: String,
) : KeymapChoice {
    CHANNEL_MENU("Open channel menu"),
    PLAY_CHANNEL("Play channel"),
}

/**
 * The remappable TV-guide keys (Settings → Remote control → TV guide).
 * Every default equals the current grid behavior exactly — LEFT/RIGHT walk
 * programme by programme, CH+/CH− are unconsumed, long-OK opens the row
 * context sheet; the remap options only ADD alternatives.
 */
data class GuideKeymap(
    val leftRight: GuideLeftRightAction = GuideLeftRightAction.BY_PROGRAMME,
    val channelUpDown: GuideChannelKeysAction = GuideChannelKeysAction.NOTHING,
    val longOk: GuideLongOkAction = GuideLongOkAction.CHANNEL_MENU,
) {
    companion object {
        /** Reads the three persisted picker labels from [settings]. */
        fun from(settings: SettingsRepository): GuideKeymap =
            GuideKeymap(
                leftRight =
                    keymapChoice(
                        settings.get(TellySettings.REMOTE_GUIDE_LEFT_RIGHT),
                        GuideLeftRightAction.entries,
                        GuideLeftRightAction.BY_PROGRAMME,
                    ),
                channelUpDown =
                    keymapChoice(
                        settings.get(TellySettings.REMOTE_GUIDE_CHANNEL_UP_DOWN),
                        GuideChannelKeysAction.entries,
                        GuideChannelKeysAction.NOTHING,
                    ),
                longOk =
                    keymapChoice(
                        settings.get(TellySettings.REMOTE_GUIDE_LONG_OK),
                        GuideLongOkAction.entries,
                        GuideLongOkAction.CHANNEL_MENU,
                    ),
            )
    }
}
