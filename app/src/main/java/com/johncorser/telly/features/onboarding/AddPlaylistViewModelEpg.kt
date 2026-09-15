package com.johncorser.telly.features.onboarding

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * The wizard's EPG step (capture 13): the url-tvg from the M3U header is
 * pre-filled into "Enter URL"; the value committed on Done is what telly
 * fetches — overriding url-tvg when edited. A blank URL skips the EPG
 * ("Optional; skippable", ux-spec 2 step 5); custom sources can still be
 * added later under Settings -> EPG -> EPG sources.
 */

fun AddPlaylistViewModel.setEpgUrl(url: String) {
    mutableState.update { it.copy(epgUrl = url, error = null) }
}

/** "Paste playlist URL" (capture 13): copies the playlist URL to edit. */
fun AddPlaylistViewModel.pastePlaylistUrl() {
    setEpgUrl(state.value.url.trim())
}

/** Done: persist the playlist with the (possibly edited) EPG URL, finish. */
fun AddPlaylistViewModel.finishEpg() {
    val playlist = parsed ?: return
    val epgUrl = state.value.epgUrl.trim()
    if (epgUrl.isNotEmpty() && epgUrl.toHttpUrlOrNull() == null) {
        mutableState.update { it.copy(error = WizardError.INVALID_URL) }
        return
    }
    scope.launch {
        repository.add(
            state.value.url.trim(),
            playlist.copy(epgUrl = epgUrl.ifEmpty { null }),
            state.value.name.trim().ifEmpty { null },
        )
        mutableState.update { it.copy(step = WizardStep.DONE) }
    }
}
