package com.johncorser.telly.features.panel

import com.johncorser.telly.features.groups.CustomGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** A focus move the row list must execute (scroll + focus the index). */
data class PanelFocusCommand(
    val version: Int,
    val index: Int,
)

/** The panel's cross-slice hooks: the parental gate + custom groups feed. */
class PanelHooks(
    val lock: PanelLock = PanelLock(),
    val customGroups: Flow<List<CustomGroup>> = flowOf(emptyList()),
)
