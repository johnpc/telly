package com.johncorser.telly.features.playback

import com.johncorser.telly.features.mylist.MyListKeys

/**
 * Verbatim TiviMate 5.2.0 context-menu rows (captures 38-40 + round3-ref 05:
 * this sheet belongs to long-OK on a panel/guide row, with the panel still
 * visible behind). Where each row leads is derived once in
 * [PlayerMenuRouting], shared by the guide's and the panel's sheets.
 */
enum class PlayerMenuItem(
    val label: String,
) {
    SEARCH("Search"),
    SETTINGS("Settings"),
    OPEN_IN_EXTERNAL_PLAYER("Open in external player"),
    RECORD("Record"),
    CUSTOM_RECORDING("Custom recording"),
    ADD_TO_MY_LIST("Add to My list"),
    PROGRAM_DESCRIPTION("Program description"),
    ADD_TO_FAVORITES("Add to Favorites"),
    BLOCK_CHANNEL("Block channel"),
    HIDE_CHANNEL("Hide channel"),
    ASSIGN_EPG("Assign EPG"),
    CHANNEL_OPTIONS("Channel options"),
    MANAGE_FAVORITES("Manage Favorites"),
    MANAGE_BLOCKING("Manage blocking"),
    MANAGE_VISIBILITY("Manage visibility"),
    REORDER_CHANNELS("Reorder channels"),
    COPY_CHANNELS("Copy channels"),
    CREATE_GROUP("Create group"),
    GROUP_OPTIONS("Group options"),
    ;

    /**
     * "Add to Favorites" flips once the channel is already a favorite;
     * "Add to My list" flips once the focused programme is saved;
     * "Block channel" flips on an already-blocked channel.
     */
    fun labelFor(
        favorite: Boolean,
        inMyList: Boolean = false,
        blocked: Boolean = false,
    ): String =
        when {
            this == ADD_TO_FAVORITES && favorite -> "Remove from Favorites"
            this == ADD_TO_MY_LIST && inMyList -> MyListKeys.REMOVE_LABEL
            this == BLOCK_CHANNEL && blocked -> "Unblock channel"
            else -> label
        }
}

/** One blue-headed section of the menu sheet; a null header renders no strip. */
data class PlayerMenuSection(
    val header: String?,
    val items: List<PlayerMenuItem>,
)

object PlayerMenu {
    /** EPG-gap fallback for the programme header and description screen. */
    const val NO_INFORMATION = "No information"

    private val topItems = listOf(PlayerMenuItem.SEARCH, PlayerMenuItem.SETTINGS)
    private val programItems =
        listOf(
            PlayerMenuItem.OPEN_IN_EXTERNAL_PLAYER,
            PlayerMenuItem.RECORD,
            PlayerMenuItem.CUSTOM_RECORDING,
            PlayerMenuItem.ADD_TO_MY_LIST,
            PlayerMenuItem.PROGRAM_DESCRIPTION,
        )
    private val channelItems =
        listOf(
            PlayerMenuItem.ADD_TO_FAVORITES,
            PlayerMenuItem.BLOCK_CHANNEL,
            PlayerMenuItem.HIDE_CHANNEL,
            PlayerMenuItem.ASSIGN_EPG,
            PlayerMenuItem.CHANNEL_OPTIONS,
        )
    private val allChannelsItems =
        listOf(
            PlayerMenuItem.MANAGE_FAVORITES,
            PlayerMenuItem.MANAGE_BLOCKING,
            PlayerMenuItem.MANAGE_VISIBILITY,
            PlayerMenuItem.REORDER_CHANNELS,
            PlayerMenuItem.COPY_CHANNELS,
            PlayerMenuItem.CREATE_GROUP,
            PlayerMenuItem.GROUP_OPTIONS,
        )

    /** The sheet's LazyColumn position of [item], headers counted as rows. */
    fun flatIndexOf(
        sections: List<PlayerMenuSection>,
        item: PlayerMenuItem,
    ): Int {
        var index = 0
        sections.forEach { section ->
            if (section.header != null) index += 1
            val at = section.items.indexOf(item)
            if (at >= 0) return index + at
            index += section.items.size
        }
        return 0
    }

    /** The full long-OK sheet on a panel/guide row (round3-ref 05). */
    fun sections(
        programTitle: String?,
        channelName: String,
    ): List<PlayerMenuSection> =
        listOf(
            PlayerMenuSection(header = null, items = topItems),
            PlayerMenuSection(header = programTitle ?: NO_INFORMATION, items = programItems),
            PlayerMenuSection(header = channelName, items = channelItems),
            PlayerMenuSection(header = "All channels", items = allChannelsItems),
        )
}
