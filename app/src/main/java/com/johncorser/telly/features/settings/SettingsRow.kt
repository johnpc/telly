package com.johncorser.telly.features.settings

/**
 * One row of a settings pane, matching the captured TiviMate row types:
 * toggle, value/choice (opens a picker), plain action, blue group header,
 * and the blue premium note. `locked` renders the dimmed padlock treatment
 * (grey text, padlock icon, not focusable) exactly as in the reference.
 */
sealed interface SettingsRow {
    val id: String

    data class Toggle(
        override val id: String,
        val title: String,
        val checked: Boolean,
        val summary: String? = null,
        val locked: Boolean = false,
    ) : SettingsRow

    data class Value(
        override val id: String,
        val title: String,
        val summary: String? = null,
        val locked: Boolean = false,
        val selected: Boolean = false,
        /** Leading circle-check (enabled playlist rows, ref/05). */
        val checkIcon: Boolean = false,
    ) : SettingsRow

    data class Action(
        override val id: String,
        val title: String,
        val locked: Boolean = false,
        val premiumKey: Boolean = false,
    ) : SettingsRow

    data class Header(
        val text: String,
    ) : SettingsRow {
        override val id: String get() = "header:$text"
    }

    data class Note(
        val text: String,
        val accent: Boolean = true,
    ) : SettingsRow {
        override val id: String get() = "note:$text"
    }
}

/** The blue note every captured pane repeats above its first row. */
const val PREMIUM_NOTE = "All features are available in Premium version"

/** The "Unlock Premium" row (key icon) every captured pane starts with. */
fun unlockPremiumRow(): SettingsRow =
    SettingsRow.Action(
        id = RowIds.UNLOCK_PREMIUM,
        title = "Unlock Premium",
        premiumKey = true,
    )

/** Shared prelude: premium note + Unlock Premium, as captured on every pane. */
fun panePrelude(): List<SettingsRow> = listOf(SettingsRow.Note(PREMIUM_NOTE), unlockPremiumRow())
