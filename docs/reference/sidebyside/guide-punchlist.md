# TV-guide first on-device verification — side-by-side punch list

Date: 2026-09-13 · telly `ea29fe0` (guide merge) vs TiviMate 5.2.0 references
(screens 24–27, 32 + uidumps/24). telly evidence: `guide-smoke/` in this
directory. Geometry cited in px on the 1920×1080 emulator (2 px = 1 dp).

## Functional results (all pass)

| # | Scenario | Result | Evidence |
| --- | --- | --- | --- |
| 1 | BACK at bare playback → guide: preview playing, info pane, timeline + now-line, channel column, real fixture titles via ProgramTitle, "No information" fillers | PASS | `01-back-to-guide.png/.xml` |
| 2 | RIGHT pans at edge; DOWN/UP keep the time anchor; LEFT at edge → groups; group switch renumbers from 1 | PASS | `02-right-x3.png`, `03-down-anchor.png`, `04-groups-open.png`, `05-group-news-renumber.png`, `06-group-sports-renumber.png` |
| 3 | Two-stage OK: airing cell on another channel tunes the preview (stage 1), 2nd OK goes fullscreen (guide pops) | PASS | `07-ok-stage1-preview-tune.png`, `08-ok-stage2-fullscreen.png` |
| 4 | Future-cell OK → 5-item premium dropdown (Remind / Record / Custom recording / Add to My list / Program description) → paywall opens and closes back to the grid | PASS | `09-future-cell-dropdown.png/.xml`, `10-paywall.png`, `11-paywall-closed.png` |
| 5 | BACK at guide root exits the app, no confirmation | PASS | `12-back-exits-app.png` + launcher `ResumedActivity` |

Colors sampled and matching the capture: screen bg `#131619`, cell
`#1B1E21`, focus pill `#DEE0E2`, clock blue `#90CAF9`, timeline label grey
`#A7A8A9`. Row pitch 78 px, 30 min = 320 px, dropdown anchored at the cell's
left edge — all match uidump 24 / capture 27.

## Visual deltas — ALL FIXED in round 2 (2026-09-13, evidence: `guide-round2/`)

Round-2 uidump measurements against uidump 24/25 unless noted. All ten
items below are FIXED; per-item resolution follows the table.

| Sev | Item | telly | TiviMate reference |
| --- | --- | --- | --- |
| P1 | Grid channel column too narrow — names truncate ("News One …", "Sports Are…") | cells start x=380 (190 dp); name text clipped at x=368 | uidump 24: names span x=214–510, cells start x=540 (270 dp) |
| P2 | Info-pane title smaller | title bounds [740,32][1381,72] → 40 px tall | [716,36][1812,90] → 54 px tall (capture 24) |
| P2 | Category/group label placement in the info pane | rendered inline right after "N min" | right-aligned at the pane's far right edge under the star (captures 24/26) |
| P2 | Grid focus while the groups column is open | grid cell keeps the full white focus pill (two white pills at once) | grid's cell drops to a dim grey "selected" pill; only the group row is white (capture 25) |
| P2 | Groups column vertical alignment | list pinned to the top (Favorites ≈ y=120) | list starts below the preview line (Favorites ≈ y=462, capture 25) |
| P3 | Past-cell dimming | cell background dimmed too (≈`#181A1D`) | background stays `#1B1E21`, only the text dims (capture 24) |
| P3 | Playing-channel ► marker position | immediately after the name text | right-aligned at the channel column edge, x≈510 (capture 32) |
| P3 | First-open hint toast ("Long OK: open menu / Left: show groups / Long Left: navigate to past programs") | absent | bottom-right toast (captures 24/26/27) |
| P3 | Channel-name font | 33 px bounds (~16 sp) | 38 px bounds (~19 sp) (uidump 24) |
| P3 | Dropdown metrics slightly off | 356 px wide, 80 px rows | 368 px wide, 78 px rows (capture 27) |
| P3 | Nav rail (search/DVR/bookmark/settings icons at far left of the guide+groups view) | absent | present (captures 16/17/25) — separate slice, not part of the guide grid |

### Round-2 resolutions

| Sev | Item | Fix | Round-2 evidence |
| --- | --- | --- | --- |
| P1 | Channel column 190→270 dp | `GuideGeometry.CHANNEL_COLUMN_DP=270`, `TIME_VIEWPORT_DP=690`; column internals re-pinned (number [32,88], logo [104,194], name x=214) | `01-guide.png`; uidump: name `News One` [214,488], cells start x=540 — exact match to uidump 24 |
| P2 | Info-pane title size | guide pane titles 23 sp via `TellyScreenProgramTitle(fontSize)` (overlay/panel keep 17 sp) | uidump: title [716,37][1520,91] → 54 px tall = ref [716,36][1812,90] |
| P2 | Group label placement | times row is full-width; group name right-aligned 16 sp white | uidump: `News` right edge x=1872 = ref |
| P2 | Double focus pill with groups open | grid drops to grey `TELLY_GUIDE_CELL_SELECTED` #424547 while `layer == Groups` | `02-groups.png` — one white pill (groups row) |
| P2 | Groups column vertical alignment | guide passes `GroupColumnMetrics` (252 dp panel, 36 dp pills, 2.5 dp gap, 18 sp, top 213 dp) | uidump: Favorites [176,441], pitch 77 px = ref 25 (440/517/594/…) within 1 px |
| P3 | Past-cell dimming | past cells keep `#1B1E21` fill; only text dims (35% white) | `GuideScreenCell` (visual spot-check `01-guide.png`) |
| P3 | ► marker position | right-aligned at the column edge (name takes `weight(1)`); end padding 10 dp | `04-group-news.png` row 2, marker ≈ x=510-520 |
| P3 | First-open hint toast | `GuideHint` (store-once flag + 15 s auto-hide) + `GuideScreenHintToast`, light #B5B7B9, bold key names, bottom-right | `01-guide.png`; box [1314,901][1887,1047] vs ref [1366,896][1888,1048] |
| P3 | Channel-name font | name 16 sp (number 17 sp to match ref bounds) | uidump: name 38 px tall, number [32,487][88,527] = ref |
| P3 | Dropdown metrics | `MENU_WIDTH_DP=184`, rows 39 dp | `03-dropdown.png`; uidump rows pitch 78 px, width 368 px |

Also fixed while in there (needed for the 270 dp column to match): timeline
labels are now centered on their half-hour tick in 160 dp slots (uidump 24:
`02:30 PM` spans [380,700] straddling the x=540 tick; telly used to
left-align labels at the tick). Round-2 uidump: `10:00 PM` [380,420][700,450].

Residual (accepted): with groups open TiviMate keeps the guide pane at full
width and lets the screen edge clip it, so its group label ("News") stays
readable at x=1797; telly compresses the pane into the remaining width, so
the right-aligned group label clips at x=1872. Revisit only if a later
slice needs the overflow-pane model.

Playback-overlay regression check after the shared-text changes
(`TellyScreenProgramTitle`/`TellyScreenTimesLine`/`TellyScreenWhiteText`
gained parameters, defaults unchanged): `05-overlay-regression.png` —
identical to round4.

## Round-4 playback regression sweep post-merge (all match round4 evidence)

| Surface | Result | Evidence (vs `round4/`) |
| --- | --- | --- |
| Info overlay (OK) | identical | `r4-01-info-overlay.png` vs `round4/02-info-overlay.png` |
| Transport row (2nd UP) | identical | `r4-02-transport-row.png` vs `round4/03b-transport-row.png` |
| Quick-bar (MENU/long-OK) | identical | `r4-03-quickbar.png` vs `round4/07-player-quickbar.png` |
| Zap overlay (CH+) | identical | `r4-04-zap-overlay.png` vs `round4/10-zap-2-zap-overlay.png` |
| Panel + inline detail (quick-bar → Channels list) | identical | `r4-05-panel.png` vs `round4/04-panel-focused.png` |
| Panel row long-OK 4-section sheet | identical | `r4-06-panel-longok.png` vs `round4/05-panel-row-longok.png` |

BACK-chain change verified: bare-playback BACK now lands on the guide (was
panel pre-guide); panel opens via the quick-bar's Channels list; BACK at
guide root exits the app.
