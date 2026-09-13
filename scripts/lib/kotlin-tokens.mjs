// Kotlin token classification shared by the Halstead gate.

export const KEYWORDS = new Set([
  'as', 'break', 'class', 'continue', 'do', 'else', 'for', 'fun', 'if', 'in',
  'interface', 'is', 'object', 'package', 'return', 'super', 'this', 'throw',
  'try', 'typealias', 'typeof', 'val', 'var', 'when', 'while', 'by', 'catch',
  'constructor', 'delegate', 'dynamic', 'field', 'file', 'finally', 'get',
  'import', 'init', 'param', 'property', 'receiver', 'set', 'setparam',
  'where', 'abstract', 'annotation', 'companion', 'const', 'crossinline',
  'data', 'enum', 'expect', 'external', 'final', 'infix', 'inline', 'inner',
  'internal', 'lateinit', 'noinline', 'open', 'operator', 'out', 'override',
  'private', 'protected', 'public', 'reified', 'sealed', 'suspend', 'tailrec',
  'vararg', 'it',
]);

// Multi-char symbols first so the tokenizer matches them greedily.
export const SYMBOL_LIST = [
  '?:', '?.', '!!', '==', '!=', '===', '!==', '<=', '>=', '&&', '||', '->',
  '..', '..<', '::', '+=', '-=', '*=', '/=', '%=', '++', '--', '=',
  '+', '-', '*', '/', '%', '<', '>', '!', '?', ':', ';', ',', '.', '(', ')',
  '{', '}', '[', ']', '@', '$',
];

export const SYMBOL_OPERATORS = new Set(SYMBOL_LIST);

const escaped = [...SYMBOL_LIST]
  .sort((a, b) => b.length - a.length)
  .map((s) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'))
  .join('|');

// Identifiers/keywords, numeric literals, or symbol operators.
export const TOKEN_PATTERN = new RegExp(
  `[A-Za-z_][A-Za-z0-9_]*|\\d[\\dxXbB_a-fA-F]*(?:\\.\\d+)?[fFlL]?|${escaped}`,
  'g',
);
