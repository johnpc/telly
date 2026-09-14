package com.johncorser.telly.core.input

/**
 * Splits a key's down/up events into a tap or a hold: the first auto-repeat
 * of a held key fires [hold] once; a release without repeats fires [tap].
 * Pure logic so key mappings stay unit-testable. A null [hold] swallows the
 * long press entirely (the tap still doesn't fire after a hold).
 */
class HoldKeyDetector<T : Any>(
    private val tap: T,
    private val hold: T?,
) {
    private var holdFired = false

    /** Key-down with the native repeat count; [hold] at most once per press. */
    fun onDown(repeatCount: Int): T? {
        if (repeatCount == 0) {
            holdFired = false
            return null
        }
        if (holdFired) return null
        holdFired = true
        return hold
    }

    /** Key-up: [tap] for a short press, nothing after a hold. */
    fun onUp(): T? {
        val wasHold = holdFired
        holdFired = false
        return if (wasHold) null else tap
    }
}
