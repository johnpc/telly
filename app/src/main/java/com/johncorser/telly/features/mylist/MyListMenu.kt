package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.mylist.db.MyListEntity
import com.johncorser.telly.features.playback.PlayerMenuRoute
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The My-list toggle behind the guide dropdown's and the shared sheet's
 * "Add to My list" rows: saving a programme persists it, selecting the row
 * again removes it (the label flips via [keys], mirroring TiviMate).
 */
class MyListMenu(
    private val store: MyListStore,
    private val clock: () -> Long,
    private val scope: CoroutineScope,
) {
    /** Saved-programme keys ([MyListKeys.of]) driving the flipped labels. */
    val keys: StateFlow<Set<String>> =
        store.entries
            .map { list -> list.map { MyListKeys.of(it.channelKey, it.startMs) }.toSet() }
            .stateIn(scope, SharingStarted.Eagerly, emptySet())

    fun toggle(
        channel: ChannelEntity,
        programme: MyListProgramme?,
    ) {
        programme ?: return
        val channelKey = MyListKeys.channelKeyOf(channel)
        val saved = MyListKeys.of(channelKey, programme.startMs) in keys.value
        scope.launch {
            if (saved) store.remove(channelKey, programme.startMs) else store.save(entryOf(channelKey, programme))
        }
    }

    private fun entryOf(
        channelKey: String,
        programme: MyListProgramme,
    ): MyListEntity =
        MyListEntity(
            channelKey = channelKey,
            startMs = programme.startMs,
            endMs = programme.endMs,
            title = programme.title,
            description = programme.description,
            addedAtMs = clock(),
        )
}

/**
 * Host context for the shared context sheet's My-list/management rows,
 * built once per host (guide sheet + playback panel sheet) so
 * [com.johncorser.telly.features.playback.PlayerMenuRouting] stays the one
 * routing table: how the focused programme resolves and where the
 * management screens open differ per host, nothing else.
 */
class MyListMenuHost(
    val menu: MyListMenu,
    val programmeFor: (ChannelEntity) -> MyListProgramme?,
    val group: () -> String,
    val openManageFavorites: () -> Unit,
    val openReorderChannels: (String) -> Unit,
) {
    /** ADD_TO_MY_LIST on the sheet: toggle the focused programme. */
    fun toggleFor(channel: ChannelEntity) = menu.toggle(channel, programmeFor(channel))

    /** Dispatches the sheet routes this host context owns (mylist rows). */
    fun run(
        route: PlayerMenuRoute,
        channel: ChannelEntity,
    ) {
        when (route) {
            PlayerMenuRoute.MY_LIST_TOGGLE -> toggleFor(channel)
            PlayerMenuRoute.MANAGE_FAVORITES -> openManageFavorites()
            PlayerMenuRoute.REORDER_CHANNELS -> openReorder()
            else -> Unit
        }
    }

    /** REORDER_CHANNELS opens the reorder screen on the host's group. */
    fun openReorder() = openReorderChannels(group())

    /** Whether the sheet's programme for [channel] is saved (label flip). */
    fun savedFor(
        channel: ChannelEntity,
        keys: Set<String>,
    ): Boolean = MyListKeys.saved(keys, channel, programmeFor(channel)?.startMs)
}
