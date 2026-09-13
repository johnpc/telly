package com.johncorser.telly.features.onboarding

/** Wizard pages, in forward order. */
enum class WizardStep { TYPE_CHOOSER, URL_ENTRY, PROCESSING, PROCESSED, DONE }

/** Playlist source kinds offered by the type chooser. */
enum class PlaylistType { M3U, XTREAM_CODES, STALKER_PORTAL }

/** TV/VOD radio choice on the processed step (UI state only for now). */
enum class PlaylistKind { TV, VOD }

/** User-visible wizard failures; the UI maps them to string resources. */
enum class WizardError { INVALID_URL, LOAD_FAILED }

/** Immutable snapshot of the add-playlist wizard. */
data class WizardUiState(
    val step: WizardStep = WizardStep.TYPE_CHOOSER,
    val url: String = "",
    val error: WizardError? = null,
    val name: String = "",
    val kind: PlaylistKind = PlaylistKind.TV,
    val liveCount: Int = 0,
    val movieCount: Int = 0,
    val groupCount: Int = 0,
) {
    val channelCount: Int get() = liveCount + movieCount
}
