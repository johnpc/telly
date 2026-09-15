package com.johncorser.telly

import androidx.compose.runtime.Composable
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.core.navigation.Route
import com.johncorser.telly.features.vod.VodDeps
import com.johncorser.telly.features.vod.VodPlaybackScreen
import com.johncorser.telly.features.vod.VodScreen

/**
 * The VOD slice's route pair (extension-file pattern keeping the base
 * route host small): the rail's Movies icon pushes the browser, OK on a
 * card pushes seekable playback, BACK pops one level at a time.
 */
@Composable
internal fun RootScreenVodRoutes(
    target: Route,
    navigator: Navigator,
    vodDeps: VodDeps,
) {
    when (target) {
        Route.Vod -> VodScreen(deps = vodDeps, onPlay = { navigator.push(Route.VodPlayback(it)) })
        is Route.VodPlayback ->
            VodPlaybackScreen(
                deps = vodDeps,
                itemKey = target.itemKey,
                onExit = { navigator.pop() },
            )
        else -> Unit
    }
}
