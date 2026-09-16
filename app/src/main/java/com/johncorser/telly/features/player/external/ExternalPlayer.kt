package com.johncorser.telly.features.player.external

/**
 * External-player policy (ux-spec §3.18): the channel sheet's "Open in
 * external player" row always fires the chooser, while ordinary tunes
 * bounce to the external app only when the "Use external player" setting
 * is On — a per-channel Channel-options override wins over the global
 * setting when present. The actual ACTION_VIEW launch is injected so this
 * stays plain-JVM testable ([ExternalPlayerActivityLauncher] is the
 * production launcher).
 */
class ExternalPlayer(
    private val enabledForTuning: () -> Boolean,
    private val launch: (String) -> Boolean,
) {
    /** True when a tune should hand off: per-channel first, global otherwise. */
    val enabledByDefault: Boolean get() = enabledForTuning()

    /** The explicit sheet row: fires whenever the stream URL is usable. */
    fun open(streamUrl: String): Boolean = streamUrl.isNotBlank() && launch(streamUrl)

    /** Tune-time policy: true when the external app took over playback. */
    fun maybeLaunch(
        streamUrl: String,
        channelOverride: Boolean? = null,
    ): Boolean = (channelOverride ?: enabledForTuning()) && open(streamUrl)

    companion object {
        /** Inert default for hosts without platform glue (tests, previews). */
        val OFF = ExternalPlayer(enabledForTuning = { false }, launch = { false })
    }
}
