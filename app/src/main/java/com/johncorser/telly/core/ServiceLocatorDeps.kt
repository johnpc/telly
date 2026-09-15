package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.catchup.CatchupDeps
import com.johncorser.telly.features.catchup.CatchupSession
import com.johncorser.telly.features.catchup.CatchupToggles
import com.johncorser.telly.features.guide.GuideDeps
import com.johncorser.telly.features.guide.GuideKeymap
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.multiview.MultiviewDeps
import com.johncorser.telly.features.playback.BlockSession
import com.johncorser.telly.features.playback.PanelTimeouts
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playback.PlaybackHooks
import com.johncorser.telly.features.playback.PlaybackSources
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.playback.PlayerKeymap
import com.johncorser.telly.features.player.Media3PlayerEngine
import com.johncorser.telly.features.player.skip.SkipSteps
import com.johncorser.telly.features.recording.recordingCenter
import com.johncorser.telly.features.reminders.remindersHub
import com.johncorser.telly.features.vod.VodDeps

/** One block-unlock session per process ("Until app restart" relock). */
private val sharedBlockSession = BlockSession()

/** ONE catch-up hand-off slot: the guide fills it, playback consumes it. */
private val catchupSession = CatchupSession()

/** Playback slice bundle over [ServiceLocator]'s app-scoped singletons. */
fun ServiceLocator.playbackDeps(
    context: Context,
    hooks: PlaybackHooks = PlaybackHooks(),
): PlaybackDeps =
    PlaybackDeps(
        sources =
            PlaybackSources(
                channelDao = visibleChannelDao(context),
                epgRepository = epgRepository(context),
                history = WatchHistory(database(context).watchHistoryDao(), clock),
                myList = myListStore(context),
            ),
        keyValueStore = keyValueStore(context),
        engineFactory = {
            Media3PlayerEngine.create(context.applicationContext, userAgentFor = streamUserAgentFor(context))
        },
        time =
            PlaybackTime(
                clock = clock,
                panelTimeouts = {
                    PanelTimeouts.forSeconds(settingsRepository(context).get(TellySettings.PLAYER_PANEL_TIMEOUT_SEC))
                },
            ),
        parental = ParentalControls(settingsRepository(context)),
        hooks =
            hooks.copy(
                playerKeymap = { PlayerKeymap.from(settingsRepository(context)) },
                recording = recordingCenter(context),
                parental = ParentalControls(settingsRepository(context)),
                blockSession = sharedBlockSession,
                catchup =
                    CatchupDeps(
                        session = catchupSession,
                        toggles = CatchupToggles { setting -> settingsRepository(context).get(setting) },
                        skipSteps = { SkipSteps.of(settingsRepository(context)) },
                    ),
            ),
    )

/** Guide slice = the playback bundle + the settings the grid honors. */
fun ServiceLocator.guideDeps(
    context: Context,
    hooks: PlaybackHooks = PlaybackHooks(),
): GuideDeps =
    GuideDeps(
        playback = playbackDeps(context, hooks),
        pastDays = { settingsRepository(context).get(TellySettings.EPG_PAST_DAYS_TO_KEEP) },
        reminders = remindersHub(context).guide,
        visibleRows = { settingsRepository(context).get(TellySettings.GUIDE_VISIBLE_CHANNELS) },
        keymap = { GuideKeymap.from(settingsRepository(context)) },
    )

/**
 * Multiview slice: one independent engine per pane from the factory. The
 * pane engines do NOT handle audio focus (N players grabbing focus pause
 * one another); the focused pane owns audio via mute state instead.
 */
fun ServiceLocator.multiviewDeps(context: Context): MultiviewDeps =
    MultiviewDeps(
        channelDao = visibleChannelDao(context),
        epgRepository = epgRepository(context),
        engines = {
            Media3PlayerEngine.create(
                context.applicationContext,
                handleAudioFocus = false,
                userAgentFor = streamUserAgentFor(context),
            )
        },
        store = keyValueStore(context),
        clock = clock,
    )

/** VOD slice: the Movies browser + seekable playback with resume. */
fun ServiceLocator.vodDeps(context: Context): VodDeps =
    VodDeps(
        items = database(context).vodItemDao(),
        positions = database(context).vodPositionDao(),
        engineFactory = {
            Media3PlayerEngine.create(context.applicationContext, userAgentFor = streamUserAgentFor(context))
        },
        rememberPosition = { settingsRepository(context).get(TellySettings.VOD_REMEMBER_POSITION) },
        clock = clock,
    )
