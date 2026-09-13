#!/usr/bin/env bash
# Full quality gate for telly. Run from anywhere; CI and the pre-commit hook
# both call this script. Every gate must pass — fix the code, never the gate.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [ -z "${JAVA_HOME:-}" ] && [ -d /Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home ]; then
  export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home
fi

step() { printf '\n=== %s ===\n' "$1"; }

step "ktlint"
./gradlew --quiet ktlintCheck

step "detekt"
./gradlew --quiet detekt

step "file length (<=100 lines per logic .kt)"
node scripts/check-file-lines.mjs

step "build (assembleDebug)"
./gradlew --quiet assembleDebug

step "unit tests + coverage report"
./gradlew --quiet testDebugUnitTest jacocoTestReport

step "coverage floor (>=80% line)"
./gradlew --quiet jacocoCoverageVerification

step "CRAP (<=15 per method)"
node scripts/crap-check.mjs

step "Halstead difficulty (<=20 per function)"
node scripts/check-halstead.mjs

step "duplication (jscpd, threshold 0)"
npx -y jscpd app/src

step "e2e feature coverage mapping"
node scripts/check-feature-coverage.mjs

printf '\nAll quality gates passed.\n'
