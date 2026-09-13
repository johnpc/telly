package com.johncorser.telly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.johncorser.telly.core.navigation.Navigator
import com.johncorser.telly.features.playlist.InMemoryPlaylistRepository
import com.johncorser.telly.features.playlist.M3uFetcher

/** Single-activity entry point; all UI is Compose for TV. */
class MainActivity : ComponentActivity() {
    private val navigator = Navigator()
    private val repository = InMemoryPlaylistRepository()
    private val fetcher = M3uFetcher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RootScreen(
                navigator = navigator,
                repository = repository,
                fetchPlaylist = fetcher::fetch,
            )
        }
    }
}
