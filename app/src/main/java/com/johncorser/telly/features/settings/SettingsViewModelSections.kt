package com.johncorser.telly.features.settings

import kotlinx.coroutines.flow.update

// Section navigation: the root list's pushes and their parental PIN gates.

/** OK on a section row pushes its sheet over the root list. */
fun SettingsViewModel.selectSection(section: SettingsSection) {
    mutableState.update { it.copy(panes = listOf(SettingsPane.Section(section))) }
}

/** Section entry, gated: "Require PIN for Settings | Playlists" prompts. */
internal fun SettingsViewModel.openSection(section: SettingsSection) {
    if (section == SettingsSection.PLAYLISTS && parental.isPlaylistsLocked()) {
        showOverlay(SettingsOverlay.PinVerify(section))
    } else {
        selectSection(section)
    }
}
