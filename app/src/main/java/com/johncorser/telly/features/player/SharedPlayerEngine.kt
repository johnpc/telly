package com.johncorser.telly.features.player

/**
 * Refcounted lease over the live-TV engine so the guide preview and the
 * fullscreen player hand the SAME stream across their route crossfade —
 * both screens are composed while the fade runs, so the incoming lease
 * overlaps the outgoing one and the pipeline never tears down mid-switch.
 * Built lazily on the first acquire; the last release frees the codecs
 * (and a changed Playback setting applies on the next build), so
 * multiview, VOD and recordings keep their own engines untouched.
 */
class SharedPlayerEngine<T : PlayerEngine>(
    private val build: () -> T,
) {
    private var engine: T? = null
    private var leases = 0

    /** The live engine, built on the first lease of a cycle. */
    fun acquire(): T {
        leases += 1
        return engine ?: build().also { engine = it }
    }

    /** Drops one lease; the last one releases the underlying engine. */
    fun release() {
        if (leases == 0) return
        leases -= 1
        if (leases > 0) return
        engine?.release()
        engine = null
    }
}
