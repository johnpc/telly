package com.johncorser.telly.features.panel

/**
 * Appearance -> Groups: which synthetic groups the guide and panel group
 * columns list (defaults on = today's Favorites + All channels + playlist
 * groups order). Only the LISTS filter; a hidden group that is already
 * selected keeps its rows until another group is picked.
 */
data class GroupVisibility(
    val allChannels: Boolean = true,
    val favorites: Boolean = true,
) {
    fun filter(groups: List<String>): List<String> =
        groups.filterNot { group ->
            (group == PanelViewModel.ALL_CHANNELS && !allChannels) ||
                (group == PanelViewModel.FAVORITES && !favorites)
        }

    companion object {
        val DEFAULT = GroupVisibility()
    }
}
