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

## P2 — FIXED (fidelity pass, branch `fix/search-p2-fidelity`)

| Item | Was (telly) | Fix |
| --- | --- | --- |
| Channel-result order | zap/number order (News One, HD, +1, Extra, 2, 24) | `SearchDao.channels` now `ORDER BY name COLLATE NOCASE, number` — matches live name order (News One, +1, 2, 24, Extra, HD, `tm-02`); CLAUDE.md search decision updated; DAO test pins the family ordering |
| Airing programme rows show progress + remaining | times only ("12:45 — 01:45 AM") | `SearchResultsBuilder` computes `progressPermille` + `"N min"` remaining (shared `ProgramTimes`) for airing hits; rows and the detail card render them via the shared `TellyScreenTimesLine` dash ("12:45 — 01:45 AM ▬▬ 50 min", `tm-03`); builder unit tests |
| Airing programme title tint | white | airing row titles tint `TELLY_CLOCK_BLUE` light blue (`tm-03`); upcoming rows keep the focus-aware default |
| Focused voice orb color | white focus fill | orb focus/press fill = `LocalAccentColor` (accent blue, `tm-01`); resting light grey unchanged |
| Section header size | 44 px tall (~22 sp box), bounds [48,228][215,272] | still 19 sp but the line box is trimmed (`includeFontPadding=false`, lineHeight 19 sp, trim Both) → 38 px box like `tm-02` |
| Detail card preselection while typing | detail card only renders once a programme row takes focus | every result batch preselects its first programme into `focusedProgram` (state only) — detail card shows while the IME is up, D-pad focus stays in the query field (ffbdfa1 DOWN-escape untouched); ViewModel unit test |

## P2 — logged (won't fix here)

| Item | telly | TiviMate (live `tm-*`) |
| --- | --- | --- |
| System IME position | Gboard centered under the bar, covers mid-screen rows (`02`) | Gboard anchored bottom-right, left rows stay readable (`tm-02`). WON'T FIX in-app: the system IME's window position is owned by the keyboard app/framework, not app-controllable (no public API to anchor Gboard bottom-right) |

## P3 — logged

- Digits-only queries: live TiviMate finds NOTHING for "7" (`tm-04-digits-query.png`); telly's number-prefix match is a deliberate superset (catalogue §4 note kept).
- DOWN from the query bar lands on the nearest card under the bar's center (4th card), not the first card. (Left as logged: Compose one-dimensional `moveFocus(Down)` picks the geometrically nearest candidate; forcing card #1 would need a focus-restorer hack and the reference behavior on this exact key isn't captured.)
  **ANSWERED (round6): the reference lands on the FIRST channel card** ("News One", leftmost, bounds [32,298][312,506]) — not the nearest card under the bar's center (`ref-round6/07-search-down-from-querybar.png/.xml`). telly's 4th-card landing is a confirmed deviation for the fix round.
- FIXED: channel-card text inset 72→64 px (`cardPad` 12→8 dp, content now 216 px wide) and card subtitle 15→19 sp — same pass as the P2 fixes.
- FIXED: programme rows now start at x=360 px (`rowTextStart` 36→28 dp; 8 dp pill pad included).
- Live TiviMate collapsed the "news" programme list to 2 rows (one per title?) where the ref capture 50 showed duplicates across channels; telly lists per airing/channel like ref 50. (Unverified grouping rule — needs another live probe before adopting.)
  **ANSWERED (round6): the Programs section is a channel-master / airings-detail two-pane, one row per AIRING, no title dedupe.** Left lane = one card per matching channel in case-insensitive NAME order; right lane = ONLY the selected card's airings, chronological, repeats included (News One shows "Newsroom Live: Newsroom Live Special" twice; verified 1:1 against fixture EPG ground truth for 4 channels). Same-titled airings are never merged across channels. DOWN stops at the selected channel's last row; LEFT+DOWN selects the next channel card, which swaps the rows pane and preselects its first row into the detail card. The old "2 rows" observation was one selected channel's 2 airings, NOT a per-title collapse. telly's flat soonest-first list with per-run shared cards is a structural mismatch (`ref-round6/06-*`, `08-programs-card-*`, README §D).
