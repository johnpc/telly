package com.johncorser.telly.features.vod

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.johncorser.telly.core.ui.TellyScreenKeyAnchor

/**
 * The layers over the video surface: while playing, the invisible key
 * anchor + the auto-hiding transport; while a stored position waits, the
 * Resume/Start-over step.
 */
@Composable
internal fun BoxScope.VodPlaybackScreenLayers(viewModel: VodPlaybackViewModel) {
    val stage by viewModel.stage.collectAsState()
    if (stage == VodStage.Playing) {
        TellyScreenKeyAnchor { event -> onVodPlaybackKey(event, viewModel.controls) }
        VodPlaybackScreenOverlay(viewModel, Modifier.align(Alignment.BottomCenter))
    }
    if (stage is VodStage.ResumePrompt) {
        VodPlaybackScreenResume(viewModel)
    }
}

/** The transport, composed only while visible (auto-hide idiom). */
@Composable
private fun VodPlaybackScreenOverlay(
    viewModel: VodPlaybackViewModel,
    modifier: Modifier,
) {
    val visible by viewModel.controls.transport.visible.collectAsState()
    if (visible) {
        VodPlaybackScreenTransport(viewModel, modifier)
    }
}
