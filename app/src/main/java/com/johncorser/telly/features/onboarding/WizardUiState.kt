package com.johncorser.telly.features.onboarding

/** Wizard pages, in forward order. */
enum class WizardStep { TYPE_CHOOSER, URL_ENTRY, PROCESSING, DONE }

/** Playlist source kinds offered by the type chooser. */
enum class PlaylistType { M3U, XTREAM_CODES, STALKER_PORTAL }

/** User-visible wizard failures; the UI maps them to string resources. */
enum class WizardError { INVALID_URL, LOAD_FAILED }

/** Immutable snapshot of the add-playlist wizard. */
data class WizardUiState(
    val step: WizardStep = WizardStep.TYPE_CHOOSER,
    val url: String = "",
    val error: WizardError? = null,
    val channelCount: Int = 0,
)
