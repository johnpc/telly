#!/usr/bin/env node
// Keeps docs/state-machines.md honest, both ways:
//  1. every path it cites exists, every row has an owner under app/src/main
//     and a JVM test or e2e reference;
//  2. every `@StateMachine("name")` marker in app/src/main has a row of that
//     name, and every row with status `explicit` has a marker — so a machine
//     cannot be renamed, moved or quietly demoted without the catalog moving;
//  3. the fullscreen overlay has ONE writer: OverlayState mutators are only
//     called from PlaybackCommands.kt;
//  4. a new implicit machine is caught early: `consume*()` one-shots and
//     `var pending` fields may only live in files the catalog cites.
import { existsSync, readFileSync, readdirSync, statSync } from "node:fs";
import { dirname, join, relative } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
const doc = readFileSync(join(ROOT, "docs/state-machines.md"), "utf8");
const errors = [];

// ---- 1. catalog rows ---------------------------------------------------------
const cited = [...new Set([...doc.matchAll(/`((?:app|scripts|e2e)\/[^`\s]+\.(?:kt|mjs|feature))`/g)].map((m) => m[1]))];
for (const p of cited) if (!existsSync(join(ROOT, p))) errors.push(`cited path does not exist: ${p}`);

const rows = doc
  .split("\n")
  .filter((l) => l.startsWith("| ") && !l.startsWith("| ---") && !l.startsWith("| Machine"))
  .map((l) => {
    const cells = l.split(" | ").map((c) => c.replace(/^\| |\|$/g, "").trim());
    return { name: cells[0], owner: cells[2] ?? "", status: cells[3] ?? "", tests: cells[4] ?? "", raw: l };
  });
for (const r of rows) {
  if (!/`app\/src\/main\/[^`]+\.kt`/.test(r.owner)) errors.push(`row "${r.name}" has no owner under app/src/main`);
  if (!/`(?:app\/src\/test\/[^`]+\.kt|e2e\/[^`]+)`|e2e `@/.test(r.tests)) errors.push(`row "${r.name}" has no JVM test or e2e reference`);
  if (!["explicit", "guarded", "implicit"].includes(r.status)) errors.push(`row "${r.name}" has unknown status "${r.status}"`);
}

// ---- 2. markers <-> rows --------------------------------------------------------
function walk(dir, out = []) {
  for (const e of readdirSync(dir)) {
    const p = join(dir, e);
    if (statSync(p).isDirectory()) walk(p, out);
    else if (p.endsWith(".kt")) out.push(p);
  }
  return out;
}
const mainFiles = walk(join(ROOT, "app/src/main/java"));
const markers = new Map(); // name -> file
for (const f of mainFiles) {
  const src = readFileSync(f, "utf8");
  for (const m of src.matchAll(/@StateMachine\("([^"]+)"\)/g)) {
    const rel = relative(ROOT, f);
    if (markers.has(m[1])) errors.push(`marker "${m[1]}" appears twice: ${markers.get(m[1])} and ${rel}`);
    markers.set(m[1], rel);
  }
}
const rowNames = new Set(rows.map((r) => r.name));
for (const [name, file] of markers) if (!rowNames.has(name)) errors.push(`@StateMachine("${name}") in ${file} has no catalog row`);
for (const r of rows) if (r.status === "explicit" && !markers.has(r.name)) errors.push(`explicit row "${r.name}" has no @StateMachine marker`);

// ---- 3. one overlay writer ---------------------------------------------------------
const MUTATORS = /\boverlays\.(set|showAutoHiding|keepAlive)\(|\boverlays::(set|showAutoHiding|keepAlive)\b/;
for (const f of mainFiles) {
  const rel = relative(ROOT, f);
  if (rel.endsWith("features/playback/PlaybackCommands.kt") || rel.endsWith("features/playback/OverlayState.kt")) continue;
  readFileSync(f, "utf8").split("\n").forEach((line, i) => {
    if (MUTATORS.test(line)) errors.push(`overlay written outside PlaybackCommands: ${rel}:${i + 1}`);
  });
}

// ---- 4. new one-shots must be catalogued ----------------------------------------
// A parameterless consume*() or a `pending` field at CLASS level (a parser's
// local accumulator is not a mode; the catalog cites real owners).
const ONE_SHOT = /^\s{4}(?:private |internal )?(?:fun consume\w*\(\)|var pending\b)/;
for (const f of mainFiles) {
  const rel = relative(ROOT, f);
  if (cited.includes(rel)) continue;
  readFileSync(f, "utf8").split("\n").forEach((line, i) => {
    if (ONE_SHOT.test(line)) errors.push(`one-shot state outside the catalog (add a row or convert): ${rel}:${i + 1}`);
  });
}

if (errors.length) {
  console.error("state-machine catalog check failed:\n  " + errors.join("\n  "));
  process.exit(1);
}
console.log(`state-machine catalog OK: ${rows.length} machines, ${markers.size} markers, ${cited.length} paths verified`);
