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
| File length | every logic `.kt` under `app/src/main` ≤ 100 lines (tests/generated exempt) | `scripts/check-file-lines.mjs` |
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
