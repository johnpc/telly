package com.johncorser.telly.features.settings

import com.johncorser.telly.core.design.AccentPalette
import com.johncorser.telly.core.settings.TellySettings

/** A single-choice picker: label list over one raw stored value. */
data class PickerSpec(
    val rowId: String,
    val title: String,
    val key: String,
    val options: List<PickerOption>,
)

data class PickerOption(
    val label: String,
    val raw: String,
)

private fun sameRaw(labels: List<String>) = labels.map { PickerOption(it, it) }

/**
 * Row id -> picker. Captured defaults are in TellySettings; option lists of
 * premium-locked reference pickers were not capturable (VERIFY-ON-DEVICE).
 */
object SettingsPickers {
    val EPG_INTERVAL_HOURS = listOf(0, 1, 2, 3, 6, 12, 24)
    val EPG_PAST_DAYS = listOf(1, 2, 3, 4, 5, 6, 7)

    private val all =
        listOf(
            PickerSpec(
                rowId = RowIds.PLAYLISTS_SORTING,
                title = "Playlists sorting",
                key = TellySettings.PLAYLISTS_SORTING.key,
                options = sameRaw(listOf("By name", "By date added")),
            ),
            PickerSpec(
                rowId = RowIds.EPG_UPDATE_INTERVAL,
                title = "Update interval, hours",
                key = TellySettings.EPG_UPDATE_INTERVAL_HOURS.key,
                options = EPG_INTERVAL_HOURS.map { PickerOption(intervalLabel(it), it.toString()) },
            ),
            PickerSpec(
                rowId = RowIds.EPG_PAST_DAYS,
                title = "Past days to keep EPG",
                key = TellySettings.EPG_PAST_DAYS_TO_KEEP.key,
                options = EPG_PAST_DAYS.map { PickerOption(it.toString(), it.toString()) },
            ),
            PickerSpec(
                rowId = RowIds.APPEARANCE_COLOR_THEME,
                title = "Color theme",
                key = TellySettings.ACCENT_COLOR.key,
                options = AccentPalette.names().map { PickerOption(themeLabel(it), it) },
            ),
            PickerSpec(
                rowId = RowIds.PLAYBACK_BUFFER_SIZE,
                title = "Buffer size",
                key = TellySettings.BUFFER_SIZE.key,
                options = sameRaw(listOf("Small", "Medium", "Large")),
            ),
            PickerSpec(
                rowId = RowIds.PLAYBACK_AUDIO_DECODER,
                title = "Audio decoder",
                key = TellySettings.AUDIO_DECODER.key,
                options = sameRaw(listOf("Hardware", "Software")),
            ),
            PickerSpec(
                rowId = RowIds.PLAYBACK_VIDEO_DECODER,
                title = "Video decoder",
                key = TellySettings.VIDEO_DECODER.key,
                options = sameRaw(listOf("Hardware", "Software")),
            ),
            PickerSpec(
                rowId = RowIds.PARENTAL_PIN_INPUT,
                title = "PIN input method",
                key = TellySettings.PARENTAL_PIN_INPUT_METHOD.key,
                options = sameRaw(listOf("Picker", "Keyboard")),
            ),
            PickerSpec(
                rowId = RowIds.PARENTAL_RELOCK,
                title = "Don't require PIN after unlocking",
                key = TellySettings.PARENTAL_RELOCK.key,
                options = sameRaw(listOf("Always require", "Until app restart")),
            ),
        ) + RemoteKeymapPickers.specs

    val byRowId: Map<String, PickerSpec> = all.associateBy { it.rowId }

    private val defaultRawByKey: Map<String, String> =
        mapOf(
            TellySettings.PLAYLISTS_SORTING.key to TellySettings.PLAYLISTS_SORTING.default,
            TellySettings.EPG_UPDATE_INTERVAL_HOURS.key to TellySettings.EPG_UPDATE_INTERVAL_HOURS.default.toString(),
            TellySettings.EPG_PAST_DAYS_TO_KEEP.key to TellySettings.EPG_PAST_DAYS_TO_KEEP.default.toString(),
            TellySettings.ACCENT_COLOR.key to TellySettings.ACCENT_COLOR.default,
            TellySettings.BUFFER_SIZE.key to TellySettings.BUFFER_SIZE.default,
            TellySettings.AUDIO_DECODER.key to TellySettings.AUDIO_DECODER.default,
            TellySettings.VIDEO_DECODER.key to TellySettings.VIDEO_DECODER.default,
            TellySettings.PARENTAL_PIN_INPUT_METHOD.key to TellySettings.PARENTAL_PIN_INPUT_METHOD.default,
            TellySettings.PARENTAL_RELOCK.key to TellySettings.PARENTAL_RELOCK.default,
        ) + RemoteKeymapPickers.defaults

    /** The raw value a picker should highlight given the store [snapshot]. */
    fun currentRaw(
        spec: PickerSpec,
        snapshot: Map<String, String>,
    ): String = snapshot[spec.key] ?: defaultRawByKey.getValue(spec.key)

    /** "None" for 0, per the captured default of both interval pickers. */
    fun intervalLabel(hours: Int): String = if (hours == 0) "None" else hours.toString()

    /** The captured value renders as "Dark  •  <accent>" (double spaces). */
    fun themeLabel(accent: String): String = "Dark  •  $accent"
}
