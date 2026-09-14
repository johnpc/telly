# Search first on-device verification — side-by-side punch list

Date: 2026-09-14 · telly search merge (`feat/search` × guide/settings shells)
vs TiviMate 5.2.0 references (screens 49–51 + uidumps/49–50) AND live
TiviMate driven read-only on the same fixture playlist (`tm-*` captures).
telly evidence: `search-smoke/` in this directory. Geometry in px on the
1920×1080 emulator (2 px = 1 dp).

## Functional results (all pass)

| # | Scenario | Result | Evidence |
| --- | --- | --- | --- |
| 1 | Quick-bar Search slot opens the search route; landing = orb + "Speak to search" bar + gear + Search history + trash | PASS | `00-quickbar.png`, `01-search-landing.png/.xml` |
| 2 | Typing "news" renders channel cards (6 News One channels, logo + name + airing title + progress) and programme rows with real fixture titles/air-times via ProgramTitle ("12:45 — 01:45 AM" today formats) | PASS | `02-query-news-results.png/.xml`, `03-results-no-keyboard.png` |
| 3 | Digits-only query "7" matches channel number 7 (Sports Arena) | PASS | `15-digits-query-channel-numbers.png` |
| 4 | Focused programme row shows the right-side detail card (title, time, description) | PASS | `05-program-row-focused-detail.png/.xml` |
| 5 | Programme OK → guide-cell dropdown (Remind / Record / Custom recording / Add to My list / Program description) → every row opens the shared Unlock Premium paywall → closes back to results | PASS | `06-program-dropdown.png/.xml`, `07-paywall.png`, `08-paywall-closed.png` |
| 6 | Voice orb OK → branded coming-soon placeholder | PASS | `09-voice-orb-coming-soon.png` |
| 7 | Channel OK → tunes fullscreen; BACK → guide (stack root, preview keeps playing); BACK → app exits (guide-root BACK chain) | PASS | `10-channel-ok-fullscreen.png`, `11-back-lands-guide.png`, `12-back-exits-app.png` + launcher `topResumedActivity` |
| 8 | History: committed on programme/channel OK + IME search, deduped ("news" once after two commits), newest first, survives app restart, entry OK re-runs the query, trash clears to "No history" | PASS | `13-history-persisted.png`, `14-history-entry-reruns.png`, `16-history-two-entries.png`, `17-trash-cleared.png` |
| 9 | Regression: quick-bar renders identically to round 4 (`round4/07-player-quickbar.png`); guide nav-rail gear still opens the settings right-sheet | PASS | `00-quickbar.png`, `18-guide-gear-settings-sheet.png` |

## P1 — FIXED during this pass

| Item | telly (before) | Fix |
| --- | --- | --- |
| D-pad focus trapped in the query field: Compose `BasicTextField` consumes DPAD_DOWN as a cursor move, so results were unreachable by remote | DOWN from the query bar kept focus in the EditText (uidump: `focused=true` on the field after DOWN) | `SearchScreenQueryField` hands DPAD_DOWN to `LocalFocusManager.moveFocus(Down)` via `onPreviewKeyEvent`; verified on-device (`04-channel-card-focused.png`) |

## P2 — logged (live-TiviMate deltas, same fixture data)

| Item | telly | TiviMate (live `tm-*`) |
| --- | --- | --- |
| Channel-result order | zap/number order (News One, HD, +1, Extra, 2, 24) — deliberate, documented in CLAUDE.md when the reference cap was "not capturable" | live shows name order (News One, +1, 2, 24, Extra, HD) (`tm-02`); one-line `ORDER BY` follow-up if we adopt it |
| Airing programme rows show progress + remaining | times only ("12:45 — 01:45 AM") | "12:45 — 01:45 AM ▬▬ 50 min" progress dash + minutes on airing rows and in the detail card (`tm-03`) |
| Airing programme title tint | white | light blue for the currently-airing row title (`tm-03`) |
| Focused voice orb color | white focus fill | accent blue focus fill (`tm-01`); resting light grey matches |
| Section header size | "Channels"/"Programs" 44 px tall (~22 sp), bounds [48,228][215,272] | 38 px (~19 sp), [48,232][165,270] (`tm-02` uidump) |
| System IME position | Gboard centered under the bar, covers mid-screen rows (`02`) | Gboard anchored bottom-right, left rows stay readable (`tm-02`); not obviously app-controllable |
| Detail card preselection while typing | detail card only renders once a programme row takes focus | first programme's detail card already visible while the keyboard is up (ref 50, `tm-03`) |

## P3 — logged

- Digits-only queries: live TiviMate finds NOTHING for "7" (`tm-04-digits-query.png`); telly's number-prefix match is a deliberate superset (catalogue §4 note kept).
- DOWN from the query bar lands on the nearest card under the bar's center (4th card), not the first card.
- Channel-card text inset: telly text starts x=72 (200 px wide), TiviMate x=64 (216 px); card subtitle font ~15 sp vs ~19 sp.
- Programme rows start x=376 vs TiviMate x=360.
- Live TiviMate collapsed the "news" programme list to 2 rows (one per title?) where the ref capture 50 showed duplicates across channels; telly lists per airing/channel like ref 50.
