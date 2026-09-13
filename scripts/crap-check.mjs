#!/usr/bin/env node
// Gate: per-method CRAP score from the JaCoCo XML report must be <= 15.
//   CRAP(m) = complexity(m)^2 * (1 - coverage(m))^3 + complexity(m)
// coverage(m) = covered / (covered + missed) using LINE counters at method level.
// Generated code is skipped: Compose singletons, synthetic $default/$lambda
// methods, BuildConfig, and databinding classes.

import { readFileSync } from 'node:fs';
import { join } from 'node:path';

const ROOT = new URL('..', import.meta.url).pathname;
const REPORT = join(
  ROOT, 'app', 'build', 'reports', 'jacoco', 'jacocoTestReport', 'jacocoTestReport.xml',
);
const MAX_CRAP = 15;

let xml;
try {
  xml = readFileSync(REPORT, 'utf8');
} catch {
  console.error(`FAIL crap-check: JaCoCo XML not found at ${REPORT}.`);
  console.error('Run: ./gradlew jacocoTestReport');
  process.exit(1);
}

const skipClass = (name) =>
  name.includes('ComposableSingletons') ||
  name.includes('BuildConfig') ||
  name.includes('databinding') ||
  name.includes('DataBinding');

const skipMethod = (name) =>
  name.includes('$default') || name.includes('$lambda') || name.startsWith('access$');

const attr = (tag, key) => (tag.match(new RegExp(`${key}="([^"]*)"`)) ?? [])[1];

const counters = (block, type) => {
  const m = block.match(new RegExp(`<counter type="${type}" missed="(\\d+)" covered="(\\d+)"`));
  return m ? { missed: Number(m[1]), covered: Number(m[2]) } : null;
};

const offenders = [];
let methodsChecked = 0;

for (const classBlock of xml.match(/<class [^>]*>[\s\S]*?<\/class>/g) ?? []) {
  const className = attr(classBlock, 'name') ?? '';
  if (skipClass(className)) continue;
  for (const methodBlock of classBlock.match(/<method [^>]*>[\s\S]*?<\/method>/g) ?? []) {
    const methodName = attr(methodBlock, 'name') ?? '';
    if (skipMethod(methodName)) continue;
    const complexityCounter = counters(methodBlock, 'COMPLEXITY');
    const lineCounter = counters(methodBlock, 'LINE');
    if (!complexityCounter) continue;
    const complexity = complexityCounter.missed + complexityCounter.covered;
    const lineTotal = lineCounter ? lineCounter.missed + lineCounter.covered : 0;
    const coverage = lineTotal === 0 ? 1 : lineCounter.covered / lineTotal;
    const crap = complexity ** 2 * (1 - coverage) ** 3 + complexity;
    methodsChecked += 1;
    if (crap > MAX_CRAP) {
      offenders.push({ className, methodName, complexity, coverage, crap });
    }
  }
}

if (offenders.length > 0) {
  console.error(`FAIL crap-check: ${offenders.length} method(s) exceed CRAP ${MAX_CRAP}.`);
  console.error('Lower complexity or add tests — fix the code, never the gate.');
  for (const o of offenders) {
    console.error(
      `  ${o.className}.${o.methodName}: CRAP=${o.crap.toFixed(1)} ` +
      `(complexity=${o.complexity}, line coverage=${(o.coverage * 100).toFixed(0)}%)`,
    );
  }
  process.exit(1);
}

console.log(`PASS crap-check: ${methodsChecked} method(s) all at CRAP <= ${MAX_CRAP}.`);
