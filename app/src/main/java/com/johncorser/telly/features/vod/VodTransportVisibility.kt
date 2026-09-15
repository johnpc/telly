package com.johncorser.telly.features.vod

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The seek transport's auto-hide: any interaction shows it, it fades after
 * [HIDE_AFTER_MS] while playing, and it stays up while paused (the info
 * overlay's five-second idiom, applied to VOD).
 */
class VodTransportVisibility(
    private val scope: CoroutineScope,
) {
    private val mutableVisible = MutableStateFlow(false)
    val visible: StateFlow<Boolean> = mutableVisible.asStateFlow()

    private var hideJob: Job? = null

    fun poke(paused: Boolean) {
        mutableVisible.value = true
        hideJob?.cancel()
        if (!paused) {
            hideJob =
                scope.launch {
                    delay(HIDE_AFTER_MS)
                    mutableVisible.value = false
                }
        }
    }

    companion object {
        const val HIDE_AFTER_MS = 5_000L
    }
}
