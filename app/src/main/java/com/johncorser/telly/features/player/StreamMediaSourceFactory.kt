package com.johncorser.telly.features.player

import android.content.Context
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

/**
 * Builds the media source factory ExoPlayer uses for every IPTV stream.
 *
 * IPTV stream URLs routinely answer with a 302 that hands off to a different
 * scheme/host (token or load-balancer redirect). The default HTTP data source
 * rejects cross-protocol redirects, surfacing them as a fatal "Source error",
 * so we opt in — matching TiviMate. A real client user-agent is sent too;
 * some providers gate streams on it — per request via [userAgent], so a
 * playlist's own User-Agent setting reaches manifest AND segment fetches
 * (the factory-level setUserAgent would override per-request headers).
 */
internal fun streamMediaSourceFactory(
    context: Context,
    userAgent: () -> String = { STREAM_USER_AGENT },
): DefaultMediaSourceFactory {
    val httpDataSource = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
    val upstream = DefaultDataSource.Factory(context, httpDataSource)
    val withUserAgent =
        ResolvingDataSource.Factory(upstream) { dataSpec: DataSpec ->
            dataSpec.withAdditionalHeaders(mapOf(USER_AGENT_HEADER to userAgent()))
        }
    return DefaultMediaSourceFactory(withUserAgent)
}

private const val USER_AGENT_HEADER = "User-Agent"

/** The default stream UA (a real TV client; some providers gate on it). */
internal const val STREAM_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 11; SHIELD Android TV) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
