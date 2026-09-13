#!/usr/bin/env node
// Gate: every Gherkin feature file under e2e/features/ must be mapped in the
// acceptance-job matrix of .github/workflows/ci.yml. Passes trivially while no
// .feature files exist.

import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import { basename, join } from 'node:path';

const ROOT = new URL('..', import.meta.url).pathname;
const FEATURES_DIR = join(ROOT, 'e2e', 'features');
const CI_FILE = join(ROOT, '.github', 'workflows', 'ci.yml');

function walk(dir, out = []) {
  for (const name of readdirSync(dir)) {
    const path = join(dir, name);
    if (statSync(path).isDirectory()) walk(path, out);
    else if (name.endsWith('.feature')) out.push(path);
  }
  return out;
}

if (!existsSync(FEATURES_DIR)) {
  console.log('PASS check-feature-coverage: no e2e/features directory yet — trivially green.');
  process.exit(0);
}

const features = walk(FEATURES_DIR);
if (features.length === 0) {
  console.log('PASS check-feature-coverage: no .feature files yet — trivially green.');
  process.exit(0);
}

const ci = readFileSync(CI_FILE, 'utf8');
const unmapped = features.filter((f) => !ci.includes(basename(f, '.feature')));

if (unmapped.length > 0) {
  console.error(`FAIL check-feature-coverage: ${unmapped.length} feature(s) missing from the ci.yml acceptance matrix.`);
  for (const f of unmapped) console.error(`  ${f}`);
  process.exit(1);
}

console.log(`PASS check-feature-coverage: all ${features.length} feature(s) mapped in ci.yml.`);
