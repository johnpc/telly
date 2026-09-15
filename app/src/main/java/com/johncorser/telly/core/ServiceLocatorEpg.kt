package com.johncorser.telly.core

import android.content.Context
import android.util.Log
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.epg.EpgFetch
import com.johncorser.telly.features.epg.EpgRefresher
import com.johncorser.telly.features.epg.EpgRetention
import com.johncorser.telly.features.epg.RefreshScheduler

/**
 * EPG refresh policy driven live by Settings -> EPG. Failed fetches are
 * logged here (the composition root is where logic meets android.util.Log)
 * so silent guide gaps stay diagnosable.
 */
fun ServiceLocator.epgRefresher(context: Context): EpgRefresher {
    val repository = epgRepository(context)
    val prefs = settingsRepository(context)
    return EpgRefresher(
        playlistDao = database(context).playlistDao(),
        scheduler =
            RefreshScheduler(
                intervalMs = {
                    RefreshScheduler.hoursToMs(prefs.get(TellySettings.EPG_UPDATE_INTERVAL_HOURS))
                },
            ),
        clock = clock,
        fetch =
            EpgFetch(
                refresh = repository::refresh,
                warn = { message, cause -> Log.w("telly", message, cause) },
            ),
        retention =
            EpgRetention(
                keepPastMs = { EpgRefresher.daysToMs(prefs.get(TellySettings.EPG_PAST_DAYS_TO_KEEP)) },
                trim = repository::trimEndedBefore,
            ),
        customSources = { playlistUrl -> epgSourceStore(context).forPlaylist(playlistUrl).map { it.url } },
    )
}
