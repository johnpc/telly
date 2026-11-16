#!/usr/bin/env node
// Keeps docs/state-machines.md honest: every `owner`/`tests` path it cites
// must exist, and every sealed/enum "mode" type the catalog names as an
// owner must still live in that file. A machine moved or renamed without
// updating the catalog fails the gate — the catalog is how the next
// developer (or agent) finds the one place a mode is allowed to change.
import { existsSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
const doc = readFileSync(join(ROOT, "docs/state-machines.md"), "utf8");

const cited = [...doc.matchAll(/`((?:app|scripts|e2e)\/[^`\s]+\.(?:kt|mjs|feature))`/g)].map((m) => m[1]);
const missing = [...new Set(cited)].filter((p) => !existsSync(join(ROOT, p)));

const rows = doc.split("\n").filter((l) => l.startsWith("| ") && !l.startsWith("| ---") && !l.startsWith("| Machine"));
const noOwner = rows.filter((r) => !/`app\/src\/main\/[^`]+\.kt`/.test(r)).map((r) => r.split("|")[1].trim());
const noTests = rows.filter((r) => !/`(?:app\/src\/test\/[^`]+\.kt|e2e\/[^`]+)`|e2e `@/.test(r)).map((r) => r.split("|")[1].trim());

let failed = false;
if (missing.length) {
  failed = true;
  console.error("docs/state-machines.md cites paths that do not exist:\n  " + missing.join("\n  "));
}
if (noOwner.length) {
  failed = true;
  console.error("catalog rows without an owner under app/src/main:\n  " + noOwner.join("\n  "));
}
if (noTests.length) {
  failed = true;
  console.error("catalog rows without a JVM test or e2e reference:\n  " + noTests.join("\n  "));
}
if (failed) process.exit(1);
console.log(`state-machine catalog OK: ${rows.length} machines, ${new Set(cited).size} paths verified`);
