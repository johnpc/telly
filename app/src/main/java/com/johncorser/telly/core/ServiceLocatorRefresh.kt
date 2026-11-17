package com.johncorser.telly.core

import android.util.Log
import com.johncorser.telly.core.refresh.RefreshGate
import kotlinx.coroutines.Dispatchers

/** logcat tag for every background refresh run (`adb logcat -s telly-refresh`). */
internal const val REFRESH_LOG_TAG = "telly-refresh"

/**
 * The ONE gate every background refresh goes through (EPG refresher, playlist
 * updater — background loops, Settings actions and the restore runner alike),
 * so EPG writes and playlist imports never overlap.
 */
internal val sharedRefreshGate: RefreshGate by lazy {
    RefreshGate(
        clock = { ServiceLocator.clock() },
        log = { Log.i(REFRESH_LOG_TAG, it) },
        timerContext = Dispatchers.Default,
    )
}
