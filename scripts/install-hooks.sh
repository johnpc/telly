#!/usr/bin/env bash
# Point git at the tracked hooks directory so every clone runs the quality
# gate before committing. Run once after cloning: ./scripts/install-hooks.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

git config core.hooksPath .hooks
chmod +x .hooks/* scripts/*.sh
echo "Installed: core.hooksPath -> .hooks (pre-commit runs scripts/quality.sh)"
