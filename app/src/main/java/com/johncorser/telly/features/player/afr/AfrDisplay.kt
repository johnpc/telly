package com.johncorser.telly.features.player.afr

/** One display mode, mirrored from android.view.Display.Mode. */
data class AfrMode(
    val id: Int,
    val width: Int,
    val height: Int,
    val refreshRate: Float,
)

/** The display's current mode plus everything it supports. */
data class AfrModeSet(
    val current: AfrMode,
    val all: List<AfrMode>,
)

/**
 * Seam between AFR logic and the Activity window: production reads
 * Display.getMode()/getSupportedModes() and writes
 * window.attributes.preferredDisplayModeId (MainActivity glue); tests
 * inject a fake.
 */
interface AfrDisplay {
    /** Null when no display is attached yet. */
    fun modes(): AfrModeSet?

    fun apply(modeId: Int)
}
