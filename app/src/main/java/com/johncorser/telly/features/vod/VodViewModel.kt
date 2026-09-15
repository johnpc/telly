package com.johncorser.telly.features.vod

import com.johncorser.telly.features.vod.db.VodItemEntity
import com.johncorser.telly.features.vod.db.VodPositionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The Movies browser's state: categories column left, item cards right
 * (ux-spec §VOD: VOD section lists groups -> items). Selecting a category
 * swaps the card grid; the first category is selected by default. A plain
 * class, unit-tested on the JVM.
 */
class VodViewModel(
    deps: VodDeps,
    scope: CoroutineScope,
) {
    private val items: StateFlow<List<VodItemEntity>> =
        deps.items.observeAll().stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val positions: StateFlow<List<VodPositionEntity>> =
        deps.positions.observeAll().stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val chosenCategory = MutableStateFlow<String?>(null)

    val categories: StateFlow<List<String>> =
        items.map(VodBrowse::categories).stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** The active category: the user's choice, else the first one. */
    val selected: StateFlow<String?> =
        combine(chosenCategory, categories) { chosen, all ->
            chosen?.takeIf { it in all } ?: all.firstOrNull()
        }.stateIn(scope, SharingStarted.Eagerly, null)

    val cards: StateFlow<List<VodCard>> =
        combine(items, selected, positions) { all, category, stored ->
            VodBrowse.cards(all, category, stored)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** True once the playlist import produced no VOD items at all. */
    val empty: StateFlow<Boolean> =
        items.map { it.isEmpty() }.stateIn(scope, SharingStarted.Eagerly, true)

    fun select(category: String) {
        chosenCategory.value = category
    }
}
