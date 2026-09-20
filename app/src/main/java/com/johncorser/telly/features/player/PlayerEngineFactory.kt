package com.johncorser.telly.features.player

/**
 * Builds one independent [PlayerEngine] per call. Multiview composes N
 * engines through [PlayerEnginePool]; the guide preview and fullscreen
 * playback lease one shared build via [SharedPlayerEngine] instead.
 */
fun interface PlayerEngineFactory {
    fun create(): PlayerEngine
}

/**
 * Owns the engines the multiview grid composes: one per pane, released
 * individually when a pane is removed and all together when the screen
 * leaves composition — no engine ever leaks a codec.
 */
class PlayerEnginePool(
    private val factory: PlayerEngineFactory,
) {
    private val engines = mutableListOf<PlayerEngine>()

    val size: Int get() = engines.size

    fun acquire(): PlayerEngine = factory.create().also { engines += it }

    fun release(engine: PlayerEngine) {
        if (engines.remove(engine)) engine.release()
    }

    fun releaseAll() {
        engines.forEach { it.release() }
        engines.clear()
    }
}
