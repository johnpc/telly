package com.johncorser.telly.features.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/** Thin AndroidView host for Media3's PlayerView; zero controller chrome. */
@Composable
fun PlayerScreenSurface(
    engine: Media3PlayerEngine,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                // Seamless zap: keep the old channel's frame while the next
                // stream tunes — no black gap (round3 P0 item 2).
                setKeepContentOnPlayerReset(true)
            }
        },
        update = { it.player = engine.player },
    )
}
