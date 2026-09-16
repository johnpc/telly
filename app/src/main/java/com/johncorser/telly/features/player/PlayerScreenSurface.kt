package com.johncorser.telly.features.player

import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.johncorser.telly.R

/**
 * Thin AndroidView host for Media3's PlayerView; zero controller chrome.
 * [textureView] renders through a TextureView instead of the default
 * SurfaceView — multiview panes need it because stacked SurfaceViews exhaust
 * the TV's hardware overlay planes and go black (surface_type is XML-only, so
 * the TextureView path inflates a layout).
 */
@Composable
fun PlayerScreenSurface(
    engine: Media3PlayerEngine,
    modifier: Modifier = Modifier,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    textureView: Boolean = false,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val view =
                if (textureView) {
                    LayoutInflater.from(context).inflate(R.layout.player_texture_surface, null) as PlayerView
                } else {
                    PlayerView(context)
                }
            view.apply {
                useController = false
                // Seamless zap: keep the old channel's frame while the next
                // stream tunes — no black gap (round3 P0 item 2).
                setKeepContentOnPlayerReset(true)
            }
        },
        update = {
            it.player = engine.player
            it.resizeMode = resizeMode
        },
    )
}
