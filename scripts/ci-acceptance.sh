#!/usr/bin/env bash
# Runs one Gherkin acceptance area against the already-booted emulator.
# Usage: scripts/ci-acceptance.sh <feature-area>   (cucumber tag = @<area>)
# Collects logcat + failure screenshots into artifacts/ for the CI upload.
set -uo pipefail

FEATURE="${1:?usage: ci-acceptance.sh <feature-area>}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

mkdir -p artifacts
adb logcat -c || true
adb logcat > "artifacts/logcat-$FEATURE.txt" 2>&1 &
LOGCAT_PID=$!

./gradlew connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.tags=@$FEATURE"
status=$?

if [ $status -ne 0 ]; then
  adb exec-out screencap -p > "artifacts/final-screen-$FEATURE.png" || true
  adb pull /sdcard/Android/data/com.johncorser.telly/files/screenshots \
    "artifacts/screenshots-$FEATURE" || true
fi

kill "$LOGCAT_PID" 2>/dev/null || true
exit $status
