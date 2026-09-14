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
  substring OR number prefix (digits-only queries) in case-insensitive
  name order — live 5.2.0 side-by-side (`tm-02`) showed name order, not
  the zap order originally guessed when the cap was "not capturable" —
  programmes by title substring still airing/upcoming, soonest first
  (LIMIT 100 — the reference cap is not capturable). Currently-airing
  programme rows append the shared dash progress + "N min" remaining to
  the times and tint the title light blue, and the first programme hit is
  preselected into the right-side detail card while the IME is still up
  (`tm-03`, ref 50) — state only, never moving D-pad focus off the query
  field. Programme rows drop hidden/unknown
  channels and share one channel card per consecutive same-channel run
  (capture 50's card-to-rows ratio). Air times: bare "03:45 — 05:15 PM"
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
