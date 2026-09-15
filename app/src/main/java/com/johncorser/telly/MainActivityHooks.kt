package com.johncorser.telly

import android.view.Display
import androidx.activity.ComponentActivity
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.playback.PlaybackHooks
import com.johncorser.telly.features.player.PlayerPlatformHooks
import com.johncorser.telly.features.player.afr.AfrController
import com.johncorser.telly.features.player.afr.AfrDisplay
import com.johncorser.telly.features.player.afr.AfrMode
import com.johncorser.telly.features.player.afr.AfrModeSet
import com.johncorser.telly.features.player.afr.AfrPreference
import com.johncorser.telly.features.player.external.ExternalPlayer
import com.johncorser.telly.features.player.external.ExternalPlayerActivityLauncher
import com.johncorser.telly.features.player.external.ExternalPlayerSetting

// MainActivity's platform glue for the playback extras (kept out of
// MainActivity.kt for the file-length gate; logic lives in the tested
// AfrController/ExternalPlayer classes).

/** The AFR controller over this activity's window/display. */
internal fun MainActivity.afrController(): AfrController =
    AfrController(
        preference = {
            AfrPreference.fromRaw(ServiceLocator.settingsRepository(this).get(TellySettings.AUTO_FRAME_RATE))
        },
        display = ActivityAfrDisplay(this),
    )

/** The base playback hooks (AFR + external player) the slices copy nav lambdas onto. */
internal fun MainActivity.playbackHooks(afr: AfrController): PlaybackHooks {
    val settings = ServiceLocator.settingsRepository(this)
    return PlaybackHooks(
        platform =
            PlayerPlatformHooks(
                onFrameRateChanged = afr::onFrameRate,
                onPlaybackStopped = afr::onPlaybackStopped,
                external =
                    ExternalPlayer(
                        enabledForTuning = {
                            ExternalPlayerSetting.isOn(settings.get(TellySettings.USE_EXTERNAL_PLAYER))
                        },
                        launch = ExternalPlayerActivityLauncher(this)::open,
                    ),
            ),
    )
}

/** Display.getMode()/getSupportedModes() in, preferredDisplayModeId out. */
internal class ActivityAfrDisplay(
    private val activity: ComponentActivity,
) : AfrDisplay {
    override fun modes(): AfrModeSet? {
        val display = currentDisplay() ?: return null
        val current = display.mode ?: return null
        return AfrModeSet(current = current.toAfrMode(), all = display.supportedModes.map { it.toAfrMode() })
    }

    override fun apply(modeId: Int) {
        val window = activity.window ?: return
        window.attributes = window.attributes.apply { preferredDisplayModeId = modeId }
    }

    @Suppress("DEPRECATION")
    private fun currentDisplay(): Display? = activity.windowManager?.defaultDisplay

    private fun Display.Mode.toAfrMode() = AfrMode(modeId, physicalWidth, physicalHeight, refreshRate)
}
