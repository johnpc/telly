// Shared Kotlin-source helpers for the gate scripts.

import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join, relative } from 'node:path';

const ROOT = new URL('../..', import.meta.url).pathname;

export function* walkKotlinFiles(dir) {
  const abs = join(ROOT, dir);
  for (const name of readdirSync(abs)) {
    const path = join(abs, name);
    if (statSync(path).isDirectory()) {
      if (name === 'build' || name === 'generated') continue;
      yield* walkKotlinFiles(join(dir, name));
    } else if (name.endsWith('.kt')) {
      yield { path: relative(ROOT, path), text: readFileSync(path, 'utf8') };
    }
  }
}

// Replaces comments with whitespace and string/char literals with STRLIT so the
// tokenizer only ever sees code. Handles line/block (nested) comments, raw
// triple-quoted strings, and escapes.
export function stripCommentsAndStrings(text) {
  let out = '';
  let i = 0;
  while (i < text.length) {
    const two = text.slice(i, i + 2);
    if (two === '//') {
      while (i < text.length && text[i] !== '\n') i += 1;
    } else if (two === '/*') {
      let depth = 1;
      i += 2;
      while (i < text.length && depth > 0) {
        if (text.slice(i, i + 2) === '/*') { depth += 1; i += 2; continue; }
        if (text.slice(i, i + 2) === '*/') { depth -= 1; i += 2; continue; }
        if (text[i] === '\n') out += '\n';
        i += 1;
      }
    } else if (text.slice(i, i + 3) === '"""') {
      i += 3;
      while (i < text.length && text.slice(i, i + 3) !== '"""') i += 1;
      i += 3;
      out += ' STRLIT ';
    } else if (text[i] === '"' || text[i] === "'") {
      const quote = text[i];
      i += 1;
      while (i < text.length && text[i] !== quote) {
        i += text[i] === '\\' ? 2 : 1;
      }
      i += 1;
      out += ' STRLIT ';
    } else {
      out += text[i];
      i += 1;
    }
  }
  return out;
}

function matchBlock(text, openIndex, open = '{', close = '}') {
  let depth = 0;
  for (let i = openIndex; i < text.length; i += 1) {
    if (text[i] === open) depth += 1;
    else if (text[i] === close) {
      depth -= 1;
      if (depth === 0) return i;
    }
  }
  return text.length - 1;
}

// Yields { name, body } for `fun` declarations (block or expression bodies)
// and for lambda bodies assigned to vals/vars.
export function* extractFunctions(clean) {
  const funPattern = /\bfun\s+(?:<[^>]*>\s*)?(?:[A-Za-z_][\w.]*\.)?([A-Za-z_]\w*|`[^`]+`)\s*\(/g;
  for (const match of clean.matchAll(funPattern)) {
    const parenOpen = match.index + match[0].length - 1;
    const parenClose = matchBlock(clean, parenOpen, '(', ')');
    let i = parenClose + 1;
    while (i < clean.length && !'{=\n'.includes(clean[i])) i += 1;
    if (clean[i] === '{') {
      yield { name: match[1], body: clean.slice(i, matchBlock(clean, i) + 1) };
    } else if (clean[i] === '=') {
      const end = clean.indexOf('\n', i);
      yield { name: match[1], body: clean.slice(i + 1, end === -1 ? clean.length : end) };
    }
  }
  const lambdaPattern = /\b(?:val|var)\s+([A-Za-z_]\w*)[^=\n]*=\s*\{/g;
  for (const match of clean.matchAll(lambdaPattern)) {
    const braceOpen = match.index + match[0].length - 1;
    yield { name: match[1], body: clean.slice(braceOpen, matchBlock(clean, braceOpen) + 1) };
  }
}
