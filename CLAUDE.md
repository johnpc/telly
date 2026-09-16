# telly — project charter

telly is an open-source IPTV player for Android TV: a pixel-faithful, TiviMate-style
live-TV experience. Native Kotlin + Jetpack Compose for TV. No backend of any kind.

## How we work

- **The director owns WHAT, Claude owns HOW.** Product decisions, slice priorities,
  and acceptance criteria come from the director; implementation, architecture-within-
  the-charter, and refactoring are Claude's call.
- **Product bar:** pixel-faithful TiviMate clone, verified side-by-side against the
  official TiviMate app running on an Android TV emulator.
- **Specs first, vertical slices always.** Every slice starts as a Gherkin `.feature`
  file under `e2e/features/`, then ships end-to-end: UI + logic + tests + gates green.
- Package layout is vertical feature slices: `com.johncorser.telly.features.<slice>`
  (playlist, epg, player, settings, ...) plus `core/` for design tokens and shared UI.
- Keep composables thin. Logic lives in plain-JVM-testable code inside the slice.

## Quality gates (all must pass; "fix the code, never the gate")

Run everything with `./scripts/quality.sh`. CI and the pre-commit hook
(`core.hooksPath = .hooks`, installed via `./scripts/install-hooks.sh`) run the same script.

| Gate | Threshold | Tool |
| --- | --- | --- |
| Lint/format | zero findings | ktlint (ktlint_official) via `./gradlew ktlintCheck` |
| Static analysis | zero findings; unused-code rules are errors | detekt (`config/detekt/detekt.yml`) |
| File length | every logic `.kt` under `app/src/main` ≤ 100 logic lines — comments/blank lines excluded (tests/generated exempt) | `scripts/check-file-lines.mjs` |
| CRAP | per-method CRAP = c²·(1−cov)³ + c ≤ 15 (generated/synthetic skipped) | `scripts/crap-check.mjs` over JaCoCo XML |
| Halstead | per-function difficulty D = (n1/2)·(N2/n2) ≤ 20 (tests exempt) | `scripts/check-halstead.mjs` |
| Duplication | threshold 0 (minLines 8, minTokens 70) | `npx -y jscpd app/src` (`.jscpd.json`) |
| Coverage | ≥ 80% line coverage of main sources (only `*Activity`, `*Screen` composables, generated excluded) | `./gradlew jacocoCoverageVerification` |
| Build + tests | green | `./gradlew assembleDebug testDebugUnitTest` |
| e2e mapping | every `.feature` file appears in the ci.yml acceptance matrix | `scripts/check-feature-coverage.mjs` |

Never raise a threshold, add an exclusion, or skip a gate to get green. Restructure the code.

## Conventions

- **Conventional commits** (`feat:`, `fix:`, `chore:`, `refactor:`, `test:`, `docs:`, `ci:`).
- **PR titles:** `type(scope): what`, e.g. `feat(playlist): parse channel groups`.
- **PR demo artifact:** every UI-affecting PR includes a screenshot or screen recording
  from the Android TV emulator, uploaded to files.jpc.io and linked as a `/d/` URL.

## Definition of done (per slice)

1. Gherkin `.feature` written and mapped in the CI acceptance matrix.
2. Implementation in a vertical slice; composables thin, logic unit-tested.
3. `./scripts/quality.sh` exits 0 locally.
4. CI green; demo artifact attached to the PR.
5. Side-by-side visual check against TiviMate on the emulator for UI slices.

## Commands

```sh
./scripts/quality.sh              # full gate (what CI and pre-commit run)
./gradlew assembleDebug           # debug APK -> app/build/outputs/apk/debug/
./gradlew testDebugUnitTest       # JVM unit tests
./gradlew jacocoTestReport        # coverage report (XML + HTML)
./gradlew ktlintFormat            # auto-fix formatting
./scripts/install-hooks.sh        # one-time: enable the pre-commit gate
./scripts/release-local.sh        # gate + build + publish a GitHub Release without Actions
```

### Gherkin e2e acceptance (cucumber-android)

The `e2e/features/**/*.feature` specs EXECUTE against the real app via
cucumber-android (step definitions under `app/src/androidTest`, features +
stream/logo fixtures staged from `e2e/` by the `syncE2eAssets` gradle task).
A MockWebServer inside the instrumentation process serves the fixtures on
`127.0.0.1:8090` (playlist/EPG regenerated per scenario so "now" programmes
exist); steps rewrite the dev host `10.0.2.2` in feature text to it.

```sh
# all areas, against a running Android TV emulator/device:
./gradlew connectedDebugAndroidTest
# one feature area (tags match the CI matrix / @<area> tag per .feature):
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.tags=@watch-and-zap
```

CI runs the same suite per feature area on an API 36 android-tv x86_64
emulator (the only 64-bit x86 TV image; API 34 TV is x86-32/arm64 only).

Local SDK note: `local.properties` must contain
`sdk.dir=/opt/homebrew/share/android-commandlinetools` on the primary dev machine
(the `ANDROID_HOME` env var there is stale — do not trust it).

## Decisions log

- **2026-09-16** Two Shield-verified parity fixes. **(1) Multiview panes render
  on a TextureView.** Media3's `PlayerView` defaults to a `SurfaceView`, which
  punches a hardware video-overlay hole; real TV hardware (Shield/mdarcy) has
  only a few overlay planes with undefined z-order, so 2-4 stacked panes
  composited to black (the emulator's software compositor hid this). surface_type
  is XML-only, so `PlayerScreenSurface` gained a `textureView` flag that inflates
  `res/layout/player_texture_surface.xml` (texture_view) instead of constructing
  a default PlayerView; `MultiviewScreenPane` passes `textureView = true`. The
  single fullscreen player stays on SurfaceView (one plane; better power/HDR).
  Side effect: TextureView panes are screencap-able (the SurfaceView window read
  all-black on the Shield) — confirmed a 2-pane grid renders live video on
  mdarcy. **(2) Info-overlay DOWN opens the channel panel.** DOWN was a dead key
  (`withinInfoOverlay` `else -> null`, and `PlaybackScreenKeys` never intercepted
  `DirectionDown`, so Compose focus no-oped it under the shortcut cards).
  `withinInfoOverlay` now maps `DOWN -> OpenPanel` and `onPreviewKey`/`previewKeyOf`
  intercept DirectionDown — the down-chevron now expands to the full channel list
  (ux-spec §2.3). Deviation from TiviMate Pro (device-captured): the reference's
  DOWN reveals the current channel's now/next programme browser; telly's panel is
  a superset (full list + the focused channel's schedule in its detail card).
  e2e: new watch-and-zap scenario "Down from the info overlay opens the channel
  list panel" (29/29 on the tv34 emulator). Verified on the Shield via screencap
  (both fixes) + quality.sh.
- **2026-09-15** v0.2.0 (versionCode 2): the feature-complete milestone after
  29 v0.1.0 releases; README rewritten to the shipped feature surface.
- **2026-09-15** De-premium milestone: the decorative premium tier is deleted —
  no Unlock Premium screen, no padlocked/dimmed rows anywhere; everything telly
  builds ships free (Search and Multiview became directly reachable).
- **2026-09-15** Picture-in-picture: quick-bar PIP slot enters system PIP, and
  General → "PIP on Home" (default off) also enters it from `onUserLeaveHint`;
  `MainActivityHooks` owns the platform glue, logic stays JVM-tested.
- **2026-09-15** Reminders: the guide dropdown's Remind row schedules into a Room
  `reminders` table (v5); `ReminderEngine` (injected-clock ticker) fires an in-app
  popup at air time and Settings → Other → Reminders lists/cancels them.
- **2026-09-15** My List + favorites management: rail bookmark icon →
  `Route.MyList` (saved programmes, my_list table, schema v6 + favoriteOrder);
  the context sheet's Manage Favorites and Reorder channels screens are live.
- **2026-09-15** Playback extras: AFR (`AUTO_FRAME_RATE`, Display.Mode switch),
  "Use external player" (Off/Always via ACTION_VIEW hand-off; catch-up/DVR stay
  internal) and configurable `SKIP_STEPS` presets feeding every seek surface.
- **2026-09-15** Playlist extras: per-playlist detail pane gains URL edit
  (re-keys the Room row in place, VOD survives), per-playlist User-Agent
  (ResolvingDataSource injects it per request), auto-update and manage groups
  (GroupFilteredChannelDao feeds playback/guide/multiview/search).
- **2026-09-15** VOD: `VodClassifier` partitions movie-style playlist entries
  into vod_items (v7), the rail's Movies icon opens the browser, playback is a
  seekable route with resume (`VodResumePolicy` + vod_positions); Settings →
  Other → VOD clears positions.
- **2026-09-15** Appearance settings are real: TV Guide (visible-channels
  density, transparency), Player (panel transparency/timeout/clock via
  `LocalPanelStyle`), Groups and Logos sub-panes plus language and font size —
  all persisted `TellySettings` driving live UI tokens.
- **2026-09-15** Remote control keymaps: Settings → Remote control hosts player
  + TV guide sub-screens; `PlayerKeymap`/`GuideKeymap` remap D-pad and media
  keys ahead of the default key policies.
- **2026-09-15** Recording/DVR: `RecordingEngine` byte-copies raw TS/progressive
  HTTP streams and records HLS for real (see the HLS-recording bullet below)
  under a foreground `RecordingService`; the in-app `RecordingScheduler` has no
  alarm-manager wakeups (scheduled captures start only while telly runs — the
  settings pane says so), and the quick-bar/rail DVR icon opens the recordings
  library.
- **2026-09-15** HLS recording supported (supersedes the "HLS is honestly
  unsupported" half of the Recording/DVR bullet — `RecordingSupport.HLS_MESSAGE`
  and `RecordingPrompt.Unsupported` are deleted; every source records).
  `RoutingStreamRecorder` keeps the engine's single `StreamRecorder` seam and
  dispatches `.m3u8` sources (`RecordingSupport.isHls`) to `HlsStreamRecorder`:
  fetch the playlist (`HlsClient`, same UA/redirect treatment as
  `OkHttpStreamRecorder`), hop a MASTER playlist onto its highest-bandwidth
  variant, then poll the MEDIA playlist per `#EXT-X-TARGETDURATION` and append
  every new segment in order — TS segments concatenate into a valid TS stream;
  fMP4 (`#EXT-X-MAP`) captures get the init segment first and concatenate into
  a valid fragmented MP4, and `HlsContainerProbe` (probes the playlist once at
  schedule time, via `RecordingFiles.newFileFor`'s `StreamContainer` seam)
  names those captures `.mp4` (probe failure falls back to `.ts`; telly's
  player sniffs the container either way). Progress is deduped by
  `#EXT-X-MEDIA-SEQUENCE`-seeded sequence numbers per capture file, so the
  engine's reconnect attempts never append a segment twice; a sequence jump
  (missed segments) logs and continues; `#EXT-X-ENDLIST` finishes the attempt
  and live playlists run to user stop / planned end. Failures keep the TS
  recorder's policy: zero-progress attempts throw (engine counts idle, FAILED
  after 3), partial-progress attempts report their bytes and reconnect.
  `HlsPlaylistParser` is pure (master-vs-media, variant pick, sequence
  numbers, ENDLIST/MAP, relative-URL resolution) and exhaustively JVM-tested.
  e2e: fixture channel 31 "HLS Live" (group "HLS", no EPG, appended so
  existing numbering is untouched; zap-wrap + VOD-count assertions updated to
  31) streams a live-style master→media playlist over three TS slices of
  news-one.ts cut at packet boundaries (gen-fixtures.mjs; concatenation is
  byte-identical to news-one.ts), and the recording feature records it,
  stops, and plays the capture back. Verified locally (quality.sh);
  on-device acceptance legs pending.
- **2026-09-15** Block channel: the context sheet's Block channel row flags the
  channel (v9); every tune path — including lastChannelId restores — passes the
  parental PIN gate first. SUPERSEDED half-sentence: multiview pane tunes were
  briefly left ungated; that was a P0 security-consistency hole and is closed —
  `MultiviewTuneGate` (over the shared `BlockGate` + `BlockSession`) now gates
  the entry pane, every picker pick and the CH+/- zap behind the same PIN card
  rendered over the multiview layers; wrong/cancelled PINs never tune a pane.
- **2026-09-15** Quick-bar track pickers: the Video/Audio/CC slots open real
  track-selection pickers off the engine's track groups and the Latency slot is
  an audio-sync stepper; all nine quick-bar slots are now live.
- **2026-09-15** Catch-up (ux-spec §2.10/§3.17; the reference sells it as
  premium — telly ships it free per the charter precedent). **Parse +
  persist:** `catchup`/`catchup-type`, `catchup-source`, `catchup-days`
  captured off `#EXTINF` into `ChannelCatchup` embedded on `channels`
  (schema v5, MIGRATION_4_5 — attributes re-import from the playlist each
  refresh like the rest of ChannelSource). **URL builders**
  (features/catchup, one tiny builder per community type): default =
  template substitution ({utc}/{start}/{lutc}/{now}/{timestamp}/{offset}/
  {duration} + `${x}` variants, epoch seconds), append = live URL +
  substituted template, shift = `?utc={start}&lutc={now}` appended,
  flussonic = last-segment `archive-{start}-{duration}` rewrite
  (mono/video/mpegts forms), xc = `/timeshift/user/pass/{durMin}/
  {yyyy-MM-dd:HH-mm}/{id}.ts`. A bare catchup-source implies "default";
  missing catchup-days defaults to 7 (the EPG past-days default; the
  reference default is not capturable). **Guide:** OK on a PAST cell of a
  catch-up channel with a real programme inside the horizon plays the
  archive directly (§3.17 "OK → plays archive"; other past cells keep the
  premium dropdown — no Play row is the assertable difference), via a
  one-shot `CatchupSession` hand-off (guide resolves URL + pushes
  Route.Playback; the playback screen consumes it instead of tuning live).
  Past-cell rendering is untouched (the spec's "dimmed cells" note is
  about past territory generally, not a per-channel differentiator).
  **Playback mode:** `CatchupPlayback` + `CatchupKeyPolicy` EXTEND
  PlaybackKeyPolicy by pre-routing (consulted first, falls through
  unchanged): RW/FF seek per SEEK_RWFF_CATCHUP over the transient
  overlays, LEFT/RIGHT per SEEK_LEFT_RIGHT and DOWN/UP per SEEK_DOWN_UP at
  bare playback only (the info overlay needs those keys for focus), and
  the *_REWINDS_LIVE toggles jump from live into the airing programme's
  archive at live edge − skip. Seek steps come from TellySettings.
  SKIP_STEPS via `CatchupSkip.of(SkipSteps.of(settings))`: back = the
  FIRST configured step, forward = the SECOND (the default "10s / 30s /
  1m / 5m" preset reproduces the original fixed 10 s back / 30 s fwd
  exactly). Catch-up tunes still pass the blocked-channel PIN gate
  (TuneController stashes the intercepted archive URL in PendingCatchup
  and the verified PIN's re-tune replays it) but NEVER hand off to the
  external player (archive URL ≠ live URL — catch-up stays on the
  internal engine, and records no history/no external either way). During
  LIVE playback the PlayerKeymap remaps apply as usual; during catch-up
  the CatchupKeyPolicy pre-route wins for the enabled seek keys and
  everything it declines falls through to the keymap unchanged.
  BACK returns where catch-up was entered
  (guide entry → guide; rewind-live → live); any live tune (zap, panel,
  recents) leaves the mode via PlaybackCommands.showZapInfo's onLiveTune
  hook. Info overlay/transport show the archived programme's title/times
  with a position/duration readout (CatchupInfo adaptation; LIVE badge
  hidden; position sampled per-second by a UI ticker while the overlay is
  visible — a logic-layer clock loop would spin runTest forever). No
  history event on a catch-up tune (not a live watch). e2e: catch-up
  fixture channel = News One (catchup="default", template onto the same
  .ts — servers ignore query params) with deep-past "Past …" EPG on News
  One + News One HD (generated BACKWARDS so now/next phases never shift),
  so a −24 h day jump lands on real playable/non-playable past cells.
  Verified locally (quality.sh); on-device acceptance legs pending.
- **2026-09-15** Custom groups + bulk channel managers: the context sheet's
  last six rows are real — NO sheet row routes to coming-soon anymore (the
  `PlayerMenuRouting` table is total; `routeOf` has no fallback). **Create
  group** = name editor (creates an EMPTY custom group — the ux-spec never
  specifies seeding it with the sheet's channel; Copy channels populates),
  **Copy channels** = target picker (skipped at exactly one group) +
  multi-select toggle list with an explicit Done, **Group options** =
  Rename/Delete of the SELECTED group (locked on playlist groups/Favorites/
  All channels — only custom groups are editable), **Assign EPG** =
  per-channel picker of every EPG id with stored data + "Auto (tvg-id)"
  default, **Manage blocking / Manage visibility** = bulk toggle editors
  over EVERY channel. Manage blocking is gated ONCE per screen entry behind
  the parental PIN (PanelLock precedent, honoring the persisted keyboard-PIN
  input method) and writes the SAME `channels.blocked` flag the Block-channel
  slice owns (v9) — the branch's duplicate blocked column was dropped at
  merge; bulk visibility deliberately does NOT zap away the tuned channel
  (the single-row Hide flow still does). Storage (Room **v12**,
  `CustomGroupsMigration.MIGRATION_11_12` — renumbered from the branch's 4→5):
  `custom_groups` + `custom_group_members` join table keyed by
  `ChannelImporter.keyOf` (identityOf over a row) so membership survives
  playlist refreshes with zero importer changes, plus `channels.epgOverride`
  living INSIDE the `ChannelOverrides` embed (next to customName/decoders/
  epgOffsetMinutes — the importer carries it automatically).
  `ChannelEntity.epgId` (= overrides.epgOverride ?: tvg-id) is the ONE lookup
  key every EPG path uses — guide/panel/playback-info/recents/history AND
  (extended at merge, since main owns them now) search + multiview — and the
  Channel-options "EPG time offset" composes ON TOP: `EpgRepository` shifts
  per channel keyed by the REMAPPED id (remap first, then offset). Custom
  groups append after the playlist groups in BOTH group columns
  (PanelRows.groupNames/channelsIn grew custom-group parameters; a name
  collision resolves to the playlist group; GroupVisibility still only
  filters the synthetic Favorites/All-channels — custom groups always show
  unless empty). Architecture: one `GroupTools` factory per host (fed by
  `CustomGroupStore` — Room + in-memory impls — via `PlaybackHooks.
  customGroups`) builds a `GroupToolSession` per row (`CreateGroup`/
  `GroupOptions`/`CopyChannels`/`AssignEpg`/`BulkFlag` sessions, pure
  JVM-tested state machines emitting `GroupToolUi` = SettingsRow lists /
  text editor / PIN entry), rendered by the single shared `GroupToolScreen`
  sheet; hosts push it as `GuideLayer.GroupTool` / `PlaybackOverlay.
  GroupTool` (BACK pops to the sheet, a completed action lands on the
  grid/panel). The sheet bundles stay main's shapes with a
  `GroupToolLauncher` folded in (`GuideSheetChannelActions` / `SheetActions`).
- **2026-09-15** Multiview (multiview-round captures + `multiview-spec.md`).
  **Captured facts, matched exactly:** the quick-bar Multiview slot opens
  `Route.Multiview` — a single centered half-size 16:9 pane on black
  (cell fractions .25/.25/.5/.5 = the captured [480,270][1440,810]) with
  the verbatim hint "Press OK to show menu" / "Your IPTV provider may
  limit the number of concurrent connections"; OK on the pane → 200 dp
  menu right of the pane, vertically centered, 40 dp rows in captured
  order (Add screen / Search and add / Change channel); all three open
  the channel picker — channel list left (REUSED `PanelViewModel` +
  `ChannelPanelScreenRow`, play arrow on the pane's channel), focused
  channel's schedule middle, airing-programme detail card top-right;
  BACK: picker → panes → fullscreen playback. **Deliberate deviation
  (charter's no-premium-tier precedent):** the reference gates every
  picker selection behind Unlock Premium; telly actually adds panes.
  Everything multi-pane is therefore DESIGNED, not cloned: 2 panes =
  side-by-side halves, 3 = one large left + two stacked right (no dead
  cell, primary stream keeps prominence — chosen over 2×2-with-empty),
  4 = 2×2; panes letterbox 16:9 via the surface's RESIZE_MODE_FIT.
  Cap = 4 panes (`MultiviewGrid.MAX_PANES`; emulator codec budgets).
  Add screen focuses the NEW pane; Search and add opens the SAME picker
  (the free reference does exactly that, capture 10, and telly's search
  is a full IME route — not embeddable cheaply); Change channel retunes
  the focused pane in place; Remove screen appears only at >1 pane and
  collapses the grid onto the removal index's neighbour. D-pad moves
  between panes (2 dp white focus border); the FOCUSED pane owns audio —
  others are muted via the new `PlayerEngine.setMuted` (ExoPlayer
  volume), because N simultaneous audio owners is nonsense on TV.
  CH+/− zap the focused pane (wrapping). BACK at the grid exits with NO
  confirmation to fullscreen playback of the focused pane's channel: it
  persists `lastChannelId` and pops — the playback screen beneath was
  uncomposed while multiview ran (its engine released, so pane codecs
  never fight the fullscreen player) and restores through its normal
  cold-start path. Multiview pane tunes do NOT record watch history
  (only the exit commit writes `lastChannelId`; the reference free build
  can't accumulate multiview history to compare against). **Engine
  seam:** `PlayerEngineFactory` (fun interface) + `PlayerEnginePool`
  (acquire per pane / release per removal / releaseAll on dispose) in
  `features/player`; pane engines come from
  `Media3PlayerEngine.create(context, handleAudioFocus = false)` — N
  players each grabbing audio focus pause one another, so the pool's
  players share the app's focus and mute state picks the audible one.
  Single playback keeps `PlaybackDeps.engineFactory` and the zap
  keep-frame behavior untouched. Per-pane engine errors render inside
  the pane (channel name + `TELLY_ERROR_TEXT` message). Picker details:
  the list reuses the panel's 39 dp single-line rows instead of the
  capture's 62.5 dp two-line rows (share-primitives mandate at jscpd 0
  beats pixel parity here; name + now-programme parity is kept), the
  schedule window is now−6 h..now+18 h (the capture shows ~4 h of past;
  the anchor is not capturable), the detail card is the focused row's
  airing programme. e2e scenarios live in the existing watch-and-zap
  feature (playback area, already in the CI matrix — no ci.yml edit);
  audio ownership is asserted via pane semantics ("audio"/"muted").
  Verified locally (quality.sh); on-device acceptance legs pending.
- **2026-09-15** History rework to the corrected ground truth
  (`docs/reference/sidebyside/history-round2/` + catalogue §3; SUPERSEDES the
  2026-09-14 History-card bullet's implementation half — the old
  `Route.Guide(historySource)` + synthetic History source group model is
  DEAD and removed: `HistoryGroup`, the guide's history keys plumbing and
  the `initialGroup`/`historySource` parameters are deleted, `Route.Guide`
  is a plain object again). New model:
  **(1) Info overlay shortcut row** = TV guide · History · one
  **recent-channel card** per recently watched channel (newest first,
  EXCLUDING the tuned channel) · **Clear** (rightmost, only while recent
  cards exist). A recent card = channel LOGO over that channel's CURRENT
  programme title in accent blue (never name/number); focusing one swaps
  the bottom chevron for its `air-time + title` line (uidumps 02/04/05,
  2 px = 1 dp; row = LazyRow so a long history scrolls). Feed =
  `RecentRowFeed` (channels × watch_history events × current × instant →
  nowNext), actions = `PlaybackRecents` on the ViewModel.
  **(2) OK on a recent card TUNES it** (zap overlay, same path as a panel
  row) — deliberate deviation per the charter precedent (cf. favorites/
  add-source): the free reference opens Unlock Premium (history-round2 §2)
  and telly has no premium tier.
  **(3) OK on the History card pushes `Route.History`** — a distinct
  full-screen "History" list (title + clear-all trash top-RIGHT per uidump
  12, "No history" empty state) and **BACK pops to the fullscreen player**.
  The reference's POPULATED list is not capturable (its standalone log was
  empty that session), so rows follow telly's 39 dp channel-row idiom:
  logo, name, the programme airing at the watch time (EPG lookup at
  `watchedAtMs`), and the watch-time clock text; OK on a row tunes it via
  the search precedent (persist `lastChannelId`, pop, playback restores) —
  logged interpretation, on-device look TBD. The screen sits on the flat
  app background, not over dimmed live video (single engine per route —
  the Search-route deviation). Clear-all acts IMMEDIATELY, no GuidedStep
  confirm: neither trash was activated in the read-only round, so no
  confirm is evidenced.
  **(4) ONE data source, documented simplification:** the reference keeps
  the recent-cards row and the standalone History log as SEPARATE sources
  (README note); telly feeds both from the existing `watch_history` table
  (recent row = newest-first minus current; History screen = the full
  capped list), so the info-row Clear card and the screen's clear-all both
  empty that one table. Recording in `TuneController.tune` is untouched
  (CAP 30 — the reference cap remains uncapturable). Gherkin: the
  watch-and-zap History scenarios rewritten to this model (recent cards,
  tune deviation, Clear, History screen list/order/relaunch/clear-all,
  BACK → player). Verified locally (quality.sh); on-device legs pending.

- **2026-09-15** Explicit EPG source configuration (final-sweep P2-1). The
  corpus PROVES both halves: the wizard HAS an EPG step (capture 13 +
  uidump 13 — title "EPG URL", guidance "Enter EPG URL for the playlist.
  You can add or change it later in the settings. XMLTV format is only
  supported.", `url-tvg` pre-filled into **Enter URL**, rows Paste from
  clipboard / Paste playlist URL (+ "similar format" hint) / greyed
  **Use default source**, actions Done/Back; Done lands on the guide,
  capture 14), and Settings → EPG → **EPG sources** (captures 57/58:
  "host (default)" row with URL sub-line + blue check, **Add source**,
  footer "EPG sources should be assigned in the playlist settings").
  Implementation: new `WizardStep.EPG_URL` between PROCESSED and DONE —
  persistence moved from `confirm()` to the EPG step's Done
  (`finishEpg()`), the committed URL overriding `url-tvg` (blank = no
  EPG, skippable per ux-spec §2 step 5; invalid non-blank re-uses the
  URL-step validation error). Custom sources = Room table `epg_sources`
  (v4, MIGRATION_3_4), keyed by the playlist's URL because the reference
  models sources per playlist (capture 58 footer + capture 20's per-
  playlist "EPG sources (1 source)" row); the auto-detected source stays
  on `playlists.epgUrl`. `EpgSourceStore` (Room + in-memory impls) feeds
  both the settings sheets and `EpgRefresher.customSources`; trim knobs
  grouped into `EpgRetention` (detekt LongParameterList — fix the code).
  **Deliberate deviation (director's directive):** the free reference
  LOCKS "Add source"; telly ships Add + per-source Edit URL/Delete
  unlocked, shaped after TiviMate's documented premium flow (ux-spec
  §3.10) — the per-source detail pane and "Delete EPG source?" confirm
  are NOT capturable and follow the playlist-detail/GuidedStep-confirm
  precedents. **Merge rule (not capturable — free tier can't add a 2nd
  source):** sources fetch auto-detected FIRST then custom in added
  order, and `EpgRepository.refresh` replaces a channel's whole schedule
  per document, so the LAST source covering a channel owns it — custom
  takes precedence per channel. Add commits attach to the enclosing
  playlist-detail pane's playlist, else the first playlist. Custom
  sources are NOT in backup JSON yet (deferred; reference backup content
  uncapturable). e2e: `epg-alt.xml` fixture (deterministic, covers the
  no-EPG Sports Arena + News One with "Alt " titles; gen-fixtures.mjs +
  FixturePlan.altSchedule mirror 1:1) drives the new epg-data scenarios
  (settings add→merge→precedence; wizard URL override) and the extended
  add-playlist happy path. Verified locally (quality.sh); on-device
  acceptance legs pending (emulators occupied this round).

- **2026-09-16** Reinstall-proof configuration (README "Backups"). **(1) Auto
  Backup:** manifest `dataExtractionRules` (31+) + `fullBackupContent` (23-30)
  include telly-settings/telly prefs + `telly.db` with its `-wal`/`-shm`
  (framework-driven full backup can't checkpoint on demand, so the WAL trio
  travels together; files domain + telly-search stay out). Proven on the
  emulator: `bmgr backupnow` → uninstall → reinstall → `bmgr restore` boots
  straight to playback, no onboarding. **(2) Automatic local export:**
  `ConfigAutoBackup` (JVM-tested: 5 s debounce via collectLatest+delay,
  NonCancellable write, payload-hash dedupe in the scalar KV store — NOT the
  settings store, which would re-trigger the loop; empty-playlists exports are
  skipped so a fresh reinstall never clobbers the old backup) re-exports the
  manual "Back up data" JSON to `Documents/telly/telly-backup.json` on
  playlist/settings changes via `MediaStoreBackupDocuments` (raw-path write
  first — the only way to overwrite another owner's orphan under all-files
  access — then MediaStore insert-or-update; API <29 has no permissionless
  path and only Auto Backup covers it). Engine restarts per MainActivity
  onCreate (fresh ServiceLocator deps; e2e resets) and stops on onDestroy.
  General pane: "Automatic backup" toggle (default ON) + last-export summary
  (`AutoBackupState` StateFlow through `SettingsFeeds`). **(3) Restore offer:**
  the welcome screen gains a focused-first "Restore previous setup" pill when
  `WelcomeRestore.offerOf` finds a readable backup (Ready) or a
  sighted-but-unreadable one (NeedsAccess). EMPIRICAL (API 34 TV emulator):
  after uninstall→reinstall the orphaned Documents file is *sighted* but NOT
  readable — scoped storage hides other-owner non-media content from both raw
  paths and MediaStore, and READ_EXTERNAL_STORAGE wouldn't help (non-media;
  no-op on 33+) — so NeedsAccess opens the system **All files access** grant
  (`MANAGE_EXTERNAL_STORAGE`; the leanback settings screen is D-pad drivable);
  the grant reliably takes effect for the app's NEXT process (the emulator's
  running process kept a stale storage view; real grants normally kill the
  app). Ready → `RestoreRunner`: importJson → re-fetch every playlist →
  land per cold-start policy. On-device proof: add fixture playlist → auto
  export → `adb uninstall` → reinstall → pill → grant → restore → channel 1
  playing with EPG. e2e: add-playlist gains the seeded-backup restore
  scenario, settings gains the auto-export round-trip scenario (BackupSteps
  seam; hooks delete the Documents file per scenario). Verified locally
  (quality.sh + add-playlist 6/6 + settings 24/24 on a TV-34 emulator).

- **2026-09-15** GitHub Actions credits are exhausted: CI/deploy workflows stay in
  the repo but must be treated as unavailable. The authoritative gate is local —
  `./scripts/quality.sh` (pre-commit enforced) plus the acceptance legs on the
  local tv34 emulator — and releases are published with
  `./scripts/release-local.sh` (gh API, no Actions minutes). Note "verified
  locally" in commit/release bodies. Last Actions-published release: v0.1.0-29.

- **2026-09-13** Native Kotlin + Compose for TV (`androidx.tv:tv-material`) chosen over
  Ionic/hybrid: TV focus handling, D-pad navigation, and playback performance demand it.
  Leanback is declared for launcher integration only; all UI is Compose.
- **2026-09-13** No backend. Playlist (M3U) and EPG (XMLTV) URLs are user-configured;
  everything is fetched device-side (OkHttp + kotlinx.serialization) and cached in Room.
- **2026-09-13** Reference app = official TiviMate installed on the Android TV emulator;
  UI slices are verified side-by-side against it.
- **2026-09-13** Media3/ExoPlayer (+ HLS) is the playback engine.
- **2026-09-13** JUnit4 (not JUnit5) for unit tests — first-class AGP support, no extra
  platform wiring; MockK + Turbine for mocks/flows.
- **2026-09-13** Navigation is a hand-rolled typed back stack (`core/navigation`:
  sealed `Route` + `Navigator` exposing a `StateFlow` stack) instead of
  androidx.navigation-compose — four routes don't justify the dependency and the
  plain class keeps back-stack semantics JVM-testable.
- **2026-09-13** Reference px→dp: the 1920×1080 uidumps are xhdpi, so 2 px = 1 dp.
  Verified against known metrics: welcome buttons 72 px = 36 dp, guided-action rows
  88 px = 44 dp, pill corner radius 8 px = 4 dp, wizard guidance title cap height
  50 px ≈ 36 sp (leanback GuidedStep default).
- **2026-09-13** Compose UI files are named `*Screen*.kt` (e.g. `WizardScreenUrlStep.kt`)
  so the existing JaCoCo `**/*Screen*` exclusion keeps thin composables out of
  coverage; all logic stays in plain classes that are fully unit-tested.
- **2026-09-13** All guide timestamps are epoch-millis `Long`s. XMLTV stamps are
  parsed with a regex + `GregorianCalendar` (UTC) in `XmltvTimestamp` — java.time
  would need API 26 or core-library desugaring at minSdk 23, and epoch longs keep
  Room queries and window math trivial. No wall-clock reads in logic: every
  repository/policy takes an injected `clock: () -> Long`.
- **2026-09-13** XMLTV parsing uses `XmlPullParser` injected as a factory:
  devices pass `android.util.Xml.newPullParser()` (only inside `ServiceLocator`),
  JVM tests pass kxml2's `KXmlParser` (`testImplementation` only). No production
  XML dependency added.
- **2026-09-13** Channel identity across playlist refreshes = `tvg-id` when
  non-blank, else `streamUrl|name` (`ChannelImporter.identityOf`). Favorite and
  hidden flags are carried over by this key; channel numbers are reassigned
  sequentially from playlist order on every import (TiviMate default numbering).
- **2026-09-13** No DI framework: `core/ServiceLocator` is the hand-rolled
  application-scoped composition root (Room database, repositories, refresher).
  It is the only place logic meets the wall clock and Android XML services.
- **2026-09-13** Room schema JSON is exported to `app/schemas` (KSP
  `room.schemaLocation`) and checked in; DAO/repository tests run headless on
  the JVM via Robolectric against in-memory databases.
- **2026-09-13** Playback engine seam: logic talks to `features/player/PlayerEngine`
  (state + video-details StateFlows, load/stop/release). `Media3PlayerEngine`
  wraps an injected `ExoPlayer` (MockK-able interface) so its listener mapping is
  JVM-tested; the real player is built only in `Media3PlayerEngine.create`
  (audio focus via AudioAttributes, HLS+progressive+TS via the default media
  source factory) and hosted by the thin `PlayerScreenSurface` AndroidView.
- **2026-09-13** Tiny key-value store for scalars (`core/kv/KeyValueStore`,
  currently just `lastChannelId`): SharedPreferences-backed
  (`SharedPrefsKeyValueStore`), not Room — one scalar doesn't justify a schema
  bump + migration, and reads must be cheap at cold start. Logic only sees the
  interface; tests use an in-memory map.
- **2026-09-13** Playback key map follows the device-verified catalogue (§3):
  OK/DOWN = info overlay, UP = channel panel at the previous channel
  (wraps 1↔N), long-OK/MENU = context menu, LEFT/RIGHT = no-op at bare
  playback. Deviations, on purpose: CH+/CH− zap directly (the catalogue flags
  its emulator no-zap observation as unreliable), and BACK at bare playback
  opens the channel panel as the stand-in for "return to TV guide" until the
  guide slice exists (so BACK never exits from playback). Info overlay
  auto-hide = 5 s (TiviMate's default panel timeout; premium-tunable, not
  capturable) driven by injected-scheduler `delay`, no wall clock.
- **2026-09-13** The 5.2.0 "channel panel" is really the guide overlay
  (capture 47). This slice ships it as groups column + channel rows
  (number/logo/name/now-programme/progress, 39 dp rows = 78 px pitch) with the
  focused row expanded into the detail card; the timeline grid arrives with
  the guide slice. Group renumbering restarts from 1 (capture 74); favorites
  and hide are the only live context-menu actions, every other captured row
  routes to a branded coming-soon placeholder.
- **2026-09-13** Settings persistence = SharedPreferences behind
  `core/settings/KeyValueStore` (string values only) with a typed
  `SettingsRepository` + `Setting<T>` descriptors (`TellySettings` catalog)
  on top — DataStore would add a dependency for no testability gain; the
  in-memory store keeps every settings test plain-JVM. Captured TiviMate
  defaults are the `Setting` defaults (EPG interval "None"=0, past days 7,
  stats toggle on, …).
- **2026-09-13** Settings UI = two-pane shell (left section list drives the
  right rows pane on focus) rendered from pure `SettingsRow` builders per
  section; row activations dispatch through generic tables
  (`SettingsToggles`, `SettingsPickers`) so the tree stays data, not code.
  Premium-locked reference rows telly cannot honor yet render dimmed +
  padlock and are skipped by focus, exactly like the capture.
- **2026-09-13** Parental PIN is stored salted-SHA-256 (`PinHasher`), never
  raw; `ParentalControls` gates locked groups/settings only while the master
  toggle is on. Backup/restore = pretty JSON (`BackupCodec`, version field,
  kotlinx.serialization) of the raw settings map + playlist identities;
  restored playlists re-fetch channels via "Update playlist". SAF intent
  wiring lives in thin `SettingsScreenHost` (untested; logic fully tested).
- **2026-09-13** Search slice (catalogue §4, captures 49-51): its own
  `Route.Search`, reached from the playback quick-bar's Search slot. Room
  queries live in a dedicated `features/search/db/SearchDao` (LIKE with
  `ESCAPE '\'`; `SearchQuery` escapes `%`/`_`/`\`): channels by name
  WORD-PREFIX (round7 corrected the earlier substring read — see the
  2026-09-14 round-7 bullet) OR number prefix (digits-only queries) in
  case-insensitive name order — live 5.2.0 side-by-side (`tm-02`) showed
  name order, not the zap order originally guessed when the cap was "not
  capturable" — programmes by title word-prefix still airing/upcoming
  (LIMIT 100 — the reference cap is not capturable). **The Programs section is a
  channel-master / airings-detail two-pane (ref-round6 §D supersedes the
  earlier flat soonest-first reading of capture 50):** a vertical master
  lane with ONE 120×103 dp card (logo + name, adjacent) per channel that
  has a matching programme, in case-insensitive NAME order, and a rows
  pane showing ONLY the selected channel's airings, chronological, one
  60 dp row per airing — no title dedupe, and same-titled programmes never
  merge across channels (verified 1:1 against the fixture EPG). Selection
  is ViewModel state (`selectedChannel`): each result batch selects the
  first master channel and focusing a master card swaps the rows pane,
  preselecting that channel's first airing into the right-side detail
  card — while the IME is still up this is state only, D-pad focus stays
  in the query field (`tm-03`, round6 06/07); DOWN from the query bar
  lands on the FIRST channel card via an explicit `FocusRequester`
  (round6 07 — Compose's spatial `moveFocus` picked the nearest card).
  DOWN in the rows pane stops dead at the last airing
  (`focusProperties down = Cancel`). OK on a master card opens the shared
  Unlock Premium screen (round7 device check FLIPPED the earlier
  tunes-like-the-Channels-shelf assumption). Currently-airing rows append
  the shared dash progress +
  "N min" remaining to the times and tint the title light blue, and
  hidden/unknown channels drop out with their airings.
  Air times: bare "03:45 — 05:15 PM"
  today, "Mon, Sep 14, …" prefix otherwise. Search history = newline-joined
  string in its own SharedPreferences file (out of backups), dedupe + cap
  20; committed on IME search and result OK. OK on a channel card persists
  `lastChannelId` and adopts the guide-root BACK chain
  (`replaceAll(Route.Guide)` then `push(Route.Playback)`); OK on a
  programme row opens the guide-cell dropdown (Remind/Record/…, capture 27)
  whose rows — like the gear's premium search settings — open the shared
  Unlock Premium screen exactly like the guide's cells; only the voice orb
  still lands on the branded coming-soon placeholder. Deviation, on purpose:
  the reference dims the live video behind search; telly's search is a
  route (single player engine), so it sits on the flat app background.
- **2026-09-13** Accent color: `LocalAccentColor` CompositionLocal (default
  sampled #2196F3) fed from Settings -> Appearance -> Color theme via
  `ProvideAccentColor`; `AccentPalette` maps captured-style accent names to
  ARGB. `RefreshScheduler` now takes an interval *provider* (settings-driven,
  0 = "None" = only never-fetched data is due) and `EpgRefresher` trims
  programmes past the "Past days to keep EPG" horizon after every run.
- **2026-09-13** TV-guide grid engine is pure logic + thin Compose: the grid
  renders NO focusable cells. A single invisible key anchor feeds
  `GuideController`, whose `GuideFocusEngine`/`GuideFocusNav`/
  `GuideScrollPlanner` own focus + the one horizontal scroll offset that the
  timeline header and every row pan from in lockstep (160 dp per 30 min,
  190 dp channel column, 39 dp rows — uidump 24). Overlaid layers (groups
  column, cell dropdown, paywall) use regular Compose focus and only close
  keys route through `GuideKeyPolicy`. Cells materialize per row via
  `GuideCellLayout` over a `ProgramDao.observeWindow` span quantized to the
  30-min grid (+3 h prefetch); EPG gaps become 30-min "No information"
  filler cells.
- **2026-09-13** Guide semantics, per catalogue §2 + device answers: OK on an
  airing cell = two-stage (tune preview → fullscreen push); OK on any
  non-airing cell = anchored dropdown (Remind/Record/Custom recording/Add to
  My list/Program description), every row opening the shared Unlock Premium
  screen (capture 31); LEFT at the window's left edge = groups column with
  per-group renumbering from 1; UP/DOWN keep the focused time anchor
  (standard TiviMate feel; not capture-verifiable); plain LEFT/RIGHT never
  pan left of "now" (capture 25 reached groups with one LEFT despite 6 h of
  past EPG); long-LEFT/RIGHT jump ±24 h (the hint toast's "navigate to past
  programs"; emulator-inconclusive, clamped to the "Past days to keep EPG"
  setting). No elapsed fill on the current cell — the device answers verify
  progress is conveyed only by the now-line + info-pane pill.
- **2026-09-13** BACK chain now matches the device-verified free build:
  bare-playback BACK and the info overlay's TV-guide card both
  `replaceAll(Route.Guide)` (the guide is always the stack root when
  visible), stage-two OK pushes `Route.Playback`, and BACK at guide root
  exits the app with no confirmation (the guide's BackHandler is disabled at
  the grid layer). The panel stays on UP/overlay keys. Cold start still
  lands on playback per the watch-and-zap spec; the guide therefore always
  resumes the last-watched channel in its preview window.
  `OkLongPressDetector` generalized into `core/input/HoldKeyDetector<T>` so
  LEFT/RIGHT holds can drive day jumps without duplicate detector code.
- **2026-09-14** Guide row context sheet (catalogue §3 38-40 CORRECTED round3 +
  round3-ref/05, dump 38): long-OK / MENU on a focused guide row opens the
  full right-side sheet with the grid still visible behind — the same
  `PlaybackScreenMenu` the playback panel uses (256 dp wide, 8 dp off
  top/right, 40 dp row pitch, focus pill inset 8 dp; 2 px = 1 dp), sections
  verbatim from `PlayerMenu`. Layer transitions live in `GuideMenuController`
  with one-level BACK popping (pushed screens → sheet → grid). Live rows:
  Search (Route.Search), Settings (right sheet), Add to/Remove from
  Favorites, Hide channel (zap-away first), Program description (title +
  synopsis from the focused cell's info). Premium-locked reference rows open
  the shared Unlock Premium screen — Record/Custom recording/Add to My list
  (capture 31), Open in external player + Block channel (locked in capture
  41), Manage Favorites + Reorder channels (sold as premium in capture 28's
  paywall body); everything else (Assign EPG, Manage blocking/visibility,
  Copy channels, Create group, Group options) is uncaptured → branded
  coming-soon. "Channel options" pushes a settings-shell pane titled with
  the channel name whose §41 rows are all locked (`GuideChannelOptions`),
  BACK pops one level.
- **2026-09-14** Panel sheet = guide sheet routing, one table: the long-OK
  context sheet's row→destination map is derived ONCE in
  `features/playback/PlayerMenuRouting` (`PlayerMenuRoute`: Search /
  Settings / favorites toggle / hide / description / channel options /
  paywall / coming-soon) and both `GuideMenuController` and
  `PlaybackMenuHandler` consume it, so the two sheets can never drift.
  The panel side gained the pushed screens as `PlaybackOverlay.Pushed`
  overlays carrying a `back` overlay (paywall / description / channel
  options / coming-soon), popped one level by BACK via
  `PlaybackCommand.PopTo` — mirroring the guide's backOf chain (pushed
  screen → sheet → panel). The §41 locked "Channel options" pane is the
  shared `GuideScreenChannelOptionsPane` (`GuideChannelOptions` rows),
  reused, not copied; the panel sheet's description reads the row's
  airing programme from `PanelRow` (title + synopsis).
- **2026-09-14** History card (capture 34's second 150×110 card): capture 48's
  uidump — taken right after the History press — is bare playback (zero text
  nodes, one focused full-screen ViewGroup), so 5.2.0 free has no History
  screen; per the capture-47 note ("opens the same overlay with History as
  source group when it exists") and §2 (no persistent History group, capture
  25), the card opens the guide (`Route.Guide(historySource = true)`, same
  replaceAll-root BACK chain as the TV-guide card) on a synthetic
  **History** source group: recently watched channels newest-first, deduped
  per channel, renumbered from 1 like every non-All group, leading the
  groups column only while it is the selected source group. Persistence =
  Room `watch_history` table (v3, MIGRATION_2_3), keyed by the
  playlist-refresh-stable channel identity (`ChannelImporter.identityOf` —
  row ids are reassigned on import), stamped by the injected clock, capped
  at 30 (`WatchHistory.CAP`; the reference cap is not capturable). Events
  are recorded in `TuneController.tune`, the same commit point as
  `lastChannelId`, so guide preview tunes count as watches too.
  **CORRECTED (history-round2, 2026-09-15) — capture-48's "bare playback"
  reading is WRONG for a history-rich install; evidence-only, rework is the
  next slice (`docs/reference/sidebyside/history-round2/`):** with real
  accumulated history the fullscreen info overlay shortcut row is
  **TV guide · History · N recent-channel cards · Clear** (capture-48's
  install had no recent/Clear cards). A **recent-channel card** = channel
  LOGO + that channel's CURRENT programme title in accent blue (not
  name/number); focusing one shows a bottom `air-time + programme title`
  line. **OK on a recent-channel card → shared Unlock Premium** in free tier
  (verified on a Music- AND a News-group card — universal, not a direct zap;
  BACK → player, no zap). **OK on the History card → a DISTINCT full-screen
  "History" list surface** — NOT bare playback, NOT the guide overlay, NOT
  an "EPG schedule browser": a leanback VerticalGrid, title "History"
  top-RIGHT + a clear-all trash icon, dimmed video behind, showing
  "No history" this session (the standalone History log and the info-row
  recent cards are separate sources; populated it would be a grid of channel
  cards). **BACK from the History screen → fullscreen player.** The info-row
  **Clear** card (trash + "Clear", rightmost) clears the recent-channel row
  (not activated — read-only round). telly's current synthetic-History-
  group-in-the-guide model does NOT match this; the rework (a distinct
  History list surface + a recent-channel cards row in the info overlay) is
  the next slice.
- **2026-09-14** Sheet-close focus restore (round5 punch list): the grid focus
  engine's state already survived a plain sheet open/close (the round-5
  "returns to the now-cell" read happened because the originating cell WAS the
  airing cell) — what could actually drift was the POSITIONAL row index while
  the sheet was up (rows re-emitting from a playlist/EPG refresh or the sheet's
  own favorite/hide land focus on whatever slid into that index). Now
  `GuideFocusMemory` snapshots (channel id, cell, anchor, scrollX, firstRow)
  when `GuideMenuController.openRowMenu` fires and re-asserts it, consumed
  once, on the first transition back to `GuideLayer.Grid`; a channel hidden
  from its own sheet keeps the engine's index-resolved fallback. Inside the
  shared sheet, rows that push screens (description / Channel options /
  paywall / coming-soon — derived from `PlayerMenuRouting`) remember
  themselves in `PlayerMenuFocus` (owned by both `GuideMenuController` and
  `PlaybackMenuHandler`, so the panel sheet restores for free) and
  `PlaybackScreenMenu` re-focuses that row on pop-back — lazy-scrolling it
  into view only when it sits below the fold — instead of resetting to
  Search; a freshly opened sheet still lands on Search. Restore-to-origin is
  the assumed TiviMate behavior (standard leanback focus memory) —
  **confirmed by ref-round6 §B** (originating-row restore) with one
  correction: the Channel-options path never returns to the sheet at all
  (see the 2026-09-14 ref-round6 bullet below).
- **2026-09-14** Sheet motion + Channel-options replace semantics, per the
  freshly captured ref-round6 evidence (`docs/reference/sidebyside/
  ref-round6/README.md` §A/§B): the shared long-OK sheet
  (`PlaybackScreenMenu`, both guide and panel hosts) now ENTERS with a
  250 ms FastOutSlowIn fade + 5 dp slide-in from the right
  (`PlaybackScreenMenuMotion.kt`) — the reference's one emitted mid-frame
  at ~140 ms sits at ~78-80 % opacity and 9-10 px (= 5 dp) from settled,
  which FastOutSlowIn hits at ~79 % of 250 ms, inside the measured
  150-280 ms envelope — over a 72 %-black backdrop scrim
  (`PlaybackScreenMenuScrim`; grid dim measured (15,20,22) → (4,6,6)) that
  settles within a frame of the sheet. CLOSE = instant cut of the sheet
  (no exit transition; the layer switch uncomposes it) followed by the
  scrim's 300 ms fade-out, so the scrim lives in the hosts, outside the
  layer switch. Channel options REPLACES the sheet (reference cross-fades
  in place; sheet fades out on push): BACK from the pane lands DIRECTLY on
  the guide grid (`backOf` → `Grid`, originating-row focus restored by
  `GuideFocusMemory`) / the playback panel (`ChannelOptions.back =
  Panel`) — never back on the sheet — and `PlayerMenuFocus` dropped its
  now-dead Channel-options case. The one-level pop back to the sheet
  stays for the uncaptured pushed layers (description/paywall/
  coming-soon). Exact timings remain to be eyeballed on-device.
- **2026-09-14** Round-7 on-device verification (evidence
  `docs/reference/sidebyside/round7/`, verdicts `round7-punchlist.md`)
  corrected three round-6-era readings and confirmed the rest:
  **(1) Sheet scrim = 60 % black, not 72 %.** Round 6 measured the dim on
  screenrecord frames, whose limited-range video encode crushes darks;
  screencap probes put the true factor at ×0.40–0.42 on live TiviMate.
  `MENU_SCRIM` is 0x99000000 (same value the settings sheet already used)
  and telly now measures ×0.40–0.42 on both hosts. New P2: the reference
  does NOT dim the originating row under the sheet (white outline, full
  brightness); telly dims uniformly.
  **(2) Channel-options cross-fade implemented:** the shared
  `PlaybackScreenMenuSurfaceSwitch` (`AnimatedContent` keyed on a
  `PlayerMenuSurface` contentKey, in `PlaybackScreenMenuMotion.kt`, hosted
  by `GuideScreen` and `PlaybackScreenMenuLayers`) fades the sheet out
  170 ms under the pane's 350 ms fade-in on push and fades the pane out
  140 ms on pop, landing directly on the grid/panel with the originating
  row focused; every other surface change stays an instant swap so the
  sheet's 250 ms entrance and instant close are untouched.
  **(3) Search matches per-word PREFIXES, not substrings** ("xtra"/"room"
  find nothing; "o" only reaches "One", "spec"/"epis" reach "…Special"/
  "Episode…"): `SearchQuery.nameLike` = `"% q%"` matched against
  `' ' || column` in both `SearchDao` queries.
  **(4) OK on a Programs master card opens the shared Unlock Premium
  screen** — live 5.2.0 does not tune from the master lane
  (`SearchViewModel.onProgramChannelResult`).
  Also per live probes: the selected-but-unfocused master card carries a
  1 dp grey outline (0xFF37393C sampled; `restingOutline` on
  `SearchScreenFocusRow`, drawn outside the 42 % alpha layer); DOWN from
  the query bar with NO channel matches lands on the first AIRING ROW
  (fixed; with channels it stays the first card, and after visiting a
  scrolled shelf the reference restores the LAST-focused card — logged P2,
  telly still targets card 1); each result batch resets the selection to
  the first master card in the reference too (telly already matched); the
  detail-card title needs `letterSpacing = 0.sp` to fit the reference's
  one-line layout. Two-pane geometry verified within a few px (P3 nits
  logged). Acceptance legs after fixes: search 12/12, tv-guide 16/16
  (one steps-layer anchor fix), channel-panel 13/13.
- **2026-09-14** Background/resume lifecycle (round7 resume P2, evidence
  `docs/reference/sidebyside/round7/resume-fix/`). Live TiviMate, warm-resumed
  (same pid) after 90 s AND 11 min backgrounded: it abandons audio focus and
  releases its codecs the INSTANT it is backgrounded (logcat), and every
  resume lands on the TV GUIDE — even from fullscreen playback — with the
  header clock re-anchored and no zap overlay or spinner; its guide clock
  also ticks per minute while open. telly's root causes were "now" sampled
  once per GuideController build (never re-seeded; the cached-app freezer
  froze the process, so the composition survived with a 20-min-old clock)
  and no player lifecycle at all (the stream kept pulling in background
  until audio-focus loss, then sat paused/stale → black surface on resume).
  Pattern now: screens owning the tuner mount `core/ui`
  `ScreenLifecycleStartStop` (DisposableEffect + LifecycleEventObserver;
  ON_START only counts after a real ON_STOP since observer registration
  replays states) driving a `PlaybackLifecycle` (features/playback):
  ON_STOP → `TuneController.suspendPlayback()` (engine.stop),
  ON_START-after-stop → `onForegrounded` re-seed then `recover` — the guide
  recovers by re-tuning its preview (`TuneController.retune`; telly's guide
  auto-tunes by charter while TiviMate's free-build preview stays untuned
  until OK), fullscreen recovers by `exitToGuide` exactly like the
  reference. Guide "now" is a `GuideNow` minute ticker (injected clock +
  delays on the injected scope, re-seeded on foreground) feeding the header
  clock, now-line, info pane and activation — the earlier "sampled once per
  open" reading is superseded; `originMs` stays anchored at build time. No
  wall-clock reads anywhere new.
- **2026-09-14** Round-7 P2/P3 follow-ups (JVM-verified, on-device pending):
  **(1) Search DOWN focus memory** — `SearchFocusMemory` (ViewModel state)
  remembers the last results-area node D-pad focus visited (Channels card /
  Programs master card / airing row, identity-keyed via a sealed `Node`);
  `searchRestoreTarget` pins one restore `FocusRequester` onto the matching
  composed node and the query bar's DOWN tries remembered → first-node →
  spatial (`SearchFocusMemory.targets`), so the round-7-verified fresh
  landings are the fallback. A new result batch clears the memory (the
  reference's post-change behavior is uncaptured); re-typing the same query
  keeps it (StateFlow dedupe).
  **(2) Undimmed originating row under the guide sheet** — the reference
  dims everything EXCEPT the long-OK row (round7 screencaps: row pixels
  identical rest vs sheet-open, white outline intact). `GuideFocusMemory`
  now keeps the origin channel id past the restore; pure
  `GuideDimExemption.bandTopDp` maps it to the CURRENT row's fixed-geometry
  band (GRID_TOP 234 dp + 39 dp pitch, null off-viewport/vanished) and
  `GuideScreenMenuScrim` punches the band out of the shared scrim with
  `BlendMode.Clear` on an offscreen layer, lingering ~300 ms through the
  scrim fade-out. Guide host only: the panel host keeps the uniform scrim
  (no grid rows behind it; evidence covers the guide).
  **(3) P3 tokens** — Programs two-pane top inset `programsTop` 22 dp
  (was shelfTop 14) and `TellyScreenTimesLine` gained a `fontSize`
  parameter (default 15 sp unchanged elsewhere; search airing rows pass
  13 sp, detail card 14 sp).
- **2026-09-15** Playback-core parity: `PlayerEngine` gains pause/resume +
  `PlayerState.Ended`; catch-up pauses with the transport pinned, archive end
  retunes live, and prev/next hop programmes via `CatchupNeighbours`; the live
  transport shows a red record dot (instant-record toggle), finishing a VOD
  clears its resume position, and recordings stop at programme end.
- **2026-09-15** Every persisted-but-dead setting is honored (settings-real
  merge). **Start/exit:** `StartRoute` is a policy class — "Turn on last
  channel on app start" **default flipped to ON** (telly previously always
  restored; OFF cold-starts on an untuned guide via the one-shot
  `consumeUntunedStart` → `GuideStartPolicies.resumePreview`), and "Confirm
  exit by second press Back" (`ExitConfirm`, 5 s window + "Press BACK again
  to exit" toast) gates the guide-root BACK only when ON — the default-off
  first-BACK exit the e2e suite pins is unchanged. "Auto start app on boot"
  = manifest `BootReceiver` (BOOT_COMPLETED) + `TellyApplication`'s runtime
  SCREEN_ON receiver, both through the `Autostart` policy. **Audio:**
  `PlayerAudioPrefs` feeds `Media3PlayerEngine.create` — passthrough (read
  once per engine build) swaps the capability-forcing `PassthroughAudioSink`
  INTO the shared `OffsetRenderersFactory` (ONE factory composes passthrough
  + the audio-sync offset sink), and "Select surround audio track by
  default" lives in `ExoTrackFacade.onTracksChanged` (`SurroundAudio.pick`,
  most channels wins) where it COMPOSES with the quick-bar picker: an
  explicit user audio pick always beats surround, and a zap (track groups
  replaced) lapses the pick and re-arms the default. **Streams:** "Use proxy
  for UDP streams" = `UdpProxy.resolve` in `TunePolicies.resolveUrl` —
  applied to LIVE tunes/retunes and multiview pane tunes (`MultiviewPanes`),
  never to catch-up archive URLs (template-built http(s), never udp).
  **Clock:** `ClockStyle` (zone + live 12/24h provider) threads through
  guide header/timeline, playback info, panel, history, catch-up readouts
  AND — beyond the branch — search air-times and the multiview picker;
  reminders/recordings/My-list keep the bare-zone 12-hour overloads.
  **Parental:** "Don't require PIN for channels only" exempts watching,
  "Require PIN for Playlists" gates the section via the ONE unified
  `SettingsOverlay.PinVerify(section?)` (null = blocked-channels pane), and
  "PIN input method" = Keyboard swaps every PIN prompt (settings dialogs +
  panel/guide/playback block gates) to the masked 4-digit IME entry
  (`SettingsScreenPinEntry`). **EPG:** "Update EPG on playlists change"
  picks forced-vs-due refresh on playlist changes; the minute-tick due-retry
  loop stays. Resize mode (`ResizeModes`/`ProvideResizeMode`) reaches the
  fullscreen surface and multiview panes.
- **2026-09-15** Channel options pane live + engine honors buffer/decoder
  settings. **Per-channel overrides** = `ChannelOverrides` embed on
  `channels` (customName / audioDecoder / videoDecoder / epgOffsetMinutes /
  externalPlayer; schema v11, `ChannelOptionsMigration.MIGRATION_10_11`),
  carried over playlist refreshes by identity like `ChannelFlags`. The §41
  pane's rows are LIVE in both hosts (guide + panel sheet, shared
  `ChannelOptionsController`): rename/Restore channel name (blank = playlist
  name), the decoder pickers, "Use external player" (per-channel wins over
  the global setting at tune — `ExternalPlayer.maybeLaunch(url, override)`,
  the UDP-proxy-resolved live URL), "EPG time offset, h:min", and Block/Hide
  reusing the sheet's PIN-gate/zap-away flows verbatim; "Channel names
  editor" pushes `Route.ChannelNames` (bulk rename list). **displayName**
  (`overrides.customName` else `source.name`) is what every channel-facing
  surface renders — guide, panel, playback info, history, my-list,
  recordings, settings-blocked, and (extended at merge, since main owns them
  now) search result rendering + multiview pane labels; search SQL keeps
  matching the PLAYLIST name (display-only rename). **EPG offset** =
  `EpgOffsets`: `EpgRepository.programsFor` AND `nowNext` shift each
  channel's programme times by its offset (padded window query, shift, trim),
  so grid/overlays/now-next move together and catch-up
  (`CatchupNeighbours`/guide archive math) inherits the shifted times via
  `programsFor`. **Engine honors Playback settings** at build time:
  `PlayerTuning` (Buffer size → `DefaultLoadControl` durations; Small = stock
  Media3, Medium/Large ×2/×4 target) + `DecoderPreferences`/
  `PreferenceMediaCodecSelector` (SOFTWARE re-ranks the MediaCodec list
  software-first per mime type — re-ranked, never filtered, so missing
  software codecs fall back to hardware); a tune applies the channel's
  decoder overrides via `engine.decoders.overrideWith(...)` BEFORE load.
  ONE engine builder — `ServiceLocatorEngines.tunedEngine` — composes
  user-agent + `PlayerAudioPrefs` (passthrough/surround) + `PlayerTuning`
  into `Media3PlayerEngine.create`; playback, guide, multiview panes, VOD
  and recordings all build through it.
