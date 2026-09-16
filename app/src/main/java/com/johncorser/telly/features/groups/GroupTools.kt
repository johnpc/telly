package com.johncorser.telly.features.groups

import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.features.playback.PlayerMenuRoute
import com.johncorser.telly.features.playlist.db.ChannelDao
import com.johncorser.telly.features.playlist.db.ChannelEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The shared factory behind the six group/bulk context-sheet rows: it owns
 * the live feeds the tool screens read (all channels, custom groups, EPG
 * ids) and builds one [GroupToolSession] per row activation. Both sheet
 * hosts (guide and playback panel) go through the same instance's
 * [sessionFor], so the tools cannot drift between the two sheets.
 */
class GroupTools(
    private val store: CustomGroupStore,
    private val channelDao: ChannelDao,
    epgIds: Flow<List<String>>,
    private val parental: ParentalControls?,
    private val scope: CoroutineScope,
) {
    /** Custom groups in column order — the group columns append these. */
    val groups: StateFlow<List<CustomGroup>> =
        store.observe().sessionState(scope, emptyList())

    private val channels: StateFlow<List<ChannelEntity>> =
        channelDao.observeAll().sessionState(scope, emptyList())

    private val epgOptions: StateFlow<List<String>> =
        epgIds.sessionState(scope, emptyList())

    /** The tool screen behind [route], or null for non-tool routes. */
    fun sessionFor(
        route: PlayerMenuRoute,
        channel: ChannelEntity,
        group: String,
        onDone: () -> Unit,
    ): GroupToolSession? =
        when (route) {
            PlayerMenuRoute.CREATE_GROUP -> CreateGroupSession(store, onDone, scope)
            PlayerMenuRoute.GROUP_OPTIONS ->
                GroupOptionsSession(group, groups.value.firstOrNull { it.name == group }, store, onDone, scope)
            PlayerMenuRoute.COPY_CHANNELS -> CopyChannelsSession(groups.value, channels, store, onDone, scope)
            PlayerMenuRoute.ASSIGN_EPG -> AssignEpgSession(channel, epgOptions, channelDao::update, onDone, scope)
            PlayerMenuRoute.MANAGE_BLOCKING ->
                BulkFlagSession(BulkFlagKind.BLOCKING, channels, channelDao::update, parental, scope)
            PlayerMenuRoute.MANAGE_VISIBILITY ->
                BulkFlagSession(BulkFlagKind.VISIBILITY, channels, channelDao::update, parental, scope)
            else -> null
        }
}

/**
 * A host-bound view of [GroupTools]: the sheet hosts know their selected
 * group; bundling it keeps the menu controllers' constructors small.
 */
class GroupToolLauncher(
    private val tools: GroupTools,
    private val selectedGroup: () -> String,
) {
    fun session(
        route: PlayerMenuRoute,
        channel: ChannelEntity,
        onDone: () -> Unit,
    ): GroupToolSession? = tools.sessionFor(route, channel, selectedGroup(), onDone)
}
