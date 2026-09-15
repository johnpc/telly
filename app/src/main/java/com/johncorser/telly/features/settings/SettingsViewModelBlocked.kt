package com.johncorser.telly.features.settings

// Blocked-channels pane + Other -> Search dispatch: the pane entry is
// PIN-gated ONCE (while a PIN exists), OK on a listed channel unblocks it,
// and the confirmed clear action empties the search history store.

/** The block/search leg of the action dispatch (split from runAction). */
internal fun SettingsViewModel.runBlockedOrSearchAction(rowId: String) {
    when (rowId) {
        RowIds.PARENTAL_BLOCKED_CHANNELS -> openBlockedChannels()
        RowIds.OTHER_SEARCH -> push(SettingsPane.OtherSearch)
        RowIds.SEARCH_CLEAR_HISTORY -> showOverlay(SettingsOverlay.ConfirmClearHistory)
        else -> runPlaylistAction(rowId)
    }
}

/** "Blocked channels" row: confirm the PIN once, then push the pane. */
private fun SettingsViewModel.openBlockedChannels() {
    if (parental.hasPin) {
        showOverlay(SettingsOverlay.PinVerify)
    } else {
        push(SettingsPane.BlockedChannels)
    }
}

/** The pane-entry PIN dialog's commit; wrong PINs keep prompting. */
fun SettingsViewModel.submitVerifyPin(pin: String) {
    if (state.value.overlay != SettingsOverlay.PinVerify || !parental.verifyPin(pin)) return
    dismissOverlay()
    push(SettingsPane.BlockedChannels)
}

/** OK on a blocked-channel row unblocks it (pane entry already PIN-gated). */
internal fun SettingsViewModel.unblockChannel(rowId: String) {
    val channelId = rowId.removePrefix(RowIds.BLOCKED_CHANNEL_PREFIX).toLongOrNull() ?: return
    val channel = blockedItems.value.firstOrNull { it.id == channelId } ?: return
    launch { blocked?.unblock(channel) }
}

/** OK on Clear in the GuidedStep confirm empties the history store. */
fun SettingsViewModel.confirmClearHistory() {
    if (state.value.overlay != SettingsOverlay.ConfirmClearHistory) return
    searchHistory?.clear()
    dismissOverlay()
}
