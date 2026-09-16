package com.johncorser.telly

import androidx.compose.runtime.Composable
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.core.navigation.Route
import com.johncorser.telly.features.mylist.ChannelNamesEditorScreen
import com.johncorser.telly.features.mylist.ManageFavoritesScreen
import com.johncorser.telly.features.mylist.MyListScreen
import com.johncorser.telly.features.mylist.ReorderChannelsScreen
import com.johncorser.telly.features.playback.PlaybackDeps

/**
 * The mylist slice's routes: the My List screen behind the guide rail's
 * bookmark icon plus the Manage Favorites / Reorder channels editors the
 * context sheet opens. An airing My List entry tunes via the search
 * precedent — the guide becomes the stack root with playback above.
 */
@Composable
internal fun RootScreenMyListRoutes(
    target: Route,
    navigator: Navigator,
    playbackDeps: PlaybackDeps,
) {
    when (target) {
        Route.MyList ->
            MyListScreen(
                deps = playbackDeps,
                onTuned = {
                    navigator.replaceAll(Route.Guide)
                    navigator.push(Route.Playback)
                },
            )
        Route.ManageFavorites -> ManageFavoritesScreen(deps = playbackDeps)
        is Route.ReorderChannels -> ReorderChannelsScreen(deps = playbackDeps, group = target.group)
        Route.ChannelNamesEditor -> ChannelNamesEditorScreen(deps = playbackDeps)
        else -> Unit
    }
}
