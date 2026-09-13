package com.johncorser.telly.features.playback

/**
 * Splits DPAD-center down/up events into a single OK or LONG_OK: the first
 * auto-repeat of a held key fires LONG_OK once; a release without repeats
 * fires OK. Pure logic so the key mapping stays unit-testable.
 */
class OkLongPressDetector {
    private var longFired = false

    /** Key-down with the native repeat count; LONG_OK exactly once per hold. */
    fun onDown(repeatCount: Int): PlaybackKey? {
        if (repeatCount == 0) {
            longFired = false
            return null
        }
        if (longFired) return null
        longFired = true
        return PlaybackKey.LONG_OK
    }

    /** Key-up: OK for a short press, nothing after a long one. */
    fun onUp(): PlaybackKey? {
        val wasLong = longFired
        longFired = false
        return if (wasLong) null else PlaybackKey.OK
    }
}
