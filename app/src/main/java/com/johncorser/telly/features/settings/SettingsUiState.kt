package com.johncorser.telly.features.settings

/** What the right pane is showing: a section, or a pushed sub-pane. */
sealed interface SettingsPane {
    data class Section(
        val section: SettingsSection,
    ) : SettingsPane

    data class PlaylistDetail(
        val url: String,
    ) : SettingsPane

    data object EpgSources : SettingsPane
}

/** Modal state over the two-pane shell. */
sealed interface SettingsOverlay {
    data class Picker(
        val spec: PickerSpec,
        val current: String,
    ) : SettingsOverlay

    data class TextEdit(
        val rowId: String,
        val title: String,
        val value: String,
    ) : SettingsOverlay

    data class ConfirmDelete(
        val url: String,
        val name: String,
    ) : SettingsOverlay

    data object PinSetup : SettingsOverlay

    data object Paywall : SettingsOverlay
}

data class SettingsUiState(
    val section: SettingsSection = SettingsSection.GENERAL,
    val subPanes: List<SettingsPane> = emptyList(),
    val overlay: SettingsOverlay? = null,
) {
    /** The pane the right side renders: the deepest push, else the section. */
    val activePane: SettingsPane get() = subPanes.lastOrNull() ?: SettingsPane.Section(section)

    /** True while a sub-pane or overlay consumes BACK before navigation. */
    val consumesBack: Boolean get() = overlay != null || subPanes.isNotEmpty()
}
