package com.johncorser.telly

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.johncorser.telly.core.ServiceLocator
import com.johncorser.telly.core.settings.TellySettings

/**
 * Thin glue for [Autostart]: declared in the manifest for BOOT_COMPLETED
 * and registered at runtime by [TellyApplication] for SCREEN_ON (the wake
 * action is never deliverable via the manifest). Some Android versions
 * restrict background activity starts; the wake row's captured summary
 * ("May not work on all devices") documents exactly that.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        val settings = ServiceLocator.settingsRepository(context)
        val launch =
            Autostart.shouldLaunch(
                action = intent?.action,
                onBoot = settings.get(TellySettings.AUTOSTART_ON_BOOT),
                onWake = settings.get(TellySettings.AUTOSTART_ON_WAKE),
            )
        if (!launch) return
        context.startActivity(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
