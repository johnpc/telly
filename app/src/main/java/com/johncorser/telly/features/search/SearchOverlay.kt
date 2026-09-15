package com.johncorser.telly.features.search

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The dropdown rows for OK on a programme result — the guide-cell set
 * (capture 27). Each is an unbuilt feature: they route to the branded
 * coming-soon placeholder, exactly like the guide's cells.
 */
enum class SearchProgramAction(
    val label: String,
) {
    REMIND("Remind"),
    RECORD("Record"),
    CUSTOM_RECORDING("Custom recording"),
    ADD_TO_MY_LIST("Add to My list"),
    PROGRAM_DESCRIPTION("Program description"),
}

/** What covers the search screen: nothing, the dropdown, or a placeholder. */
sealed interface SearchOverlay {
    data object None : SearchOverlay

    /** Anchored action dropdown on the focused programme row. */
    data class ProgramMenu(
        val hit: SearchProgramHit,
    ) : SearchOverlay

    /** Branded "coming soon" interstitial for not-yet-built destinations. */
    data class ComingSoon(
        val feature: String,
    ) : SearchOverlay
}

/**
 * The search screen's overlay stack (one deep): the programme dropdown and
 * the voice-search placeholder show through here.
 */
class SearchOverlays {
    private val mutable = MutableStateFlow<SearchOverlay>(SearchOverlay.None)

    val current: StateFlow<SearchOverlay> = mutable.asStateFlow()

    fun show(overlay: SearchOverlay) {
        mutable.value = overlay
    }

    /** BACK with an overlay up closes just the overlay. */
    fun dismiss(): Boolean {
        if (mutable.value == SearchOverlay.None) return false
        mutable.value = SearchOverlay.None
        return true
    }
}
