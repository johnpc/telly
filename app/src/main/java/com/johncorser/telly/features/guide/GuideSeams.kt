package com.johncorser.telly.features.guide

import com.johncorser.telly.features.mylist.InMemoryMyListStore
import com.johncorser.telly.features.mylist.MyListStore
import com.johncorser.telly.features.reminders.GuideReminders

/** Optional cross-feature seams behind the guide's menus + settings. */
class GuideSeams(
    val reminders: GuideReminders? = null,
    val myList: MyListStore = InMemoryMyListStore(),
    /** Appearance -> TV guide -> Number of visible channels (7 = today). */
    val visibleRows: () -> Int = { GuideGeometry.VISIBLE_ROWS },
)
