package com.johncorser.telly.features.playback

import com.johncorser.telly.features.groups.GroupToolLauncher
import com.johncorser.telly.features.groups.GroupTools
import com.johncorser.telly.features.mylist.MyListMenuHost
import com.johncorser.telly.features.panel.PanelHooks
import com.johncorser.telly.features.panel.PanelViewModel
import kotlinx.coroutines.CoroutineScope

/**
 * The one way both sheet hosts (guide + playback panel) assemble the
 * factory behind the six group/bulk context-sheet tool rows, so the two
 * sheets share feeds and persistence and can never drift.
 */
internal fun PlaybackEnv.groupTools(scope: CoroutineScope): GroupTools =
    GroupTools(hooks.customGroups, channelDao, epgRepository.channelIds(), hooks.parental, scope)

/** The playback panel with the custom-groups feed folded into its hooks. */
internal fun playbackPanel(
    env: PlaybackEnv,
    tools: GroupTools,
    scope: CoroutineScope,
): PanelViewModel =
    PanelViewModel(
        env.channelDao,
        env.epgRepository,
        env.time.clock,
        scope,
        env.time.style,
        PanelHooks(lock = env.hooks.panelLock, customGroups = tools.groups),
    )

/** [SheetActions.over] with the host-bound group-tool launcher attached. */
internal fun sheetActionsFor(
    env: PlaybackEnv,
    scope: CoroutineScope,
    myList: MyListMenuHost?,
    tools: GroupTools,
    selectedGroup: () -> String,
): SheetActions = SheetActions.over(env, scope, myList, GroupToolLauncher(tools, selectedGroup))
