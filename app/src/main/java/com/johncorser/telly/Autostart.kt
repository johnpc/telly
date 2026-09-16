package com.johncorser.telly

/**
 * Auto-start policy (Settings -> General): BOOT_COMPLETED launches telly
 * when "Auto start app on boot" is on; SCREEN_ON relaunches it when "Auto
 * start app on wake up from sleep mode" is on. Raw action strings (not
 * Intent constants) keep this plain-JVM testable.
 *
 * Honesty note on wake: SCREEN_ON is only deliverable to a runtime-registered
 * receiver, so it works while the telly process is alive (registered by
 * [TellyApplication]) — the row's captured "May not work on all devices"
 * summary stays true. A sticky service just to keep the receiver alive would
 * be battery-hostile and was deliberately not added.
 */
object Autostart {
    const val BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    const val SCREEN_ON = "android.intent.action.SCREEN_ON"

    fun shouldLaunch(
        action: String?,
        onBoot: Boolean,
        onWake: Boolean,
    ): Boolean =
        when (action) {
            BOOT_COMPLETED -> onBoot
            SCREEN_ON -> onWake
            else -> false
        }
}
