package com.johncorser.telly.features.guide

import com.johncorser.telly.features.player.external.ExternalPlayerSetting
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.settings.PickerOption
import kotlin.math.abs

/**
 * Value formatting + option lists of the Channel-options pane: the
 * Default/Hardware/Software decoder picks, the ±12 h EPG offset in 30-min
 * steps, and the external-player toggle's effective state (per-channel
 * override first, the global Playback setting otherwise).
 */
object ChannelOptionsValues {
    /** "Default" = no per-channel override; the global setting applies. */
    const val FOLLOW_GLOBAL = "Default"

    val decoderOptions: List<PickerOption> =
        listOf(FOLLOW_GLOBAL, "Hardware", "Software").map { PickerOption(it, it) }

    fun decoderLabel(raw: String?): String = raw ?: FOLLOW_GLOBAL

    /** The stored override behind a picked label (null = follow global). */
    fun decoderRawOf(picked: String): String? = picked.takeIf { it != FOLLOW_GLOBAL }

    /** -12:00 .. +12:00 in 30-minute steps (raw = the minutes). */
    val offsetOptions: List<PickerOption> =
        (-HALF_STEPS..HALF_STEPS).map { step ->
            val minutes = step * STEP_MINUTES
            PickerOption(offsetLabel(minutes), minutes.toString())
        }

    /** "h:min" with a leading minus for negative offsets ("0:00", "-1:30"). */
    fun offsetLabel(minutes: Int): String {
        val sign = if (minutes < 0) "-" else ""
        val magnitude = abs(minutes)
        return "$sign${magnitude / MINUTES_PER_HOUR}:" +
            (magnitude % MINUTES_PER_HOUR).toString().padStart(2, '0')
    }

    /** The toggle's checked state: per-channel override > global setting. */
    fun externalEffective(
        channel: ChannelEntity,
        globalOn: Boolean,
    ): Boolean = ExternalPlayerSetting.overrideOf(channel.overrides.externalPlayer) ?: globalOn

    private const val STEP_MINUTES = 30
    private const val HALF_STEPS = 24
    private const val MINUTES_PER_HOUR = 60
}
