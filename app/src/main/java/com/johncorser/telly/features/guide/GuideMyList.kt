package com.johncorser.telly.features.guide

import com.johncorser.telly.features.mylist.MyListMenu
import com.johncorser.telly.features.mylist.MyListMenuHost
import com.johncorser.telly.features.mylist.MyListProgramme
import kotlinx.coroutines.flow.StateFlow

/**
 * The guide host's sheet My-list context: its programme is the focused
 * cell's (null on "No information" filler), its group the guide's selected
 * one, and the management screens open through the guide callbacks.
 */
internal fun guideMyListHost(
    menu: MyListMenu,
    focus: StateFlow<GuideFocus?>,
    group: () -> String,
    callbacks: GuideCallbacks,
): MyListMenuHost =
    MyListMenuHost(
        menu = menu,
        programmeFor = { _ -> focus.value?.cell?.program?.let(MyListProgramme::of) },
        group = group,
        openManageFavorites = callbacks.onOpenManageFavorites,
        openReorderChannels = callbacks.onOpenReorderChannels,
    )
