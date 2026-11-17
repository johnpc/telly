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
    log="${RUNNER_TEMP:-/tmp}/emulator-$AVD.log"
    # The Mini is shared: other repos' runners start at boot outside the CI
    # coordinator, so an iOS build (xcodebuild + simulators) can overlap this
    # leg. On 16 GB that pushes the machine deep into swap and the emulator
    # stalls (2026-11-16: a 6.8 s video freeze, an 8 s activity launch and a
    # failed channel-panel leg). Yield until no xcodebuild is running (at most
    # 30 min) so the leg starts on a quiet machine.
    for i in $(seq 1 180); do
      pgrep -q xcodebuild || break
      [ "$i" = 1 ] && echo "waiting for another repo's xcodebuild on this machine to finish..."
      sleep 10
    done
    # One adb server up front: platform-tools 37's server has crashed and
    # restarted on every poll while the emulator was still half up (macOS 27).
    adb start-server >/dev/null 2>&1 || true
    # No nohup: under launchd/the Actions runner there is no controlling
    # console and nohup exits with "can't detach from console" WITHOUT running
    # the command (the emulator never started and the old adb wait-for-device
    # blocked a CI leg for 20 min). A disowned background process survives the
    # step; stdin from /dev/null so it never waits on a terminal.
    emulator -avd "$AVD" -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim \
      -no-snapshot-save -no-snapshot-load < /dev/null > "$log" 2>&1 &
    disown
    for _ in $(seq 1 100); do
      if ! kill -0 "$!" 2>/dev/null; then
        echo "emulator $AVD exited early" >&2
        tail -20 "$log" >&2 || true
        exit 1
      fi
      if adb devices 2>/dev/null | grep -q '^emulator-[0-9]*[[:space:]]*device' &&
        [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
        adb shell settings put global window_animation_scale 0 >/dev/null
        adb shell settings put global transition_animation_scale 0 >/dev/null
        adb shell settings put global animator_duration_scale 0 >/dev/null
        echo "emulator $AVD booted"
        exit 0
      fi
      sleep 3
    done
    echo "emulator $AVD did not boot in time" >&2
    tail -20 "$log" >&2 || true
    "$0" stop
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
