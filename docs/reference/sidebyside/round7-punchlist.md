# Round 7 punch list — sheet motion + search two-pane device verification

Date: 2026-09-14 · Evidence in `round7/` (see its README for methods and frame
timings). Fixed items landed on main in this round; the rest are logged here.

## Fixed this round (device-verified after the fix)

| # | Item | Was | Now |
| --- | --- | --- | --- |
| 1 | Sheet backdrop scrim darkness (both hosts) | 72 % black (0xB8) — factor 0.28, chosen from round-6 screenrecord frames whose limited-range encode crushed the darks | 60 % black (0x99) — screencap factor 0.40–0.42, matching live TiviMate's 0.40–0.42 at the same probe points (`round7/02-*.png`, `05-*.png`) |
| 2 | Channel-options push/pop motion | single-frame swaps both ways | reference cross-fade: sheet fadeOut 170 ms under pane fadeIn 350 ms in place; pop fadeOut 140 ms straight to grid/panel (`PlaybackScreenMenuSurfaceSwitch`, both hosts; `round7/03-*.mp4`) |
| 3 | Search match rule | name/title **substring** (`LIKE %q%`) | **word-prefix** (`' ' \|\| column LIKE '% q%'`) — probes: "xtra"/"room" find nothing, "o"→"One", "spec"/"epis"→"Special"/"Episode" |
| 4 | OK on a Programs master card | tuned the channel (assumed Channels-shelf semantics) | opens the shared **Unlock Premium** screen like live 5.2.0 (`round7/09-*`); e2e scenario added |
| 5 | Selected-but-unfocused master card | uniform 42 % resting alpha, no marker | 1 dp grey outline (0xFF37393C, sampled) like the reference (`round7/06-tm-search-news.png`) |
| 6 | DOWN from the query bar with no channel matches | landed on master card 1 | lands on the **first airing row** like live 5.2.0 (`round7/` §C4); e2e scenario added |
| 7 | Detail-card title wrapped to 2 lines | theme 0.5 sp tracking | `letterSpacing = 0.sp` — same string now fits one line like the reference |
| 8 | tv-guide acceptance step crash (steps layer) | "at roughly the same time" with no prior anchor threw in `FixtureServer.nowProgramme` | `GuideSteps.anchorOrNow()` defaults to now on first use |

## Fixed after round 7 (JVM-verified; on-device confirmation pending)

| # | Item | Was | Now |
| --- | --- | --- | --- |
| 9 | DOWN-from-bar focus memory with a visited, scrolled shelf (P2, `round7/10-tm-down-scrolled-shelf-focus-memory.png`) | `moveDownFromBar` always requested card 1 | `SearchFocusMemory` in the ViewModel remembers the last results-area node that held D-pad focus (Channels card / Programs master card / airing row, identity-keyed); the query bar's DOWN tries the remembered node's `FocusRequester` first and falls back to the round-7-verified fresh landing (first card, else first airing row) when nothing was visited or the node left composition. A new result batch clears the memory (post-change behavior is uncaptured). e2e scenario added (existing steps only) |
| 10 | Originating guide row dimmed under the sheet (P2, `round7/02-*`) | fullscreen 0x99 scrim dimmed the whole grid uniformly | scrim cut-out: `GuideDimExemption.bandTopDp` maps the sheet's origin channel (persisted by `GuideFocusMemory` past the restore) to its CURRENT full-width 39 dp row band, and `GuideScreenMenuScrim` punches it out of the shared scrim (`BlendMode.Clear` over an offscreen layer), lingering through the scrim's ~300 ms fade-out. The white focused-cell outline stays visible at full brightness like the reference. Guide host only — the round-7 evidence covers the guide grid; the 5.2.0 free build has no separate panel host and telly's panel has no grid rows behind its sheet, so the panel keeps the uniform scrim |
| 11 | Programs lanes ~16 px high inside the section (P3) | two-pane content shared the shelf's 14 dp top inset | `SearchScreenDims.programsTop` = 22 dp (shelfTop + 8 dp) on the Programs two-pane row |
| 12 | Airing-row time line a font size up (P3, 256 vs 209 px) | shared `TellyScreenTimesLine` hardcoded 15 sp | `fontSize` parameter (default 15 sp, so overlay/panel/guide are untouched); `SearchScreenAirTime` passes its own size through — 13 sp on the rows, 14 sp on the detail card (now consistent with its bare-times branch) |

On-device follow-ups for the new fixes: confirm the restored-card behavior with a
physically scrolled shelf + the IME/focus interaction of the new search scenario;
eyeball the undimmed row band (full width incl. channel cell, no seam at the band
edges during the scrim fades); confirm the rows-lane/time-size pixels against tm.

## P2 — logged

- **Stale guide clock + black preview after a long background.** After ~10+ min
  backgrounded (task-switched to TiviMate), telly's guide header clock read 6:28 PM at
  ~6:40 and the preview window stayed black until process restart; fresh cold start
  is fine. Likely the guide's "now" ticker and the preview engine don't resume.

## P3 — logged (geometry nits, "news" side-by-side, `round7/06-*`)

- TiviMate quick-bar / IME positioning items from earlier rounds unchanged.
