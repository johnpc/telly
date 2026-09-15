package com.johncorser.telly.features.playback

import com.johncorser.telly.features.mylist.MyListMenuHost
import com.johncorser.telly.features.panel.PanelRow
import com.johncorser.telly.features.playlist.db.ChannelEntity
import com.johncorser.telly.features.recording.RecordingMenu
import com.johncorser.telly.features.recording.RecordingPrompt

/**
 * Executes the panel sheet's rows through the shared [PlayerMenuRouting]
 * table (the guide's sheet consumes the same one, so the two can never
 * drift): favorites/hide persist via [ChannelActions], Search and Settings
 * clear the overlay chrome first, Program description pushes a screen whose
 * BACK pops back to the sheet, Channel options REPLACES the sheet — its
 * BACK lands directly on the panel, never back on the sheet (ref-round6
 * §A) — and every unbuilt row keeps the coming-soon placeholder.
 */
class PlaybackMenuHandler(
    private val actions: ChannelActions,
    private val overlays: OverlayState,
    private val tuner: TuneController,
    private val hooks: PlaybackHooks = PlaybackHooks(),
    private val rowOf: (Long) -> PanelRow? = { null },
    private val recording: () -> RecordingMenu? = { null },
) {
    /** Which sheet row BACK from a pushed screen re-focuses. */
    val sheetFocus = PlayerMenuFocus()

    /** Long-OK on a panel row: a fresh sheet, focus reset to the first row. */
    fun openChannelMenu(channelId: Long) {
        sheetFocus.clear()
        overlays.set(PlaybackOverlay.ChannelMenu(channelId))
    }

    /**
     * The channel a menu action applies to: the panel row's or the tuned one,
     * re-resolved from the live list so repeated actions see fresh flags.
     */
    fun menuChannel(): ChannelEntity? {
        val id =
            (overlays.value as? PlaybackOverlay.ChannelMenu)?.channelId
                ?: tuner.current.value?.id
                ?: return null
        return tuner.byId(id) ?: tuner.current.value
    }

    fun onMenuItem(item: PlayerMenuItem) {
        val channel = menuChannel() ?: return
        sheetFocus.onActivated(item)
        when (val route = PlayerMenuRouting.routeOf(item)) {
            PlayerMenuRoute.SEARCH -> openScreen(hooks.onOpenSearch)
            PlayerMenuRoute.SETTINGS -> openScreen(hooks.onOpenSettings)
            PlayerMenuRoute.TOGGLE_FAVORITE -> toggleFavorite(channel)
            PlayerMenuRoute.HIDE_CHANNEL -> hide(channel)
            // The row fires the chooser regardless of the tune-time setting.
            PlayerMenuRoute.EXTERNAL_PLAYER -> {
                val next = afterAction(overlays.value)
                hooks.platform.external.open(channel.source.streamUrl)
                overlays.set(next)
            }
            PlayerMenuRoute.DESCRIPTION -> push { back -> description(rowOf(channel.id), back) }
            PlayerMenuRoute.CHANNEL_OPTIONS ->
                overlays.set(PlaybackOverlay.ChannelOptions(channel.source.name, back = afterAction(overlays.value)))
            PlayerMenuRoute.MY_LIST_TOGGLE -> {
                // Saves/removes the row's airing programme, sheet dismissed.
                val next = afterAction(overlays.value)
                actions.myList?.toggleFor(channel)
                overlays.set(next)
            }
            PlayerMenuRoute.MANAGE_FAVORITES, PlayerMenuRoute.REORDER_CHANNELS ->
                openScreen { actions.myList?.run(route, channel) }
            PlayerMenuRoute.RECORD -> record { it.onRecord(channel) }
            PlayerMenuRoute.CUSTOM_RECORDING -> record { it.onCustomRecording(channel) }
            PlayerMenuRoute.COMING_SOON -> push { back -> PlaybackOverlay.ComingSoon(item.label, back) }
        }
    }

    /** The host's My-list context; the sheet UI reads label state off it. */
    val myList: MyListMenuHost? get() = actions.myList

    /** Record rows act through the DVR menu (coming-soon while unwired). */
    private fun record(action: (RecordingMenu) -> Unit) {
        val menu = recording()
        if (menu == null) {
            push { back -> PlaybackOverlay.ComingSoon(PlayerMenuItem.RECORD.label, back) }
        } else {
            action(menu)
        }
    }

    /** Maps DVR prompts onto overlays (the guide's controller does its own). */
    fun onRecordingPrompt(prompt: RecordingPrompt) {
        overlays.set(PlaybackRecordingPrompts.overlayFor(prompt, overlays.value))
    }

    /** All §41 pane rows are locked; any activation lands on coming-soon. */
    fun onChannelOption(rowId: String) = push { back -> PlaybackOverlay.ComingSoon(rowId, back) }

    /** Pushed screens remember the overlay behind them; BACK pops to it. */
    private fun push(next: (back: PlaybackOverlay) -> PlaybackOverlay) = overlays.set(next(overlays.value))

    /**
     * Search and the settings sheet open over bare playback (no stale
     * panel/menu chrome behind the scrim).
     */
    private fun openScreen(open: () -> Unit) {
        overlays.set(PlaybackOverlay.None)
        open()
    }

    private fun toggleFavorite(channel: ChannelEntity) {
        val next = afterAction(overlays.value)
        actions.toggleFavorite(channel)
        overlays.set(next)
    }

    private fun hide(channel: ChannelEntity) {
        val next = afterAction(overlays.value)
        tuner.zapAwayFrom(channel)
        actions.hide(channel)
        overlays.set(next)
    }
}

/**
 * Where a row that dismisses the sheet lands: the panel behind the channel
 * menu, bare playback otherwise. Channel options shares this — the pane
 * replaces the sheet, so its BACK target is the panel too.
 */
private fun afterAction(current: PlaybackOverlay): PlaybackOverlay =
    if (current is PlaybackOverlay.ChannelMenu) PlaybackOverlay.Panel else PlaybackOverlay.None

/** The sheet row's airing programme: title + synopsis (dump 40). */
private fun description(
    row: PanelRow?,
    back: PlaybackOverlay,
): PlaybackOverlay =
    PlaybackOverlay.Description(
        title = row?.nowTitle ?: PlayerMenu.NO_INFORMATION,
        text = row?.description ?: PlayerMenu.NO_INFORMATION,
        back = back,
    )
