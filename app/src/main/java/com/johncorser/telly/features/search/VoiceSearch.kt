package com.johncorser.telly.features.search

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The mic orb's voice-input seam (TiviMate launches Android voice search):
 * [start] fires the platform recognizer through the injected [launch]
 * (MainActivity's ActivityResult glue) and a device without one degrades
 * to [notify] (a toast); a recognized transcript lands in [transcripts]
 * for the search screen to consume into the query. Pure decision logic —
 * no Android types.
 */
class VoiceSearch(
    private val launch: () -> Boolean = { false },
    private val notify: (String) -> Unit = {},
) {
    private val mutable = MutableStateFlow<String?>(null)

    /** The latest unconsumed transcript, or null. */
    val transcripts: StateFlow<String?> = mutable.asStateFlow()

    /** Orb OK: launch the recognizer, or say why nothing happened. */
    fun start() {
        if (!launch()) notify(UNAVAILABLE_MESSAGE)
    }

    /** The recognizer's result; blank/cancelled results are dropped. */
    fun onResult(text: String?) {
        if (!text.isNullOrBlank()) mutable.value = text
    }

    /** Hands the transcript to the query exactly once. */
    fun consume(): String? {
        val text = mutable.value
        mutable.value = null
        return text
    }

    companion object {
        /** Shown when the device has no speech recognizer (TV emulators). */
        const val UNAVAILABLE_MESSAGE = "Voice search is not available on this device"
    }
}
