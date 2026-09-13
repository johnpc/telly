#!/usr/bin/env node
// Gate: every Kotlin LOGIC file under app/src/main must be <= 100 lines.
// Test files (app/src/test, app/src/androidTest) and generated code are exempt.

import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join, relative } from 'node:path';

const ROOT = new URL('..', import.meta.url).pathname;
const TARGET = join(ROOT, 'app', 'src', 'main');
const MAX_LINES = 100;

function walk(dir, out = []) {
  for (const name of readdirSync(dir)) {
    const path = join(dir, name);
    const stat = statSync(path);
    if (stat.isDirectory()) {
      if (name === 'build' || name === 'generated') continue;
      walk(path, out);
    } else if (name.endsWith('.kt')) {
      out.push(path);
    }
  }
  return out;
}

// Counts LOGIC lines only: comments (// line, /* block */, KDoc) and blank
// lines are excluded, so documentation never competes with the limit.
function countLogicLines(text) {
  const noBlockComments = text.replace(/\/\*[\s\S]*?\*\//g, (m) =>
    m.replace(/[^\n]/g, ''),
  );
  return noBlockComments
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line !== '' && !line.startsWith('//')).length;
}

const offenders = [];
for (const file of walk(TARGET)) {
  const lines = countLogicLines(readFileSync(file, 'utf8'));
  if (lines > MAX_LINES) {
    offenders.push({ file: relative(ROOT, file), lines });
  }
}

if (offenders.length > 0) {
  console.error(`FAIL check-file-lines: ${offenders.length} file(s) exceed ${MAX_LINES} lines.`);
  console.error('Split them into smaller units — fix the code, never the gate.');
  for (const { file, lines } of offenders) {
    console.error(`  ${file}: ${lines} lines`);
  }
  process.exit(1);
}

console.log(`PASS check-file-lines: all logic .kt files are <= ${MAX_LINES} lines.`);
