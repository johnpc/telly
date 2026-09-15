package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.player.afr.AfrPreference
import com.johncorser.telly.features.player.external.ExternalPlayerSetting
import com.johncorser.telly.features.player.skip.SkipSteps

/**
 * The playback-extras pickers (AFR / external player / skip steps),
 * option lists owned by their feature slices. Merged into
 * [SettingsPickers.byRowId]; split out for the file-length gate.
 */
internal fun playbackExtrasPickerSpecs(): List<PickerSpec> =
    listOf(
        PickerSpec(
            rowId = RowIds.PLAYBACK_AFR,
            title = "Auto frame rate (AFR)",
            key = TellySettings.AUTO_FRAME_RATE.key,
            options = sameRaw(AfrPreference.options),
        ),
        PickerSpec(
            rowId = RowIds.PLAYBACK_EXTERNAL_PLAYER,
            title = "Use external player",
            key = TellySettings.USE_EXTERNAL_PLAYER.key,
            options = sameRaw(ExternalPlayerSetting.options),
        ),
        PickerSpec(
            rowId = RowIds.PLAYBACK_SKIP_STEPS,
            title = "Skip steps",
            key = TellySettings.SKIP_STEPS.key,
            options = sameRaw(SkipSteps.PRESETS),
        ),
    )

/** The extras' captured-style defaults for [SettingsPickers.currentRaw]. */
internal fun playbackExtrasDefaultRaws(): Map<String, String> =
    mapOf(
        TellySettings.AUTO_FRAME_RATE.key to TellySettings.AUTO_FRAME_RATE.default,
        TellySettings.USE_EXTERNAL_PLAYER.key to TellySettings.USE_EXTERNAL_PLAYER.default,
        TellySettings.SKIP_STEPS.key to TellySettings.SKIP_STEPS.default,
    )
