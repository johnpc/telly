package com.johncorser.telly

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.os.Build

/**
 * Registers the SCREEN_ON relaunch receiver for "Auto start app on wake up
 * from sleep mode": SCREEN_ON is runtime-register-only, so the toggle works
 * while the telly process is alive (see [Autostart]'s honesty note).
 */
class TellyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Autostart.SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(BootReceiver(), filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(BootReceiver(), filter)
        }
    }
}
