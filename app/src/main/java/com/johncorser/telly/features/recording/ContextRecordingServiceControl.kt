package com.johncorser.telly.features.recording

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/**
 * Production [RecordingServiceControl]: while captures run the foreground
 * [RecordingService] shows the first one (+count); when none remain the
 * service stops and the notification goes away.
 */
class ContextRecordingServiceControl(
    private val context: Context,
) : RecordingServiceControl {
    override fun sync(sessions: List<RecordingSession>) {
        val first = sessions.firstOrNull()
        if (first == null) {
            context.stopService(Intent(context, RecordingService::class.java))
        } else {
            ContextCompat.startForegroundService(
                context,
                RecordingNotifications.intent(context, first, sessions.size),
            )
        }
    }
}
