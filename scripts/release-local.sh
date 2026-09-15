#!/bin/sh
# Local replacement for .github/workflows/android-deploy.yml — use when GitHub
# Actions credits are unavailable. Builds the unsigned release APK, runs the
# full quality gate first, and publishes a GitHub Release via the API (gh),
# which costs no Actions minutes.
#
# Usage: ./scripts/release-local.sh
set -eu

cd "$(dirname "$0")/.."

./scripts/quality.sh

./gradlew assembleRelease

VERSION=$(grep 'versionName = ' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')
# Continue the workflow's run-number sequence: next number after the latest
# existing v<version>-N tag (local builds and Actions builds share one line).
LAST=$(gh release list --limit 100 --json tagName \
  --jq "[.[].tagName | select(startswith(\"v$VERSION-\")) | split(\"-\")[1] | tonumber] | max // 0")
BUILD=$((LAST + 1))
TAG="v$VERSION-$BUILD"

mkdir -p dist
cp app/build/outputs/apk/release/app-release-unsigned.apk "dist/telly-$TAG.apk"

gh release create "$TAG" \
  --title "telly v$VERSION (build $BUILD)" \
  --notes "Built and gate-verified locally (GitHub Actions unavailable). Commit: $(git rev-parse HEAD)" \
  "dist/telly-$TAG.apk"

echo "Published $TAG from $(git rev-parse --short HEAD)"
