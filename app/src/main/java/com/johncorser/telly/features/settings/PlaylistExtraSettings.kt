package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.Setting
import com.johncorser.telly.core.settings.boolSetting
import com.johncorser.telly.core.settings.intSetting
import com.johncorser.telly.core.settings.stringSetting

// Per-playlist settings keyed by the playlist URL, like the enable flag
// (see playlistEnabledSetting). A playlist URL edit re-keys these via
// PlaylistKeyMigration.

/** User-Agent sent with this playlist's fetches and its channels' streams. */
fun playlistUserAgentSetting(url: String): Setting<String> = stringSetting("playlist_user_agent:$url", "")

/** "Update interval, hours" (0 = the captured default "None"). */
fun playlistUpdateIntervalSetting(url: String): Setting<Int> = intSetting("playlist_update_interval:$url", 0)

/** "Update on app start": forces a fetch at launch for this playlist. */
fun playlistUpdateOnStartSetting(url: String): Setting<Boolean> = boolSetting("playlist_update_on_start:$url", false)

/** Manage groups: whether the playlist's [group] is shown (default on). */
fun playlistGroupEnabledSetting(
    url: String,
    group: String,
): Setting<Boolean> = boolSetting("playlist_group_enabled:$url:$group", true)

/** The per-playlist update-interval choices (TiviMate's playlist picker). */
val PLAYLIST_UPDATE_INTERVAL_HOURS = listOf(0, 1, 2, 4, 8, 12, 24)

/** The "Update interval, hours" picker over the playlist's own KV key. */
fun playlistUpdateIntervalPicker(url: String): PickerSpec =
    PickerSpec(
        rowId = RowIds.PLAYLIST_UPDATE_INTERVAL,
        title = "Update interval, hours",
        key = playlistUpdateIntervalSetting(url).key,
        options =
            PLAYLIST_UPDATE_INTERVAL_HOURS.map {
                PickerOption(SettingsPickers.intervalLabel(it), it.toString())
            },
    )
