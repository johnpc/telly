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

Status legend: `explicit` = sealed/enum state + single owner + tests, and
the owner type carries `@StateMachine("<row name>")` ·
`guarded` = state is still split but every known invalid transition is
rejected at its owner + tested · `implicit` = known gap, listed so nobody
re-discovers it.

The gate (`scripts/check-state-machines.mjs`) checks, both ways, that every
`@StateMachine` marker in `app/src/main` has a row of the same name here and
every `explicit` row has a marker; that the fullscreen overlay has exactly
one writer (`OverlayState` mutators only inside `PlaybackCommands.kt`); and
that no `consume*()` one-shot or `var pending` field appears in a file this
catalog does not cite.

## Player and playback

| Machine | States | Owner | Status | Tests |
| --- | --- | --- | --- | --- |
| Engine connection | Idle · Buffering · Playing · Reconnecting(retry armed, epoch-cancelled) · Ended · Error | `app/src/main/java/com/johncorser/telly/features/player/EngineReconnector.kt` (+ `app/src/main/java/com/johncorser/telly/features/player/Media3PlayerEngine.kt`) | explicit | `app/src/test/java/com/johncorser/telly/features/player/Media3PlayerEngineTest.kt` |
| Reconnect budget | attempt 0..6 with backoff 1→30 s | `app/src/main/java/com/johncorser/telly/features/player/ReconnectPolicy.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/player/ReconnectPolicyTest.kt` |
| Stall detector | Healthy · FrozenRun(n) · BufferingRun(n) | `app/src/main/java/com/johncorser/telly/features/player/StallDetector.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/player/StallDetectorTest.kt` |
| Tuned stream | null · Live(channel) · Archive(channel, url) · External(channel) | `app/src/main/java/com/johncorser/telly/features/playback/TunedStream.kt` (published only by `TuneController.tune`) | explicit | `app/src/test/java/com/johncorser/telly/features/playback/TuneControllerTest.kt`, `app/src/test/java/com/johncorser/telly/features/playback/TuneControllerBlockTest.kt` |
| Catch-up mode | Off · Playing(request, fromLive) — DERIVED from Tuned stream = Archive(armed url) | `app/src/main/java/com/johncorser/telly/features/catchup/CatchupPlayback.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/catchup/CatchupPlaybackTest.kt` |
| Catch-up hand-off | Empty · Pending(request) — the guide sets it, the next playback screen consumes it once | `app/src/main/java/com/johncorser/telly/features/catchup/CatchupSession.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/catchup/GuideCatchupTest.kt`, `app/src/test/java/com/johncorser/telly/features/catchup/CatchupPlaybackTest.kt` |
| Blocked-channel gate | Ungated · Prompting(channel) · PassOnce(id) · SessionUnlocked; an intercepted archive tune is stashed for the verified PIN's replay | `app/src/main/java/com/johncorser/telly/features/playback/BlockGate.kt`, `app/src/main/java/com/johncorser/telly/features/playback/PendingCatchup.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/playback/BlockGateTest.kt` |
| Block session | Locked · Unlocked ("Until app restart"); relocked when parental turns off or the PIN changes | `app/src/main/java/com/johncorser/telly/features/playback/BlockGate.kt` (`BlockSession`, relock wired from `app/src/main/java/com/johncorser/telly/core/settings/ParentalControls.kt`) | explicit | `app/src/test/java/com/johncorser/telly/core/settings/ParentalControlsTest.kt` |
| Fullscreen overlay | `PlaybackOverlay` sealed (None · Info · InfoTransport · ZapInfo · QuickBar · Panel · ChannelMenu · pushed screens …); EVERY write is a `PlaybackCommand` through the one executor (funnel gate-checked) | `app/src/main/java/com/johncorser/telly/features/playback/PlaybackOverlay.kt`; key → command table `app/src/main/java/com/johncorser/telly/features/playback/PlaybackKeyPolicy.kt`; executor `app/src/main/java/com/johncorser/telly/features/playback/PlaybackCommands.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/playback/PlaybackKeyPolicyTest.kt`, `app/src/test/java/com/johncorser/telly/features/playback/PlaybackViewModelTest.kt` |
| Picture-in-picture | Windowed · InPip — entering from ANY path clears the overlay | `app/src/main/java/com/johncorser/telly/features/pip/PipState.kt` (cleared in `PlaybackViewModel.init`) | explicit | `app/src/test/java/com/johncorser/telly/features/playback/PlaybackViewModelHooksTest.kt` |
| Background suspend | Running · Suspended(one-shot, consumed by the recover step) | `app/src/main/java/com/johncorser/telly/features/playback/PlaybackLifecycle.kt`, flag in `app/src/main/java/com/johncorser/telly/features/playback/TuneController.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/guide/GuideControllerTest.kt` |
| Multiview | `MultiviewLayer` Panes · Menu · Picker(mode) + PIN card over any (a gated pane action is stashed for the verified PIN) | `app/src/main/java/com/johncorser/telly/features/multiview/MultiviewLayer.kt`, `app/src/main/java/com/johncorser/telly/features/multiview/MultiviewViewModel.kt`, `app/src/main/java/com/johncorser/telly/features/multiview/MultiviewTuneGate.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/multiview/MultiviewViewModelTest.kt` |
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
| Voice search mailbox | Idle · Transcript(text) — the recognizer result is handed to the query exactly once | `app/src/main/java/com/johncorser/telly/features/search/VoiceSearch.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/search/VoiceSearchTest.kt` |
| Navigator | typed back stack (push / pop / replaceAll) | `app/src/main/java/com/johncorser/telly/core/navigation/Navigator.kt` | explicit | `app/src/test/java/com/johncorser/telly/core/navigation/NavigatorTest.kt` |
| Onboarding wizard | `WizardStep` TYPE_CHOOSER · URL_ENTRY · PROCESSING · PROCESSED · EPG_URL · SAVING · DONE; every forward edge checks the step it leaves | `app/src/main/java/com/johncorser/telly/features/onboarding/WizardUiState.kt`, transitions in `app/src/main/java/com/johncorser/telly/features/onboarding/AddPlaylistViewModel.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/onboarding/AddPlaylistViewModelTest.kt` |
| Welcome restore offer | `RestoreOffer` None · Ready · NeedsAccess | `app/src/main/java/com/johncorser/telly/features/onboarding/WelcomeRestore.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/onboarding/WelcomeRestoreTest.kt` |
| Start route | `StartDecision` Welcome · Playback · GuideUntuned (carries `tunesPreview`); the first guide after a cold start claims it | `app/src/main/java/com/johncorser/telly/features/onboarding/StartRoute.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/onboarding/StartRouteTest.kt` |

## Settings, data, background work

| Machine | States | Owner | Status | Tests |
| --- | --- | --- | --- | --- |
| Settings sheet | `SettingsPane` stack + `SettingsOverlay` (Picker · TextEdit(playlistUrl captured at open) · confirms · PinSetup(armsParental) · PinVerify(target)) | `app/src/main/java/com/johncorser/telly/features/settings/SettingsUiState.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/settings/SettingsViewModelTest.kt` |
| Parental PIN gates | Off · On(pin) — the master toggle turns on ONLY when its PIN commits; `PinTarget` SETTINGS · PLAYLISTS · BLOCKED_CHANNELS names what a verified PIN opens | `app/src/main/java/com/johncorser/telly/features/settings/SettingsUiState.kt` (`PinTarget`), commits in `app/src/main/java/com/johncorser/telly/features/settings/SettingsViewModelOverlays.kt`, `app/src/main/java/com/johncorser/telly/core/settings/ParentalControls.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/settings/SettingsViewModelTest.kt`, `app/src/test/java/com/johncorser/telly/core/settings/ParentalControlsTest.kt` |
| Manual refresh | `RefreshStatus` idle · busy(row) · message | `app/src/main/java/com/johncorser/telly/features/settings/SettingsViewModelRefresh.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/settings/SettingsViewModelRefreshTest.kt` |
| EPG freshness | `EpgFreshness` NeverFetched · Fresh(at) · Failed(failedAt, lastSuccess) — read off two PERSISTED stamps (v13 `epgFailedAtMs`), backoff 5 min survives restarts | `app/src/main/java/com/johncorser/telly/features/epg/EpgFreshness.kt`, applied by `app/src/main/java/com/johncorser/telly/features/epg/EpgRefresher.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/epg/EpgFreshnessTest.kt`, `app/src/test/java/com/johncorser/telly/features/epg/EpgRefresherTest.kt` |
| Recording row | `RecordingStatus` SCHEDULED → RECORDING → DONE \| FAILED, SCHEDULED → FAILED (missed); every edge is a SQL update guarded by the current status and reports whether it happened | `app/src/main/java/com/johncorser/telly/features/recording/RecordingStatus.kt`, transitions in `app/src/main/java/com/johncorser/telly/features/recording/RecordingStoreTransitions.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/recording/RecordingStoreTest.kt`, `app/src/test/java/com/johncorser/telly/features/recording/RecordingEngineTest.kt` |
| Reminder | `ReminderDue.State` PENDING · DUE · EXPIRED, then FIRED as a PERSISTED claim (v13 `firedAtMs`, guarded update) before the popup — a crash can never fire twice | `app/src/main/java/com/johncorser/telly/features/reminders/ReminderDue.kt`, `app/src/main/java/com/johncorser/telly/features/reminders/ReminderEngine.kt` | explicit | `app/src/test/java/com/johncorser/telly/features/reminders/ReminderEngineTest.kt` |
| Auto backup | Disabled · Idle(hash) · Debouncing · Exporting | `app/src/main/java/com/johncorser/telly/features/settings/ConfigAutoBackup.kt` | guarded | `app/src/test/java/com/johncorser/telly/features/settings/ConfigAutoBackupTest.kt` |

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

## What the 2026-11-16 pass changed

- **Fullscreen overlay has one writer.** `PlaybackCommand.Show` and
  `KeepAlive` joined the command vocabulary; the menu handler, track
  pickers, recording prompts, PIP and coming-soon all execute commands.
  The gate fails any other `OverlayState` mutator call.
- **Recording transitions are SQL-guarded** (`WHERE status = …`) and report
  whether they happened: a cancel racing a start records nothing, a finish
  on a DONE or deleted row is a no-op, a missed window fails only a
  SCHEDULED row. The UI's optimistic "stopping" marks are pruned the moment
  Room shows the row finalized.
- **EPG freshness is persisted** (`epgFailedAtMs`): the 5-minute failure
  backoff survives a restart and the `0 = never` sentinels are interpreted
  in one classifier.
- **Reminder claims are persisted** (`firedAtMs`) before the popup; a
  reminder can never fire twice across a crash.
- **Settings text edits carry their playlist** (captured when the editor
  opened), so a refresh underneath the editor cannot retarget the commit.
- **Start route is a value** (`StartDecision`) the first guide claims,
  replacing the set-here/consume-there flag.
- **Block session relocks** when parental controls turn off or the PIN
  changes.
- **Markers + gate**: `@StateMachine("…")` on every explicit owner,
  cross-checked against this file; heuristic detection of new one-shots.

## Known gaps (listed, not hidden)

- `RecordingsViewModel.stoppedIds` is still an optimistic UI copy of one
  row's status (pruned, bounded, but a copy). A `Stopping` status in the
  row itself would retire it.
- `ReminderEngine.handled` duplicates the persisted claim within one
  process; it only saves a redundant SQL update per sweep.
- `CatchupSession` and `VoiceSearch` are one-shot mailboxes between two
  screens with no expiry; a request nobody consumes waits for the next
  screen that does.
