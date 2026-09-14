# Visual eyeball round 5 (on-device, verified locally)

Date: 2026-09-14 · telly (main, post-panel-sheet-routing) on the tv34 API-34
emulator vs TiviMate 5.2.0 references (`search-smoke/tm-*`, `round4/`,
`round3-ref/`). CI is down — every capture and gate here is local. telly
evidence: `visual-round5/*.png` + `*.xml`. Geometry in px on the 1920×1080
emulator (2 px = 1 dp). Colors sampled with PIL.

## Search

The prior "P2 fidelity" pass (commits 5261958/0a3772c) set the header to
19 sp white, the card name to 19 sp, and left every result at full opacity.
Re-measuring against the **no-keyboard** reference `tm-03` (the `tm-02`
keyboard capture is dimmed by the IME scrim and gave a misleading read last
round) showed the whole results surface renders at a uniform **42% alpha**,
and the section header is a small muted label, not a big white one.

| Item | Reference (tm-03) | telly before | Fix | Evidence |
| --- | --- | --- | --- | --- |
| Section header ("Channels"/"Programs"/"Search history") | 21 px glyph band in a 38 px box, muted white ≈`#717375` (42% of `#F5F7FA` over bg) | 19 sp, full white — 30 px glyph band | 14 sp in a 19 sp trimmed box, color = `TELLY_TEXT_PRIMARY` × 0.42; measured 22 px band, `#727578` — matches ref | `ty-search-results-fixed.png` |
| Resting result alpha (cards, program rows, logos, titles, times, airing blue, detail card) | uniform 42% (all tm-03 samples = full color × 0.42 over `#131619`) | full opacity | `SearchScreenFocusRow(dimWhenResting=true)` on channel cards + program rows; program channel-card column, and the non-focusable detail card, wrap in `alpha(0.42)`; focus restores full opacity | `ty-search-results-fixed.png` |
| Channel-shelf name order | News One → +1 → 2 → 24 → Extra → HD | already correct (name COLLATE NOCASE) | none | `ty-search-results.xml` |
| Card name size | 22 px glyph band (~14 sp) | 19 sp (29 px) | 14 sp | ↑ |
| Program row title / air-time size | title 30 px band, time 19 px | 17 sp / 14 sp | 16 sp / 13 sp | ↑ |
| Airing-title light-blue | `#90CAF9` × 0.42 over bg ≈`(72,98,119)`, matches ref `(69,94,115)` | `#90CAF9` full | kept `TELLY_CLOCK_BLUE`, now under the 0.42 row layer → lands on the ref blue by construction | ↑ |
| Orb focus fill = accent + white mic | accent circle, white glyph | already correct | none (confirmed `ty-search-orb-focused.png`) | accent fill, white mic |
| Card text x=64, rows x=360 | 64 / 360 px | already correct (`cardPad` 8, `rowTextStart` 28) | none (`ty-search-results.xml`: card name [64,…], rows [556,…] pill start 360 after 8 dp pad) | ↑ |
| Detail card pre-rendered while IME up | yes | already correct | none | `ty-search-results-ime.png` |
| DOWN escapes the query field | yes | already correct (ffbdfa1) | none | `ty-search-results.xml` shows a card focused after DOWN |
| "Search history" header x-position | x=384 (`tm-01` bounds [384,248]) | x=360 (`historyStart = orb+gap` = 178 dp) | `historyStart = 192.dp` → x=384 | `ty-search-orb-focused.png` header at [384,…] |

## Guide sheet / groups

| Item | Reference | telly | Verdict / fix |
| --- | --- | --- | --- |
| Header wraps with long programme titles | wraps | wraps to 2 lines ("Global Update: Global Update Special", "Newsroom Live: Newsroom Live Special") | MATCH (`ty-sheet-long-title.png/.xml`) |
| Below-fold "All channels" section scrolls | scrolls | scrolls to Manage Favorites / Manage blocking / Reorder / Copy / Create group / Group options | MATCH (`ty-sheet-belowfold.png`) |
| Focus re-lands on originating row after BACK | **ANSWERED (round6): reference RESTORES the originating row** — sheet opened from the 5th visible row (ch 5, News One 2, [0,702][1920,780]); after BACK that same row is focused=true (`ref-round6/04-row5-*.xml`; the row also keeps a white outline while the sheet is open) | **after BACK from the sheet, focus returns to the guide grid's now-cell** (grid has no per-row focusable — the key anchor re-grabs). | Logged — no cheap fix (grid focus is a single anchor by design; row-level restore would need the guide focus engine to persist and re-apply, out of scope) — now a CONFIRMED deviation with reference evidence |
| BACK from Channel options returns to the "Channel options" row | **ANSWERED (round6): N/A — the reference pane REPLACES the sheet** (cross-fade in place, sheet fades out on push); one BACK from Channel options lands directly on the guide grid with the originating row focused, never back on the sheet (`ref-round6/03-after-back-from-channel-options.png/.xml`, `03-channel-options-push-pop.mp4`) | **telly resets sheet focus to the top (Search row)** after popping ChannelOptions → RowMenu | Logged — telly's one-level pop (pane → sheet) is itself a structural deviation; reference dismisses the sheet on push. Fix round should decide whether to adopt replace-semantics or keep the stack |
| "Program description" full-screen message readability | readable | readable full-screen `OnboardingScreenMessage` (title + synopsis) | MATCH (`ty-program-description.png`) |

## History

| Item | Reference | telly | Verdict |
| --- | --- | --- | --- |
| Card press → guide with History group leading, renumbered from 1 | yes | History leads the groups column, rows renumbered 1..N newest-first | MATCH (`ty-history-guide.png`, `ty-history-groups.xml`: column = History, Favorites, All channels, …) |
| Watch order newest-first after zapping | newest first | 2 entries, newest (News One) at row 1 | MATCH (`ty-history-guide.png`) |
| BACK exits app from History guide root | exits | `topResumedActivity` → `com.google.android.tvlauncher` after one BACK from grid root | MATCH (verified via dumpsys) |
| History group ABSENT from the channel panel's column | absent | panel groups column shows no History (only playback panel; History is guide-only) | MATCH (`ty-panel-groups.png`) |

## Settings / PIN

| Item | Reference | telly | Verdict |
| --- | --- | --- | --- |
| BACK-escape fix (9e549e0) didn't change visuals | — | settings sheet + guided steps render unchanged; settings acceptance leg green | MATCH |
| New PIN wheel vs reference PIN entry | reference uses a numeric entry | `SettingsScreenPinWheel.kt` present from the harness merge | **ANSWERED (round6): the reference PIN entry UI is NOT capturable** — in 5.2.0 free, every Parental-controls row is premium-locked AND skipped by focus (the Off toggle can never be reached, so no PIN screen exists to shoot; `ref-round6/09-parental-*.png/.xml`). Only anchor: locked row "PIN input method: **Picker**" (default). telly's wheel is consistent with "Picker"; a pixel diff needs a premium account. |

## Fixed this round

1. **Search resting alpha + header size/color** (all the search rows above).
   Rebuilt, reinstalled, eyeballed (`ty-search-results-fixed.png`): header
   now `#727578` at a 22 px band; whole surface at 42%. Search acceptance
   leg green.
2. **`historyStart` 178 dp → 192 dp** so the landing "Search history" header
   sits at x=384 like `tm-01`.
3. **RIGHT no longer dead-ends in the guide groups column.** The groups
   column had `GuideKeyPolicy` mapping RIGHT → CloseLayer, but the key never
   reached the controller (the groups list owns Compose focus and there is no
   focusable to the right, so the focus search swallowed RIGHT). Added an
   `onPreviewKeyEvent` on the groups `Box` that routes DPAD_RIGHT to
   `controller.onKey(GuideKey.RIGHT)`. Verified on-device: RIGHT closes the
   column back to the grid now-cell (`/tmp` g4→g5). tv-guide acceptance leg
   green (15/15 on rerun; one prior run had an unrelated wizard-restart flake).

## Logged (not fixed)

- Guide sheet BACK focus restore (originating row / Channel-options row) —
  needs sheet focus memory + grid focus-engine persistence; no captured
  reference frame to pin the target; out of scope for a cheap fix.
  **ANSWERED (round6):** targets now pinned — sheet BACK must restore the
  originating row; Channel options must REPLACE the sheet (see updated table
  rows + `ref-round6/README.md` §A/§B).
- PIN wheel visual diff — needs a dedicated PIN pass with a reference capture.
  **ANSWERED (round6):** no reference capture is possible on the free build
  (section fully premium-locked and unfocusable); see `ref-round6/README.md` §C.
- Playback-sheet routing: NOT touched (another agent is aligning
  `PlaybackMenuHandler`). Observation only: the guide long-OK sheet and the
  playback quick-bar "Channels list" both route through
  `PlaybackScreenMenu` / the guide `GuideMenuController`; the sheet's premium
  rows correctly open the shared Unlock Premium screen (`ty-quickbar.xml`
  shows the full sheet with Open-in-external-player / Record / Custom
  recording / Add to My list / Program description + channel section).
