package com.johncorser.telly.features.recording

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.johncorser.telly.core.ui.TellyScreenKeyAnchor
import com.johncorser.telly.features.player.PlayerScreenSurface
import kotlinx.coroutines.delay
import java.io.File

/**
 * Fullscreen playback of a DONE capture (Media3 over the file URI, the
 * MultiviewScreen engine-usage precedent) with a seek transport:
 * LEFT/RIGHT skip ±10 s, OK toggles pause, BACK returns to the library.
 */
@Composable
internal fun RecordingScreenPlayer(
    deps: RecordingDeps,
    row: RecordingRow,
    onExit: () -> Unit,
) {
    val engine = remember { deps.engineFactory() }
    DisposableEffect(Unit) { onDispose { engine.release() } }
    LaunchedEffect(Unit) { engine.load(Uri.fromFile(File(row.entry.filePath)).toString()) }
    var positionMs by remember { mutableStateOf(0L) }
    var durationMs by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            positionMs = engine.player.currentPosition.coerceAtLeast(0)
            durationMs = engine.player.duration.coerceAtLeast(0)
            delay(TRANSPORT_POLL_MS)
        }
    }
    BackHandler { onExit() }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("recording-player"),
    ) {
        PlayerScreenSurface(engine, Modifier.fillMaxSize())
        TellyScreenKeyAnchor { event -> onRecordingPlayerKey(event, engine.player) }
        RecordingScreenTransport(
            title = row.entry.title,
            positionMs = positionMs,
            durationMs = durationMs,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

private const val TRANSPORT_POLL_MS = 500L
