package com.johncorser.telly.features.player

import androidx.media3.common.MediaItem

/**
 * Builds the [MediaItem] behind every stream tune. Live playback speed is
 * pinned to 1.0×: Media3 otherwise chases the target live offset of a live
 * HLS stream by varying playback speed inside its 0.97–1.03 fallback
 * bounds, and every catch-up burst after network jitter shows up as video
 * judder plus resampled audio. TiviMate never chases the live edge — it
 * just drifts behind — so neither does telly. Non-live sources ignore the
 * live configuration entirely.
 */
internal fun streamMediaItem(streamUrl: String): MediaItem =
    MediaItem
        .Builder()
        .setUri(streamUrl)
        .setLiveConfiguration(
            MediaItem.LiveConfiguration
                .Builder()
                .setMinPlaybackSpeed(1f)
                .setMaxPlaybackSpeed(1f)
                .build(),
        ).build()
