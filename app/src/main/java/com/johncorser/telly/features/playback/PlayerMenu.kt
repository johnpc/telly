package com.johncorser.telly.features.playback

/**
 * Verbatim TiviMate 5.2.0 context-menu rows (captures 38-40 + round3-ref 05:
 * this sheet belongs to long-OK on a panel/guide row, with the panel still
 * visible behind). Only favorites and hide are functional this slice;
 * everything else routes to a branded "coming soon" placeholder.
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

    /** "Add to Favorites" flips once the channel is already a favorite. */
    fun labelFor(favorite: Boolean): String =
        if (this == ADD_TO_FAVORITES && favorite) "Remove from Favorites" else label
}

/** One blue-headed section of the menu sheet; a null header renders no strip. */
data class PlayerMenuSection(
    val header: String?,
    val items: List<PlayerMenuItem>,
)

object PlayerMenu {
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

    /** The full long-OK sheet on a panel/guide row (round3-ref 05). */
    fun sections(
        programTitle: String?,
        channelName: String,
    ): List<PlayerMenuSection> =
        listOf(
            PlayerMenuSection(header = null, items = topItems),
            PlayerMenuSection(header = programTitle ?: "No information", items = programItems),
            PlayerMenuSection(header = channelName, items = channelItems),
            PlayerMenuSection(header = "All channels", items = allChannelsItems),
        )
}
