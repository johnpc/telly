package com.johncorser.telly.features.mylist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.johncorser.telly.R
import com.johncorser.telly.core.ui.OnboardingScreenMessage
import com.johncorser.telly.core.ui.TellyScreenMutedText
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playback.PlayerMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.TimeZone

/**
 * The My List screen behind the guide rail's bookmark icon (Route.MyList):
 * saved programmes newest-first, OK on an airing entry tunes its channel
 * (guide-root BACK chain, the search precedent), OK on a future entry shows
 * its description, long-OK removes an entry, ended entries auto-hide. No
 * captures exist (premium-locked in the reference free tier), so the layout
 * follows telly's History-screen idiom on the flat app background.
 */
@Composable
fun MyListScreen(
    deps: PlaybackDeps,
    onTuned: () -> Unit,
) {
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    val viewModel =
        remember {
            MyListViewModel(
                store = deps.myList,
                channelDao = deps.sources.channelDao,
                lastChannel = deps.keyValueStore,
                clock = deps.clock,
                zone = TimeZone.getDefault(),
                scope = scope,
            )
        }
    DisposableEffect(Unit) { onDispose { scope.cancel() } }
    val rows by viewModel.rows.collectAsState()
    var described by remember { mutableStateOf<MyListRow?>(null) }
    BackHandler(enabled = described != null) { described = null }
    MyListScreenScaffold(
        title = stringResource(R.string.mylist_title),
        hint = stringResource(R.string.mylist_remove_hint).takeIf { rows.isNotEmpty() },
    ) {
        if (rows.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TellyScreenMutedText(text = stringResource(R.string.mylist_empty), fontSize = 18.sp)
            }
        } else {
            MyListScreenList(rows, viewModel, onTuned, onDescribe = { described = it })
        }
        described?.let { row ->
            OnboardingScreenMessage(
                headline = row.entry.title,
                subtitle = row.entry.description ?: PlayerMenu.NO_INFORMATION,
            )
        }
    }
}
