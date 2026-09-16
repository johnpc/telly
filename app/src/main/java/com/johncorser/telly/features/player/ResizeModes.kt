package com.johncorser.telly.features.player

import androidx.media3.ui.AspectRatioFrameLayout

/**
 * Maps the persisted "Resize mode" raw value (store-only key; captured
 * TiviMate default "Fit") onto Media3 surface resize modes. Unknown values
 * fall back to Fit — the previously hardcoded behavior.
 */
object ResizeModes {
    fun of(raw: String): Int =
        when (raw.trim().lowercase()) {
            "stretch", "fill" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            "crop", "zoom" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            "fixed width" -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            "fixed height" -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
}
