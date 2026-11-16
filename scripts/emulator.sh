#!/usr/bin/env bash
# Boots / stops the headless Android TV emulator for the acceptance legs.
# Usage: scripts/emulator.sh start [avd] | stop | ensure-avd [avd]
#   start      boot the AVD headless (software GPU, no audio/snapshots) and
#              block until sys.boot_completed = 1 (fails after 5 min)
#   stop       kill every running emulator and wait until adb lists none
#   ensure-avd create the AVD when it does not exist yet (needs the
#              android-tv arm64/x86_64 API 36 system image installed)
# The AVD name defaults to telly-tv36 (the Mac mini's); CI passes its own.
set -euo pipefail

AVD="${2:-telly-tv36}"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/opt/homebrew/share/android-commandlinetools}}"
export PATH="$SDK/platform-tools:$SDK/emulator:$SDK/cmdline-tools/latest/bin:$PATH"

case "${1:-}" in
  ensure-avd)
    if ! avdmanager list avd 2>/dev/null | grep -q "Name: $AVD\$"; then
      arch=$(uname -m); [ "$arch" = "arm64" ] && abi=arm64-v8a || abi=x86_64
      echo no | avdmanager create avd -n "$AVD" -k "system-images;android-36;android-tv;$abi" -d tv_1080p --force
    fi
    ;;
  start)
    nohup emulator -avd "$AVD" -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim \
      -no-snapshot-save -no-snapshot-load > "${RUNNER_TEMP:-/tmp}/emulator-$AVD.log" 2>&1 &
    adb wait-for-device
    for _ in $(seq 1 100); do
      if [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
        adb shell settings put global window_animation_scale 0 >/dev/null
        adb shell settings put global transition_animation_scale 0 >/dev/null
        adb shell settings put global animator_duration_scale 0 >/dev/null
        echo "emulator $AVD booted"
        exit 0
      fi
      sleep 3
    done
    echo "emulator $AVD did not boot in time" >&2
    tail -20 "${RUNNER_TEMP:-/tmp}/emulator-$AVD.log" >&2 || true
    exit 1
    ;;
  stop)
    for serial in $(adb devices | awk '/^emulator-/ {print $1}'); do
      adb -s "$serial" emu kill >/dev/null 2>&1 || true
    done
    for _ in $(seq 1 30); do
      adb devices | grep -q '^emulator-' || exit 0
      sleep 1
    done
    pkill -f "emulator -avd" || true
    ;;
  *) echo "usage: $0 start [avd] | stop | ensure-avd [avd]" >&2; exit 2 ;;
esac
