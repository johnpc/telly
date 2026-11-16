package com.johncorser.telly.features.onboarding

import kotlinx.coroutines.flow.update

// The wizard's draft edits: text/choice fields that change no step. Split
// from AddPlaylistViewModel, which keeps the step transitions.

fun AddPlaylistViewModel.setUrl(url: String) {
    mutableState.update { it.copy(url = url, error = null) }
}

fun AddPlaylistViewModel.setName(name: String) {
    mutableState.update { it.copy(name = name) }
}

fun AddPlaylistViewModel.chooseKind(kind: PlaylistKind) {
    mutableState.update { it.copy(kind = kind) }
}
