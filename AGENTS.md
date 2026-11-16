# AGENTS.md — how agents (and humans) work in telly

Read [CLAUDE.md](CLAUDE.md) first: it is the charter, the quality gates and
the decisions log. This file is the operating manual on top of it — the
rules that keep the user experience, the developer experience and the agent
experience moving together.

## 1. Reliability: there is no such thing as a flaky test

A test that passes sometimes is a failing test. When a CI leg or a local run
fails intermittently:

1. **Do not re-run to green.** Pull the artifacts (`artifacts/` — logcat,
   failure screenshot) and find the race.
2. **Fix the race, not the symptom.** Wait on a real condition (a node
   exists, a flow emitted, the input system accepted the key, the IME is
   gone), never on a fixed sleep. If a step must retry, it retries on an
   observed refusal (e.g. `UiDevice.pressKeyCode` returned false) and logs
   each retry so the rate stays visible.
3. **Pin it.** Add the deterministic wait to the shared harness
   (`app/src/androidTest/.../TellyWorld.kt`, `PlaybackDriver.kt`) so every
   scenario inherits it; add a JVM test when the race is in logic.
4. **Never loosen.** No raised timeouts "to be safe", no `@Ignore`, no
   skipped legs, no `continue-on-error`. The gate stays; the code moves.

The same rule applies to the quality gates: thresholds never move.

## 2. State machines: one mode, one owner

Every mode the app can be in is listed in
[docs/state-machines.md](docs/state-machines.md) with its states, the ONE
file that owns its transitions, and its tests. `scripts/check-state-machines.mjs`
(part of `./scripts/quality.sh`) fails when a cited owner or test file goes
missing, so the catalog cannot rot.

Before adding a boolean, a nullable "pending" field, a counter or a
`consumeX()` one-shot, ask: which machine is this part of? Add the state to
its sealed type and the transition to its owner instead. If the machine is
not in the catalog yet, add a row (status `implicit`) first, then convert.

## 3. Verification happens on the Mac mini

The Mac mini (`johns-Mac-Mini.local`, user `john`, Apple Silicon) is the
CI runner and the verification box. It has JDK 17, the Android SDK at
`/opt/homebrew/share/android-commandlinetools`, the Android emulator and an
Android TV API 36 arm64 AVD named `telly-tv36`.

```sh
# sync the work tree (never .git / build output)
rsync -az --delete --exclude build --exclude .gradle --exclude '*/build' \
  --exclude .git --exclude docs/reference ./ john@johns-Mac-Mini.local:~/repo/telly/

# on the mini: emulator, gate, one acceptance leg, screenshots, recordings
A=/opt/homebrew/share/android-commandlinetools
export PATH=$A/platform-tools:$A/emulator:$HOME/actions-toolcache/node/22.23.4/arm64/bin:$PATH
export JAVA_HOME=$(/usr/libexec/java_home -v 17) ANDROID_HOME=$A
emulator -avd telly-tv36 -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -no-snapshot-save &
adb wait-for-device; until [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" = 1 ]; do sleep 3; done
bash scripts/quality.sh                 # the authoritative gate
bash scripts/ci-acceptance.sh settings  # one Gherkin area; artifacts/ on failure
adb exec-out screencap -p > shot.png    # look at the real UI
adb shell screenrecord --time-limit 30 /sdcard/clip.mp4 && adb pull /sdcard/clip.mp4
```

CI's acceptance job runs the same way on the same machine (`scripts/emulator.sh
start` → `scripts/ci-acceptance.sh <area>` → `scripts/emulator.sh stop`, one
leg at a time), so a green leg here predicts a green leg there.

Rules of engagement: no `sudo`, no global installs, never touch
`~/actions-runner-*/_work`, and check `pgrep -fl Runner.Worker || echo idle`
before a long run. Run long commands detached (`nohup … > /tmp/x.log`) and
read the log; never hold an interactive ssh open for a gate.

A UI change is not done until you have looked at it: take the screenshot or
recording, compare against TiviMate where the charter says so, and attach it
to the PR. The acceptance harness can do the looking at scale:

```sh
E2E_GRADLE_ARGS="-Pandroid.testInstrumentationRunnerArguments.screenshots=all" \
  bash scripts/ci-acceptance.sh catch-up
ls artifacts/            # passed-<scenario>.png / FAILED-<scenario>.png + logcat
```

Screenshots are written to AGP's `additionalTestOutputDir`, which the
connected-test task pulls out BEFORE uninstalling the app (the old
external-files location was wiped by that uninstall, so CI never kept one).

## 4. The fake backend

- **CI / e2e:** a `MockWebServer` inside the instrumentation process
  (`app/src/androidTest/.../fixtures/FixtureServer.kt`) serves the playlist,
  EPG variants, logos and stream slices on `127.0.0.1:8090`, re-anchored to
  "now" per scenario. Feature text uses the dev host `10.0.2.2`; steps
  rewrite it. Nothing in a scenario may reach the network.
- **Demo / store review:** the same generator (`e2e/fixtures/gen-fixtures.mjs
  --base … --out …`) emits a self-contained static site that the
  `Demo site` workflow publishes daily to GitHub Pages:
  `https://johnpc.github.io/telly/playlist.m3u` (EPG at `/epg.xml`,
  `/epg.xml.gz`, second source `/epg-alt.xml`). Point the app — or an App
  Store reviewer — at it when a real provider cannot be shared. It is
  public, synthetic content: never put anything private in `e2e/fixtures/`.

## 5. Conventions in brief

- Conventional commits; PR title `type(scope): what`; demo artifact on UI PRs.
- Composables thin and named `*Screen*.kt`; logic in plain JVM-tested classes.
- Every slice starts as a `.feature` under `e2e/features/` and is mapped in
  the CI acceptance matrix.
- Finish the whole job: gate green on the mini, acceptance legs for the areas
  you touched, screenshots looked at, decisions log updated.
