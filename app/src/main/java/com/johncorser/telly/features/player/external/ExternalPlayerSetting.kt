package com.johncorser.telly.features.player.external

/**
 * Raw values of the "Use external player" picker (TellySettings
 * USE_EXTERNAL_PLAYER). Off/On per the ux-spec §3.18 external-player row.
 */
object ExternalPlayerSetting {
    const val OFF = "Off"
    const val ON = "On"

    val options: List<String> = listOf(OFF, ON)

    fun isOn(raw: String): Boolean = raw == ON
}
