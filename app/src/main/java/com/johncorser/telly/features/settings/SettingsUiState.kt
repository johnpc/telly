package com.johncorser.telly.features.settings

/** One pushed sheet: a section, or a deeper sub-sheet. */
sealed interface SettingsPane {
    data class Section(
        val section: SettingsSection,
    ) : SettingsPane

    data class PlaylistDetail(
        val url: String,
    ) : SettingsPane

    data object EpgSources : SettingsPane

    data class EpgSourceDetail(
        val sourceId: Long,
    ) : SettingsPane

    data object AppearanceTvGuide : SettingsPane

    data object AppearancePlayer : SettingsPane

    data object AppearanceGroups : SettingsPane

    data object AppearanceLogos : SettingsPane
}

/** Modal state over the sheet stack. */
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

    data class ConfirmDeleteSource(
        val sourceId: Long,
        val name: String,
    ) : SettingsOverlay

    data object PinSetup : SettingsOverlay
}

/**
 * The right-sheet stack (device-verified 2026-09-13): settings is a single
 * 360 dp sheet at the screen's right edge over the dimmed underlying
 * surface. The root sheet lists the sections; OK pushes a section sheet
 * that REPLACES the root in place, BACK pops one sheet at a time and
 * leaves settings from the root. [panes] holds the pushed sheets; empty
 * means the root section list.
 */
data class SettingsUiState(
    val panes: List<SettingsPane> = emptyList(),
    val overlay: SettingsOverlay? = null,
) {
    /** The sheet on top: the deepest push, or null for the root list. */
    val activePane: SettingsPane? get() = panes.lastOrNull()

    /** True while an overlay or pushed sheet consumes BACK before leaving. */
    val consumesBack: Boolean get() = overlay != null || panes.isNotEmpty()
}
