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
| 9 | Stale guide clock + black preview after a long background (was P2 below) | guide "now" sampled once at controller build, never re-seeded; nothing stopped/re-tuned the player around STOP/START, so the backgrounded process froze with a paused stale stream and resumed black at the old clock | reference-verified resume semantics (`resume-fix/`): stream stops on ON_STOP (TiviMate abandons focus + codecs instantly), guide clock ticks per minute (`GuideNow`) and re-seeds on the START after a stop, guide re-tunes its preview, fullscreen leaves for the guide exactly like TiviMate's resume (90 s + 11 min probes, no zap overlay) |

## P2 — logged

- **DOWN-from-bar focus memory with a visited, scrolled shelf.** Reference: after
  focusing a card and scrolling the Channels shelf (query "h", RIGHT×8), UP then DOWN
  RESTORES the last-focused card with the shelf still scrolled
  (`round7/10-tm-down-scrolled-shelf-focus-memory.png`) — leanback focus memory.
  telly always requests card 1 (correct for the fresh-batch case, which is what
  round 6 measured). Needs a focus-memory pass (Compose `focusRestorer` or
  screen-held state), not a one-liner; behavior after the results change underneath
  the memory is still uncaptured.
- **The originating guide row is NOT dimmed under the sheet.** TiviMate's sheet
  backdrop dims everything EXCEPT the long-OK row (screencap: row-5 pixels identical
  rest vs sheet-open, all other rows ×0.40). telly's fullscreen scrim dims the whole
  grid uniformly. Needs a per-row dim (or scrim cut-out) in the grid layer.
- ~~**Stale guide clock + black preview after a long background.**~~ **FIXED**
  (row 9 above; evidence + reference resume observations in `round7/resume-fix/`).

## P3 — logged (geometry nits, "news" side-by-side, `round7/06-*`)

- telly's Programs rows lane sits ~16 px higher inside the section than the
  reference (rows title y629 vs 668 with headers 18 px apart).
- Airing-row time line renders wider than the reference (256 vs 209 px for the same
  16-char string) — the shared `TellyScreenTimesLine` range font is a size up from
  tm's ~13 sp; shared with the guide info pane, so left alone this round.
- TiviMate quick-bar / IME positioning items from earlier rounds unchanged.
