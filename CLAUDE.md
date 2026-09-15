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
