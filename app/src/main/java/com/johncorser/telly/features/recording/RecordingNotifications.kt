package com.johncorser.telly.features.recording

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.johncorser.telly.R
import com.johncorser.telly.features.playback.ProgramTimes

/** Builds the foreground-service notification: channel + elapsed time. */
object RecordingNotifications {
    const val CHANNEL_ID = "recording"
    const val NOTIFICATION_ID = 0x0EC
    private const val EXTRA_CHANNEL_NAME = "channelName"
    private const val EXTRA_STARTED_AT = "startedAtMs"
    private const val EXTRA_COUNT = "count"

    /** The start intent carrying what the notification should show. */
    fun intent(
        context: Context,
        session: RecordingSession,
        count: Int,
    ): Intent =
        Intent(context, RecordingService::class.java)
            .putExtra(EXTRA_CHANNEL_NAME, session.channelName)
            .putExtra(EXTRA_STARTED_AT, session.startedAtMs)
            .putExtra(EXTRA_COUNT, count)

    fun sessionOf(intent: Intent?): RecordingSession =
        RecordingSession(
            channelName = intent?.getStringExtra(EXTRA_CHANNEL_NAME) ?: "",
            startedAtMs = intent?.getLongExtra(EXTRA_STARTED_AT, 0L) ?: 0L,
        )

    fun countOf(intent: Intent?): Int = intent?.getIntExtra(EXTRA_COUNT, 1) ?: 1

    /** "News One — 12:34" (+" and 1 more" with parallel captures). */
    fun text(
        session: RecordingSession,
        count: Int,
        nowMs: Long,
    ): String {
        val more = if (count > 1) " and ${count - 1} more" else ""
        return "${session.channelName}$more — ${ProgramTimes.span(nowMs - session.startedAtMs)}"
    }

    fun build(
        context: Context,
        session: RecordingSession,
        count: Int,
        nowMs: Long,
    ): android.app.Notification {
        ensureChannel(context)
        return NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_rail_dvr)
            .setContentTitle("Recording")
            .setContentText(text(session, count, nowMs))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Recording", NotificationManager.IMPORTANCE_LOW),
        )
    }
}
