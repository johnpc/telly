package com.johncorser.telly.features.player.external

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.johncorser.telly.R

/**
 * Thin platform glue for [ExternalPlayer.open]: fires an ACTION_VIEW
 * chooser for the stream URL under the generic video MIME. With no handler
 * installed it shows the "No external player installed" toast instead of
 * crashing, so tune-time callers gracefully fall back to internal playback.
 */
class ExternalPlayerActivityLauncher(
    private val context: Context,
) {
    /** True when a handler exists and the chooser was fired. */
    fun open(streamUrl: String): Boolean {
        val view = Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(streamUrl), MIME_VIDEO)
        @Suppress("DEPRECATION")
        if (context.packageManager.resolveActivity(view, 0) == null) {
            Toast.makeText(context, R.string.external_player_missing, Toast.LENGTH_LONG).show()
            return false
        }
        context.startActivity(Intent.createChooser(view, context.getString(R.string.external_player_chooser)))
        return true
    }

    companion object {
        private const val MIME_VIDEO = "video/*"
    }
}
