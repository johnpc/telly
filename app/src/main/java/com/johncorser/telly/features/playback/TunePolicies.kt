package com.johncorser.telly.features.playback

import com.johncorser.telly.features.player.external.ExternalPlayer

/**
 * The tune-time policies: the external-player handoff, the blocked-PIN gate
 * and the stream-URL resolution (UDP-proxy rewrite; identity by default).
 * The resolve applies to LIVE tunes and retunes only — catch-up archive
 * URLs are always http(s) built from templates, never udp, so they skip it.
 */
data class TunePolicies(
    val external: ExternalPlayer = ExternalPlayer.OFF,
    /** Owns the PIN prompt for blocked channels ([TuneBlockPrompt] drives it). */
    val gate: BlockGate = BlockGate(),
    val resolveUrl: (String) -> String = { it },
)
