# Round 7 — on-device verification of the sheet-motion + search two-pane merges

Date: 2026-09-14 · telly main (`e983ffe` + round-7 fixes) vs live TiviMate 5.2.0 on the
tv34 AVD (1920×1080, 2 px = 1 dp), both on the regenerated fixture backend
(`e2e/fixtures/` on :8090). Method: `adb shell screenrecord` frame-stepped with
`ffmpeg -vsync 0` + per-frame `ffprobe` PTS; pixel probes with PIL; **scrim darkness via
`screencap` (full-range RGB), not screenrecord frames** — that difference re-dated a
round-6 number (below).

## A. Acceptance legs (gate)

- `search` 12/12, `tv-guide` 16/16, `channel-panel` 13/13 — all green on the final
  round-7 build (`scripts/ci-acceptance.sh` per leg).
- One steps-layer fix: the new "Channel options replaces the sheet…" tv-guide scenario
  called "the focused cell is on channel 1 at roughly the same time" without any prior
  step recording a time anchor → `GuideSteps.anchorMs` was 0 and
  `FixtureServer.nowProgramme` threw. `anchorOrNow()` now defaults the anchor to now on
  first use (test layer, not app).

## B. Sheet motion side-by-side

### B1/B2 — entrance + close (01-*.mp4)

- telly entrance: last sheet-less frame t=3.496, settled t≈3.746 — **~250 ms**, grid
  dim lands with the sheet (within a frame). TiviMate take: sheet pops between emitted
  frames 5.862→6.201 with the dim landing ≤1 frame later — same class as round 6
  (~150–280 ms, only-on-change frame emission).
- telly close: sheet region drops from settled to gone in **one frame**
  (7.415→7.445), then the scrim fades back 7.445→7.763 ≈ **318 ms** (target 300).
  TiviMate: sheet cut 8.288→8.405, dim gone by 8.492. **MATCH.**

### B3 — scrim darkness (02-*.png) — round-6 number CORRECTED

- Screencap probes (multiplication factor rest→sheet, same coordinates):
  TiviMate guide **0.40–0.42** at every probe ((66,68,71)→(26,27,28),
  (173,174,175)→(69,70,70), …). Round 6's "(15,20,22)→(4,6,6) ≈ 72 % black" came from
  screenrecord frames, whose limited-range video encode crushes darks — the honest dim
  is **≈60 % black**.
- telly before: factor 0.27–0.29 (72 % scrim) — too dark. **FIXED:** `MENU_SCRIM`
  0xB8→0x99000000 (matches the settings sheet's existing 0x99). After: telly factors
  **0.40–0.42** at the same probes.
- Panel host (05-*.png): telly's sheet-over-panel backdrop now also measures **0.40**
  everywhere. The 5.2.0 free build has no separate panel host (its "panel" IS the
  guide), so the guide's 0.40 is the reference for both telly hosts.
- Logged (round7 punch list): TiviMate does NOT dim the originating row — the focused
  guide row keeps full brightness + white outline under the sheet; telly dims the whole
  grid uniformly.

### B4 — Channel-options push/pop (03-*.mp4)

- TiviMate push (fresh take): focused-pill fade-out ~85–100 ms emitted (round 6 saw
  ~170 ms; only-on-change emission makes short fades take-dependent), pane fade-in
  ~450 ms visible (round 6: ~350 ms); pop ~130 ms fade landing **directly on the guide
  grid** with the originating row focused ([0,624][1920,702] = row 3, the long-OK row).
- telly before: push and pop were **single-frame cuts** — visibly wrong next to the
  reference cross-fade. **FIXED:** shared `PlaybackScreenMenuSurfaceSwitch`
  (`AnimatedContent`, contentKey = menu surface) in `PlaybackScreenMenuMotion.kt`, used
  by both hosts (`GuideScreen`, `PlaybackScreenOverlays`→`PlaybackScreenMenuLayers`):
  sheet→pane = fadeOut(170 ms) under fadeIn(350 ms), pane→host = fadeOut(140 ms);
  everything else stays an instant swap (sheet enter keeps its internal 250 ms
  fade+slide, sheet close stays a cut).
- telly after (03-telly-…-crossfade-fixed.mp4): pill fade 2.560→2.666 (~110 ms
  visible of the 170 tween), pane ramp 2.576→2.807 (~230 ms visible of 350); pop
  5.590→5.675 (~90–140 ms) → grid with the originating row-5/row-1 cell focused
  (04-*.png). Panel path: pop lands on the panel with the row focused (05-*.png).

### B5 — regressions

- Fresh sheet opens land on Search (uidump: focused pill = Search row) — PASS.
- Below-fold restore receives real D-pad focus: Manage Favorites (row 13) → paywall →
  BACK → Manage Favorites focused; DOWN moves to Manage blocking — PASS.

## C. Search Programs two-pane side-by-side ("news", 06-*.png/.xml)

### C1 — geometry

- Master cards: name pitch 206 px both (tm 788→994, telly 753→959); card content
  x=64/104 class matches; airing-row pitch **119–120 px both**; row titles at
  **x=360 both**; detail card x=1240..1840 both. Section gap Channels→Programs header:
  tm 84 px vs telly 86 px. P3 nits logged: telly's rows lane sits ~16 px higher inside
  the section, and the airing-row time line renders slightly wider than tm's
  (256 vs 209 px for the same string; shared `TellyScreenTimesLine` sizing).
- Detail-card title: reference fits "Newsroom Live: Episode 8. S1 E8" on ONE line in
  the same 600 px box; telly wrapped to two — the theme's 0.5 sp tracking. **FIXED**
  (`letterSpacing = 0.sp` on the detail title).

### C2 — master-card OK (09-*.png/.xml) — assumption FLIPPED

- Live 5.2.0: OK on a Programs master card opens the **Unlock Premium screen** — it
  does NOT tune (BACK returns to search, card still focused). telly's "tunes like the
  Channels shelf" assumption was wrong. **FIXED:**
  `SearchViewModel.onProgramChannelResult()` → shared paywall; e2e scenario added.

### C3 — selected-but-unfocused master card (06-tm-search-news.png)

- The reference outlines the selected card (grey rounded border, edge pixels
  (55,57,60); the +1 card below has none). **FIXED:** `restingOutline` on
  `SearchScreenFocusRow` (1 dp, `Dims.selectedCardBorder` = 0xFF37393C, drawn outside
  the 42 % resting-alpha layer so it lands at the sampled value).

### C4 — DOWN from the query bar (10-*.png)

- Fresh query, Channels shelf present: reference lands on the FIRST card
  ([32,298][312,506]) — telly matches ([48,289][296,492]). PASS.
- Fresh query, NO channel matches ("newsroom"): reference lands on the **first AIRING
  ROW** ([336,310][1160,421]), not the master card. telly targeted the master card —
  **FIXED** (firstFocus requester moved to the rows pane's first row; e2e scenario
  added).
- Scrolled shelf after visiting a card (query "h", RIGHT×8 to News One HD, UP, DOWN):
  the reference **restores the last-focused card** ([1344,298][1624,506], shelf still
  scrolled) — leanback focus memory, NOT "always card 1". telly always requests card
  1. Logged as P2 (needs a focus-memory pass, not a blind fix).

### C5 — selection vs further typing (08-*.png)

- With News One +1 selected, returning to the bar and typing "room" ("newsroom"): the
  reference RESETS the selection to the first master card (border on News One, rows =
  News One's airings). telly's reset-to-first-per-batch **matches** — no change.

### C6 — regressions (06-telly-*.png)

- Detail card pre-renders while the IME is up; airing rows show dash + "N min"; airing
  titles tint light blue; channel & master lanes in case-insensitive name order;
  DOWN in rows stops at the last airing. All PASS.

### Bonus — match rule is WORD-PREFIX, not substring (probes)

- "xtra" → Nothing found (never matches inside "Extra"); "room" → Nothing found
  (never inside "Newsroom"); "o" → only the News One family ("One"); "spec"/"epis" →
  "…Special"/"Episode…" (later words match). Same rule for channels and programmes.
  **FIXED:** `SearchQuery.nameLike` = `"% q%"` against `' ' || column` in `SearchDao`
  (word-anchored LIKE); unit + DAO tests pin the mid-word negatives.

## Other observations (logged, not fixed)

- After telly sat backgrounded for ~10+ min (task-switching to TiviMate), the guide
  header clock was stale (6:28 PM at 6:40) and the preview window black until process
  restart; fresh cold start is fine. Round-7 punch list P2.
