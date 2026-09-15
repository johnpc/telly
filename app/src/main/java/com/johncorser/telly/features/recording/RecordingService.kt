package com.johncorser.telly.features.recording

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground shell around the record engine: keeps the process alive while
 * captures run (survives the activity backgrounding) and shows channel +
 * elapsed. Thin glue by design — start/stop/copy logic lives in the
 * JVM-tested recording classes; this only renders their state. The one
 * wall-clock read drives the elapsed text, nothing else.
 */
class RecordingService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var ticker: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val session = RecordingNotifications.sessionOf(intent)
        val count = RecordingNotifications.countOf(intent)
        ServiceCompat.startForeground(
            this,
            RecordingNotifications.NOTIFICATION_ID,
            RecordingNotifications.build(this, session, count, System.currentTimeMillis()),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
        restartTicker(session, count)
        return START_NOT_STICKY
    }

    /** Re-posts the notification each second so the elapsed time ticks. */
    private fun restartTicker(
        session: RecordingSession,
        count: Int,
    ) {
        ticker?.cancel()
        ticker =
            scope.launch {
                while (isActive) {
                    delay(TICK_MS)
                    NotificationManagerCompat.from(this@RecordingService).notify(
                        RecordingNotifications.NOTIFICATION_ID,
                        RecordingNotifications.build(
                            this@RecordingService,
                            session,
                            count,
                            System.currentTimeMillis(),
                        ),
                    )
                }
            }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TICK_MS = 1_000L
    }
}
