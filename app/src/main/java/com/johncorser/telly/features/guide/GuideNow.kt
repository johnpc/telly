package com.johncorser.telly.features.guide

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The guide's "now": seeded from the injected clock, re-sampled on every
 * element of [ticks] (production passes `PlaybackTime.minuteTicks` — the
 * reference header clock ticks per minute, round7 resume verification) and
 * explicitly via [reseed] when the activity returns to the foreground after
 * a background stop. No wall-clock reads in logic: only the injected clock;
 * tests inject their own tick flow and drive it deterministically.
 */
class GuideNow(
    private val clock: () -> Long,
    scope: CoroutineScope,
    ticks: Flow<Unit>,
) {
    private val mutable = MutableStateFlow(clock())

    val now: StateFlow<Long> = mutable.asStateFlow()

    init {
        scope.launch { ticks.collect { reseed() } }
    }

    /** Re-samples the injected clock (minute tick + foreground resume). */
    fun reseed() {
        mutable.value = clock()
    }
}
