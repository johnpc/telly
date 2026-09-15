package com.johncorser.telly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Thin lifecycle bridge for screens that must react to the activity going
 * to the background: [onStop] fires on every ON_STOP, [onStart] only on the
 * ON_START that follows one (observer registration replays the current
 * state, so a fresh composition never sees a spurious start).
 */
@Composable
fun ScreenLifecycleStartStop(
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        var stopped = false
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> {
                        stopped = true
                        onStop()
                    }
                    Lifecycle.Event.ON_START ->
                        if (stopped) {
                            stopped = false
                            onStart()
                        }
                    else -> Unit
                }
            }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
