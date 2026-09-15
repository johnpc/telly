package com.johncorser.telly.features.player

import android.content.Context
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

/**
 * Builds the media source factory ExoPlayer uses for every IPTV stream.
 *
 * IPTV stream URLs routinely answer with a 302 that hands off to a different
 * scheme/host (token or load-balancer redirect). The default HTTP data source
 * rejects cross-protocol redirects, surfacing them as a fatal "Source error",
 * so we opt in — matching TiviMate. A real client user-agent is sent too;
 * some providers gate streams on it.
 */
internal fun streamMediaSourceFactory(context: Context): DefaultMediaSourceFactory {
    val httpDataSource =
        DefaultHttpDataSource
            .Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent(STREAM_USER_AGENT)
    return DefaultMediaSourceFactory(DefaultDataSource.Factory(context, httpDataSource))
}

private const val STREAM_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 11; SHIELD Android TV) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
