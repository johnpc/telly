#!/usr/bin/env bash
# Runs one Gherkin acceptance area against the already-booted emulator.
# Usage: scripts/ci-acceptance.sh <feature-area>   (cucumber tag = @<area>)
# Collects logcat + failure screenshots into artifacts/ for the CI upload.
set -uo pipefail

FEATURE="${1:?usage: ci-acceptance.sh <feature-area>}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

mkdir -p artifacts
# A device that is momentarily "offline" (adb reconnecting after the previous
# leg) must not turn into a failed leg: wait for it to come back first.
adb wait-for-device
adb logcat -c || true
adb logcat > "artifacts/logcat-$FEATURE.txt" 2>&1 &
LOGCAT_PID=$!

# E2E_GRADLE_ARGS lets a local run add e.g.
# -Pandroid.testInstrumentationRunnerArguments.screenshots=all (a screenshot
# per scenario for UX review); CI passes nothing extra.
# shellcheck disable=SC2086
./gradlew connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.tags=@$FEATURE" ${E2E_GRADLE_ARGS:-}
status=$?

# Per-scenario screenshots (failures always; every scenario with
# -Pandroid.testInstrumentationRunnerArguments.screenshots=all) are pulled by
# AGP into the additional-output dir before the app is uninstalled.
EXTRA=app/build/outputs/connected_android_test_additional_output
if [ -d "$EXTRA" ]; then
  find "$EXTRA" -name '*.png' -exec cp {} "artifacts/" \; 2>/dev/null || true
fi
if [ $status -ne 0 ]; then
  adb exec-out screencap -p > "artifacts/final-screen-$FEATURE.png" || true
fi

kill "$LOGCAT_PID" 2>/dev/null || true
exit $status
