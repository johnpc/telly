package com.johncorser.telly.features.search

/**
 * The dropdown rows for OK on a programme result — the guide-cell set
 * (capture 27). In the free reference every row leads to Unlock Premium;
 * telly routes them to the shared paywall, exactly like the guide's cells.
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

    /** The shared Unlock Premium screen (capture 28) for premium-gated rows. */
    data object Paywall : SearchOverlay

    /** Branded "coming soon" interstitial for not-yet-built destinations. */
    data class ComingSoon(
        val feature: String,
    ) : SearchOverlay
}
