package com.johncorser.telly.features.playback

import com.johncorser.telly.features.playlist.db.ChannelEntity

// The panel sheet's channel-flag and My-list legs, split from
// PlaybackMenuHandler to keep its class under the function-count gate.

/** Favorite toggling persists and dismisses the sheet. */
internal fun PlaybackMenuHandler.toggleFavorite(channel: ChannelEntity) {
    val next = afterAction(overlays.value)
    actions.channels.toggleFavorite(channel)
    overlays.set(next)
}

/** Hiding the watched channel zaps away first, then persists the flag. */
internal fun PlaybackMenuHandler.hide(channel: ChannelEntity) {
    val next = afterAction(overlays.value)
    tuner.zapAwayFrom(channel)
    actions.channels.hide(channel)
    overlays.set(next)
}

/**
 * The My-list rows: toggle saves/removes the row's airing programme with
 * the sheet dismissed; the management rows open their screens over bare
 * playback like Search.
 */
internal fun PlaybackMenuHandler.runMyList(
    route: PlayerMenuRoute,
    channel: ChannelEntity,
) {
    if (route == PlayerMenuRoute.MY_LIST_TOGGLE) {
        val next = afterAction(overlays.value)
        actions.myList?.toggleFor(channel)
        overlays.set(next)
    } else {
        openScreen { actions.myList?.run(route, channel) }
    }
}

/**
 * The Block/Unblock PIN dialog's commit: a verified (or freshly set) PIN
 * flips the flag and lands where favorite/hide land; a wrong PIN keeps the
 * dialog up.
 */
fun PlaybackMenuHandler.submitBlockPin(pin: String) {
    val dialog = overlays.value as? PlaybackOverlay.BlockPin ?: return
    if (actions.blocker.submit(pin, dialog.mode, dialog.channel)) {
        overlays.set(afterAction(dialog.back))
    }
}

/**
 * Where a row that dismisses the sheet lands: the panel behind the channel
 * menu, bare playback otherwise. Channel options shares this — the pane
 * replaces the sheet, so its BACK target is the panel too.
 */
internal fun afterAction(current: PlaybackOverlay): PlaybackOverlay =
    if (current is PlaybackOverlay.ChannelMenu) PlaybackOverlay.Panel else PlaybackOverlay.None
