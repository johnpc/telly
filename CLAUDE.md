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
- **2026-09-13** Accent color: `LocalAccentColor` CompositionLocal (default
  sampled #2196F3) fed from Settings -> Appearance -> Color theme via
  `ProvideAccentColor`; `AccentPalette` maps captured-style accent names to
  ARGB. `RefreshScheduler` now takes an interval *provider* (settings-driven,
  0 = "None" = only never-fetched data is due) and `EpgRefresher` trims
  programmes past the "Past days to keep EPG" horizon after every run.
