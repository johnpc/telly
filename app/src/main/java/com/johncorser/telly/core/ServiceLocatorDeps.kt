package com.johncorser.telly.core

import android.content.Context
import com.johncorser.telly.core.settings.ParentalControls
import com.johncorser.telly.core.settings.SettingsRepository
import com.johncorser.telly.core.settings.TellySettings
import com.johncorser.telly.features.catchup.CatchupDeps
import com.johncorser.telly.features.catchup.CatchupSession
import com.johncorser.telly.features.catchup.CatchupToggles
import com.johncorser.telly.features.groups.RoomCustomGroupStore
import com.johncorser.telly.features.guide.GuideDeps
import com.johncorser.telly.features.guide.GuideKeymap
import com.johncorser.telly.features.guide.GuideStartPolicies
import com.johncorser.telly.features.history.WatchHistory
import com.johncorser.telly.features.playback.BlockSession
import com.johncorser.telly.features.playback.ClockStyle
import com.johncorser.telly.features.playback.PanelTimeouts
import com.johncorser.telly.features.playback.PlaybackDeps
import com.johncorser.telly.features.playback.PlaybackHooks
import com.johncorser.telly.features.playback.PlaybackSources
import com.johncorser.telly.features.playback.PlaybackTime
import com.johncorser.telly.features.playback.PlayerKeymap
import com.johncorser.telly.features.playback.UdpProxy
import com.johncorser.telly.features.player.skip.SkipSteps
import com.johncorser.telly.features.recording.recordingCenter
import com.johncorser.telly.features.reminders.remindersHub

/** One block-unlock session per process ("Until app restart" relock). */
internal val sharedBlockSession = BlockSession()

/** ONE catch-up hand-off slot: the guide fills it, playback consumes it. */
private val catchupSession = CatchupSession()

/** The persisted UDP-proxy rewrite ("Use proxy for UDP streams"), read live per tune. */
internal fun proxyResolve(settings: SettingsRepository): (String) -> String =
    { url -> UdpProxy.resolve(settings.get(TellySettings.UDP_PROXY), url) }

/** The persisted 12/24-hour clock choice, read live per render. */
internal fun clockStyleOf(settings: SettingsRepository): ClockStyle =
    ClockStyle(h24 = { ClockStyle.is24Raw(settings.get(TellySettings.CLOCK_FORMAT)) })

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
        engineFactory = { tunedEngine(context) },
        time =
            PlaybackTime(
                clock = clock,
                style = clockStyleOf(settingsRepository(context)),
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
                resolveUrl = proxyResolve(settingsRepository(context)),
                customGroups = RoomCustomGroupStore(database(context).customGroupDao),
            ),
    )

/** Guide slice = the playback bundle + the settings the grid honors. */
fun ServiceLocator.guideDeps(
    context: Context,
    hooks: PlaybackHooks = PlaybackHooks(),
    resumePreview: () -> Boolean = { true },
): GuideDeps =
    GuideDeps(
        playback = playbackDeps(context, hooks),
        pastDays = { settingsRepository(context).get(TellySettings.EPG_PAST_DAYS_TO_KEEP) },
        reminders = remindersHub(context).guide,
        visibleRows = { settingsRepository(context).get(TellySettings.GUIDE_VISIBLE_CHANNELS) },
        keymap = { GuideKeymap.from(settingsRepository(context)) },
        start =
            GuideStartPolicies(
                confirmExit = { settingsRepository(context).get(TellySettings.CONFIRM_EXIT) },
                resumePreview = resumePreview,
            ),
    )
