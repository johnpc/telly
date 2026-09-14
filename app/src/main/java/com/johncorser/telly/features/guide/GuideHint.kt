package com.johncorser.telly.features.guide

import com.johncorser.telly.core.kv.KeyValueStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * The guide's first-open hint toast (captures 24/26/27: "Long OK: open
 * menu / Left: show groups / Long Left: navigate to past programs" at the
 * bottom right). Shown once ever — the store remembers — and auto-hidden
 * on the injected scope so logic never reads the wall clock.
 */
class GuideHint(
    private val store: KeyValueStore,
) {
    /** Emits true while the toast should show; flips itself off. */
    fun startIn(scope: CoroutineScope): StateFlow<Boolean> {
        val visible = MutableStateFlow(store.getLong(KEY) == null)
        if (visible.value) {
            store.putLong(KEY, 1L)
            scope.launch {
                delay(HIDE_AFTER_MS)
                visible.value = false
            }
        }
        return visible
    }

    companion object {
        const val KEY = "guide.hint.shown"
        const val HIDE_AFTER_MS = 15_000L
    }
}
