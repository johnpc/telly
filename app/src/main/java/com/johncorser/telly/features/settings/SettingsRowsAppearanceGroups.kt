package com.johncorser.telly.features.settings

import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings

/**
 * Appearance -> Groups and Appearance -> Logos sub-panes. Groups toggles
 * cover telly's two synthetic groups (Favorites + All channels; there is
 * no synthetic History group since the history-round2 rework, so no dead
 * toggle for it). Logos defaults keep today's #2C5F8A 4 dp-rounded tile.
 */
fun appearanceGroupsRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Toggle(
            id = RowIds.APPEARANCE_GROUPS_ALL_CHANNELS,
            title = "Show 'All channels' group",
            checked = s.get(TellySettings.SHOW_ALL_CHANNELS_GROUP),
        ),
        SettingsRow.Toggle(
            id = RowIds.APPEARANCE_GROUPS_FAVORITES,
            title = "Show 'Favorites' group",
            checked = s.get(TellySettings.SHOW_FAVORITES_GROUP),
        ),
    )

fun appearanceLogosRows(s: SettingsRepository): List<SettingsRow> =
    listOf(
        SettingsRow.Value(
            id = RowIds.APPEARANCE_LOGOS_BACKGROUND,
            title = "Logo background",
            summary = s.get(TellySettings.LOGO_BACKGROUND),
        ),
        SettingsRow.Toggle(
            id = RowIds.APPEARANCE_LOGOS_ROUNDED,
            title = "Rounded corners",
            checked = s.get(TellySettings.LOGO_ROUNDED_CORNERS),
        ),
    )

/** The Appearance rows that push a sub-pane, for the action dispatcher. */
fun appearancePaneFor(rowId: String): SettingsPane? =
    when (rowId) {
        RowIds.APPEARANCE_TV_GUIDE -> SettingsPane.AppearanceTvGuide
        RowIds.APPEARANCE_PLAYER -> SettingsPane.AppearancePlayer
        RowIds.APPEARANCE_GROUPS -> SettingsPane.AppearanceGroups
        RowIds.APPEARANCE_LOGOS -> SettingsPane.AppearanceLogos
        else -> null
    }

/** The four Appearance sub-panes' rows, for [rowsFor]'s pane routing. */
fun appearancePaneRows(
    pane: SettingsPane,
    settings: SettingsRepository,
): List<SettingsRow> =
    when (pane) {
        SettingsPane.AppearanceTvGuide -> appearanceTvGuideRows(settings)
        SettingsPane.AppearancePlayer -> appearancePlayerRows(settings)
        SettingsPane.AppearanceGroups -> appearanceGroupsRows(settings)
        SettingsPane.AppearanceLogos -> appearanceLogosRows(settings)
        else -> emptyList()
    }

/** The four Appearance sub-panes' sheet titles, for [paneTitle]. */
fun appearancePaneTitle(pane: SettingsPane): String =
    when (pane) {
        SettingsPane.AppearanceTvGuide -> "TV guide"
        SettingsPane.AppearancePlayer -> "Player"
        SettingsPane.AppearanceGroups -> "Groups"
        SettingsPane.AppearanceLogos -> "Logos"
        else -> ""
    }
