package com.johncorser.telly.features.settings

import com.johncorser.telly.core.state.StateMachine

/**
 * Transient state of the manual "Update ..." action rows (Update playlist /
 * Update all playlists / Update EPG): the row whose work is in flight, and
 * the completion message shown briefly once it lands.
 */
@StateMachine("Manual refresh")
data class RefreshStatus(
    val busyRowId: String? = null,
    val message: String? = null,
)

/** Marks the in-flight action row so the sheet renders its spinner. */
fun List<SettingsRow>.markBusy(busyRowId: String?): List<SettingsRow> =
    if (busyRowId == null) {
        this
    } else {
        map { row ->
            if (row is SettingsRow.Action && row.id == busyRowId) row.copy(busy = true) else row
        }
    }
