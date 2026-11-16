package com.johncorser.telly.core.state

/**
 * Marks the ONE type that owns a mode's state (a sealed interface, enum or
 * class) and names the row it has in `docs/state-machines.md`.
 * `scripts/check-state-machines.mjs` cross-checks both directions: every
 * marker must have a catalog row of the same name, and every catalog row
 * that is not a listed gap must have a marker. Source-only — it exists for
 * the gate and for the next reader, never at runtime.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class StateMachine(
    val name: String,
)
