# State machines in telly

Every "mode" the app can be in is a state machine, whether or not the code
calls it one. This catalog is the single list of them: what the states are,
which file OWNS the transitions, and how each is tested. It exists for three
readers at once — the user (every entry is a mode they can see or feel), the
developer (one place to look before touching a flag) and the agent (the
`owner:` paths are machine-checked by `scripts/check-state-machines.mjs`, so
a renamed or deleted owner fails the quality gate instead of rotting here).

## The rule

**One mode, one owner.** A mode is represented by ONE value (a sealed
interface / enum / data class), written in ONE place (a transition function
or a single class), and every reader derives from that value instead of
keeping its own copy. Concretely:

- No boolean + nullable + counter trios that only mean something together.
  Fold them into a sealed type whose variants carry their data.
- No "consume once" flags read in one class and set in another. Return the
  decision from the function that knows it.
- No sentinel encodings (`0` = never, `null` = "the other pane"). Name the
  state.
- Timers are part of the state: a scheduled retry/hide/tick that can
  outlive the mode it belongs to is a bug (see the engine reconnector).
- Every transition the user can trigger has a JVM test; races between two
  triggers get a test the day they are found.

When you find an implicit machine, add it here first (status `implicit`),
then convert it. Adding a row costs one line; it is never wrong to add one.

Status legend: `explicit` = sealed/enum state + single owner + tests ·
`guarded` = state is still split but every known invalid transition is
rejected at its owner + tested · `implicit` = known gap, listed so nobody
re-discovers it.

## Player and playback

| Machine | States | Owner | Status | Tests |
| --- | --- | --- | --- | --- |
| Engine connection | Idle · Buffering · Playing · Reconnecting(retry armed) · Ended · Error | `app/src/main/java/com/johncorser/telly/features/player/Media3PlayerEngine.kt` + `app/src/main/java/com/johncorser/telly/features/player/EngineReconnector.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/player/Media3PlayerEngineTest.kt` |
| Reconnect budget | attempt 0..6 with backoff 1→30 s | `app/src/main/java/com/johncorser/telly/features/player/ReconnectPolicy.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/player/ReconnectPolicyTest.kt` |
| Stall detector | Healthy · FrozenRun(n) · BufferingRun(n) | `app/src/main/java/com/johncorser/telly/features/player/StallDetector.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/player/StallDetectorTest.kt` |
| Tuned stream | null · Live(channel) · Archive(channel, url) · External(channel) | `app/src/main/java/com/johncorser/telly/features/playback/TunedStream.kt` (published only by `TuneController.tune`) | explicit | `app/src/test/java/com/johncorser/telly/features/playback/TuneControllerTest.kt`, `app/src/test/java/com/johncorser/telly/features/playback/TuneControllerBlockTest.kt` |
| Catch-up mode | Off · Playing(request, fromLive) — DERIVED from Tuned stream = Archive(armed url) | `app/src/main/java/com/johncorser/telly/features/catchup/CatchupPlayback.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/catchup/CatchupPlaybackTest.kt` |
| Blocked-channel gate | Ungated · Prompting(channel) · PassOnce(id) · SessionUnlocked | `app/src/main/java/com/johncorser/telly/features/playback/BlockGate.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/playback/BlockGateTest.kt` |
| Fullscreen overlay | `PlaybackOverlay` sealed (None · Info · InfoTransport · ZapInfo · QuickBar · Panel · ChannelMenu · pushed screens …) | `app/src/main/java/com/johncorser/telly/features/playback/PlaybackOverlay.kt`; key → command table `app/src/main/java/com/johncorser/telly/features/playback/PlaybackKeyPolicy.kt`; executor `app/src/main/java/com/johncorser/telly/features/playback/PlaybackCommands.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/playback/PlaybackKeyPolicyTest.kt`, `app/src/test/java/com/johncorser/telly/features/playback/PlaybackViewModelTest.kt` |
| Picture-in-picture | Windowed · InPip — entering from ANY path clears the overlay | `app/src/main/java/com/johncorser/telly/features/pip/PipState.kt` (cleared in `PlaybackViewModel.init`) | explicit | `app/src/test/java/com/johncorser/telly/features/playback/PlaybackViewModelHooksTest.kt` |
| Background suspend | Running · Suspended(one-shot) | `app/src/main/java/com/johncorser/telly/features/playback/PlaybackLifecycle.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/guide/GuideControllerTest.kt` |
| Multiview | `MultiviewLayer` Panes · Menu · Picker(mode) + PIN card over any | `app/src/main/java/com/johncorser/telly/features/multiview/MultiviewViewModel.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/multiview/MultiviewViewModelTest.kt` |
| VOD playback | `VodStage` Loading · ResumePrompt · Playing | `app/src/main/java/com/johncorser/telly/features/vod/VodStage.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/vod/VodPlaybackViewModelTest.kt` |

## Guide, panel, search, navigation

| Machine | States | Owner | Status | Tests |
| --- | --- | --- | --- | --- |
| Guide layer | `GuideLayer` Grid · Groups · RowMenu · CellMenu · pushed screens (each carries its `back`) | `app/src/main/java/com/johncorser/telly/features/guide/GuideModel.kt` (`backOf`), `app/src/main/java/com/johncorser/telly/features/guide/GuideMenuController.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/guide/GuideMenuControllerTest.kt` |
| Guide keys | (layer, key) → command | `app/src/main/java/com/johncorser/telly/features/guide/GuideKeyPolicy.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/guide/GuideKeyPolicyTest.kt` |
| Group tool completion | Open(session) · Done — only pops while the layer still shows THAT session | `app/src/main/java/com/johncorser/telly/features/guide/GuideSheetChannelActions.kt`, `app/src/main/java/com/johncorser/telly/features/playback/PlaybackMenuHandler.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/groups/CopyChannelsSessionTest.kt` |
| Exit confirm | Idle · Armed (= warning toast visible) | `app/src/main/java/com/johncorser/telly/features/guide/ExitConfirm.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/guide/ExitConfirmTest.kt` |
| Long-press detection | Idle · Held · HoldFired | `app/src/main/java/com/johncorser/telly/core/input/HoldKeyDetector.kt` | explicit | `app/src/test/java/com/johncorser/telly/core/input/HoldKeyDetectorTest.kt` |
| Guide focus memory | Empty · Saved(snapshot) · Consumed | `app/src/main/java/com/johncorser/telly/features/guide/GuideFocusMemory.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/guide/GuideFocusMemoryTest.kt` |
| Panel focus | Browsing(group, row) · PinPrompt(group); the list honors every focus command version | `app/src/main/java/com/johncorser/telly/features/panel/PanelViewModel.kt`, `app/src/main/java/com/johncorser/telly/features/panel/ChannelPanelScreenList.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/panel/PanelViewModelTest.kt` + e2e `@channel-panel` |
| Search overlay | `SearchOverlay` None · ProgramMenu · CustomRecording · ComingSoon | `app/src/main/java/com/johncorser/telly/features/search/SearchOverlay.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/search/SearchViewModelTest.kt` |
| Navigator | typed back stack (push / pop / replaceAll) | `app/src/main/java/com/johncorser/telly/core/navigation/Navigator.kt` | explicit | `app/src/test/java/com/johncorser/telly/core/navigation/NavigatorTest.kt` |
| Onboarding wizard | `WizardStep` TYPE_CHOOSER · URL_ENTRY · PROCESSING · PROCESSED · EPG_URL · SAVING · DONE; every forward edge checks the step it leaves | `app/src/main/java/com/johncorser/telly/features/onboarding/AddPlaylistViewModel.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/onboarding/AddPlaylistViewModelTest.kt` |
| Welcome restore offer | `RestoreOffer` None · Ready · NeedsAccess | `app/src/main/java/com/johncorser/telly/features/onboarding/WelcomeRestore.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/onboarding/WelcomeRestoreTest.kt` |

## Settings, data, background work

| Machine | States | Owner | Status | Tests |
| --- | --- | --- | --- | --- |
| Settings sheet | `SettingsPane` stack + `SettingsOverlay` (Picker · TextEdit · confirms · PinSetup(armsParental) · PinVerify(target)) | `app/src/main/java/com/johncorser/telly/features/settings/SettingsUiState.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/settings/SettingsViewModelTest.kt` |
| Parental PIN gates | Off · On(pin) — the master toggle turns on ONLY when its PIN commits; `PinTarget` SETTINGS · PLAYLISTS · BLOCKED_CHANNELS names what a verified PIN opens | `app/src/main/java/com/johncorser/telly/features/settings/SettingsViewModelOverlays.kt`, `app/src/main/java/com/johncorser/telly/core/settings/ParentalControls.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/settings/SettingsViewModelTest.kt`, `app/src/test/java/com/johncorser/telly/core/settings/ParentalControlsTest.kt` |
| Manual refresh | `RefreshStatus` idle · busy(row) · message | `app/src/main/java/com/johncorser/telly/features/settings/SettingsViewModelRefresh.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/settings/SettingsViewModelRefreshTest.kt` |
| EPG freshness | NeverFetched · Fresh(at) · Failed(at, backoff 5 min) · Forced | `app/src/main/java/com/johncorser/telly/features/epg/EpgRefresher.kt`, `app/src/main/java/com/johncorser/telly/features/epg/RefreshScheduler.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/epg/EpgRefresherTest.kt` |
| Recording row | `RecordingStatus` SCHEDULED · RECORDING · DONE · FAILED | `app/src/main/java/com/johncorser/telly/features/recording/RecordingStatus.kt`, `app/src/main/java/com/johncorser/telly/features/recording/RecordingEngine.kt` | implicit | `app/src/test/java/com/johncorser/telly/features/recording/RecordingEngineTest.kt` |
| Reminder | `ReminderDue.State` PENDING · DUE · EXPIRED (fired-once lives in memory) | `app/src/main/java/com/johncorser/telly/features/reminders/ReminderDue.kt`, `app/src/main/java/com/johncorser/telly/features/reminders/ReminderEngine.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/reminders/ReminderEngineTest.kt` |
| Auto backup | Disabled · Idle(hash) · Debouncing · Exporting | `app/src/main/java/com/johncorser/telly/features/settings/ConfigAutoBackup.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/settings/ConfigAutoBackupTest.kt` |
| Start route | Welcome · Playback · GuideUntuned(one-shot) | `app/src/main/java/com/johncorser/telly/features/onboarding/StartRoute.kt` | implicit | `app/src/test/java/com/johncorser/telly/features/onboarding/StartRouteTest.kt` |

## What the 2026-11-15 pass changed (and why a user notices)

- **Zap during a reconnect no longer re-buffers the new channel.** A
  retry armed for a dropped stream used to fire up to 30 s later on
  whatever was playing by then; a stopped (backgrounded) or released
  engine could be re-prepared from the grave. The pending retry is now
  retired by every `load/stop/release/READY` (epoch check in
  `EngineReconnector`).
- **Catch-up can no longer get stuck "on" over live video.** The mode is
  derived from the tuner's `TunedStream`; Hide channel's zap-away, a
  PIN-gated archive tune, or any live tune leaves it by construction.
  Handing a channel to the external player now stops the internal stream
  instead of leaving its audio running underneath.
- **Parental controls cannot be "on with no PIN".** Toggling the master
  switch opens PIN setup and turns on only when the PIN commits; BACK
  leaves it off. "Require PIN for Settings" was persisted but enforced
  nowhere — the sheet now opens behind the PIN and BACK on that prompt
  leaves settings.
- **PIP from HOME drops the overlay** like the quick-bar slot always did,
  so a panel/sheet does not keep eating keys behind the tiny window.
- **Multiview** ignores picks while the PIN card is up (the CH+/− path
  already did).
- **The wizard's Done is single-shot** (`SAVING`), so a double-tap or a
  BACK during the write can't persist twice or land DONE on an old step.
- **Exit confirm** is one field: BACK exits exactly while the toast shows.
- **The channel panel's focus command** is honored on every version, so
  reopening on the tuned row lands there even if the D-pad had moved on.
- **A dead EPG URL** retries every 5 min instead of every minute tick and
  every playlist emission.
- **Group tool completion** only pops the sheet while that tool is still
  the layer (BACK first, completion later used to pop twice).

## Known gaps (listed, not hidden)

- Recording lifecycle: the DB `status` column, the engine's in-memory
  `active` map and the UI's optimistic `stoppedIds` are three copies of one
  state; `finish` has no `WHERE status = RECORDING` guard. Next candidate.
- `StartRoute.consumeUntunedStart` is a set-here/consume-there one-shot;
  `forChannelCount` should return the start decision instead.
- `BlockSession.unlocked` ("Until app restart") is never reset when
  parental controls are disabled or the PIN changes within a session.
- The overlay state has ~17 writers outside `PlaybackCommands.execute`
  (menu handler, track pickers, recording prompts); they are consistent
  today but not funneled.
- `ReminderEngine` remembers "fired" only in memory; a crash between
  `onDue` and `remove` re-fires once after restart.
