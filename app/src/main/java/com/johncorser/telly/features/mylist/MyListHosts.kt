package com.johncorser.telly.features.mylist

import com.johncorser.telly.features.panel.PanelViewModel
import com.johncorser.telly.features.playback.PlaybackHooks

/**
 * The playback-panel host's sheet My-list context: its programme is the
 * focused row's airing one, its group the panel's selected group, and the
 * management screens open through the playback hooks.
 */
fun panelMyListHost(
    menu: MyListMenu,
    panel: PanelViewModel,
    hooks: PlaybackHooks,
): MyListMenuHost =
    MyListMenuHost(
        menu = menu,
        programmeFor = { channel ->
            panel.rows.value.firstOrNull { it.channel.id == channel.id }?.let(MyListProgramme::of)
        },
        group = { panel.selectedGroup.value },
        openManageFavorites = hooks.onOpenManageFavorites,
        openReorderChannels = hooks.onOpenReorderChannels,
    )
