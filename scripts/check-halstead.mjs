#!/usr/bin/env node
// Gate: Halstead difficulty D = (n1 / 2) * (N2 / n2) per function body must be <= 20.
// n1 = distinct operators, n2 = distinct operands, N2 = total operands.
// Operators: Kotlin keywords + symbol operators. Operands: identifiers + literals.
// Functions: `fun` declarations plus lambda bodies assigned to vals. Tests exempt.

import { walkKotlinFiles, stripCommentsAndStrings, extractFunctions } from './lib/kotlin-source.mjs';
import { KEYWORDS, TOKEN_PATTERN, SYMBOL_OPERATORS } from './lib/kotlin-tokens.mjs';

const MAX_DIFFICULTY = 20;

function difficulty(body) {
  const operators = new Map();
  const operands = new Map();
  for (const match of body.matchAll(TOKEN_PATTERN)) {
    const token = match[0];
    if (KEYWORDS.has(token) || SYMBOL_OPERATORS.has(token)) {
      operators.set(token, (operators.get(token) ?? 0) + 1);
    } else {
      operands.set(token, (operands.get(token) ?? 0) + 1);
    }
  }
  const n1 = operators.size;
  const n2 = operands.size;
  const totalOperands = [...operands.values()].reduce((a, b) => a + b, 0);
  if (n2 === 0) return 0;
  return (n1 / 2) * (totalOperands / n2);
}

const offenders = [];
let functionsChecked = 0;

for (const { path, text } of walkKotlinFiles('app/src/main')) {
  if (path.endsWith('Test.kt')) continue; // test files exempt
  const clean = stripCommentsAndStrings(text);
  for (const fn of extractFunctions(clean)) {
    const d = difficulty(fn.body);
    functionsChecked += 1;
    if (d > MAX_DIFFICULTY) {
      offenders.push({ path, name: fn.name, d });
    }
  }
}

if (offenders.length > 0) {
  console.error(`FAIL check-halstead: ${offenders.length} function(s) exceed D=${MAX_DIFFICULTY}.`);
  console.error('Reduce vocabulary/repetition in these bodies — fix the code, never the gate.');
  for (const o of offenders) {
    console.error(`  ${o.path} :: ${o.name}: D=${o.d.toFixed(1)}`);
  }
  process.exit(1);
}

console.log(`PASS check-halstead: ${functionsChecked} function(s) all at D <= ${MAX_DIFFICULTY}.`);
