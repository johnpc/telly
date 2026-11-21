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
# Shared storage outlives the app: a backup an earlier leg auto-exported would
# make this leg's "fresh install" offer "Restore previous setup" (the harness
# cannot delete another owner's file under scoped storage, adb shell can), and
# that pill then holds the initial focus "Add playlist" expects (run
# 44133018036: the welcome scenario timed out on focus; the failure screenshot
# showed the restore pill). The first version of this wipe ran blind right
# after boot and swallowed its errors; it left the file behind. Shared storage
# (FUSE, user 0) comes up a little after sys.boot_completed, so wait until a
# shell write to /sdcard works, then wipe the file AND its MediaStore rows
# (the welcome probe reads both) and VERIFY, failing loudly instead of leaving
# a 30 s focus timeout to explain.
BACKUP_DIR=/sdcard/Documents/telly
for _ in $(seq 1 60); do
  adb shell 'touch /sdcard/.telly-ci-probe && rm /sdcard/.telly-ci-probe' >/dev/null 2>&1 && break
  sleep 1
done
adb shell "rm -rf $BACKUP_DIR"
adb shell content delete --uri content://media/external/file \
  --where "\"relative_path LIKE 'Documents/telly/%'\"" || true
if adb shell "ls $BACKUP_DIR" >/dev/null 2>&1; then
  echo "::error::$BACKUP_DIR survived the pre-leg wipe; the welcome screen would offer 'Restore previous setup'" >&2
  adb shell "ls -la $BACKUP_DIR" >&2 || true
  exit 1
fi
ROWS=$(adb shell content query --uri content://media/external/file --projection _display_name:relative_path \
  --where "\"relative_path LIKE 'Documents/telly/%'\"" 2>&1 | tr -d '\r')
if echo "$ROWS" | grep -q '^Row:'; then
  echo "::error::MediaStore still lists Documents/telly rows after the pre-leg wipe (the welcome probe counts them): $ROWS" >&2
  exit 1
elif ! echo "$ROWS" | grep -q 'No result found'; then
  echo "::warning::could not verify MediaStore rows for Documents/telly: $ROWS" >&2
fi
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
