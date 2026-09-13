# Welcome + Add-playlist wizard (M3U path) — side-by-side punch list

Build under test: commit `fcb06d0` (`feat(onboarding): welcome screen and add-playlist wizard`),
assembled from an isolated worktree, installed on emulator-5554 (1920x1080, xhdpi, 2 px = 1 dp).
Reference: official TiviMate captures in `docs/reference/screens/02…12` + uidumps.
Fixture: `http://10.0.2.2:8090/playlist.m3u` (verified reachable from inside the emulator via
`nc` — HTTP 200, 5887 bytes).

Telly captures in this directory:

| telly capture | mirrors reference |
| --- | --- |
| `telly-02-welcome.png` | `02-welcome.png` |
| `telly-02b-welcome-settings-focused.png` | (focus-visual check) |
| `telly-03-add-playlist-type-chooser.png` | `03-add-playlist-type-chooser.png` |
| `telly-07-m3u-url-form.png` | `07-m3u-url-form.png` |
| `telly-07b-disabled-next-focused.png` | (disabled-action focus check) |
| `telly-08-m3u-url-edit-keyboard.png` | `08-m3u-url-edit-keyboard.png` |
| `telly-09-m3u-url-typed.png` | `09-m3u-url-typed.png` |
| `telly-10-m3u-after-url-commit.png` | `10-m3u-after-url-commit.png` |
| `telly-10b-next-focused.png` | `10-m3u-after-url-commit.png` (focus state) |
| `telly-11-m3u-processing.png`, `telly-12-m3u-processing-done.png` | UNREACHABLE — both show the load-error state caused by item 1 |
| `telly-motion-first-frame-after-step-change.png` | (motion evidence: step renders one frame before the focus pill) |

## What matches (verified, no action)

- Colors are exact everywhere sampled: background `#131619`, guidance pane `#232629`,
  focus pill `#DEE0E2`, middle/action column `#131619`, divider `#222527` at x=1616.
- Guidance icon pane: icon bbox pixel-identical (`[133,368][346,516]`) on 03 and 07.
- Action-row pill geometry: 595x87 px, corner radius 8 px, rows start y=378, pitch 104 px —
  all match the reference exactly.
- Next/Back action pills: focused Next pill x-bounds within 3 px of reference
  (`telly [1658,378][1881,466]` vs ref `[1655,378][1878,466]`).
- Default focus at every step matches (welcome: Add playlist; chooser: M3U playlist;
  URL step: Enter URL).
- D-pad traversal: LEFT/RIGHT between welcome buttons; DOWN Enter URL → Paste →
  Select local; RIGHT from options to the actions column — all work.
- BACK semantics match the catalogue at every step: URL step → chooser, chooser → welcome,
  welcome → app exits.
- Next enables live while typing (grey → white), matching ref 09.
- Inline EditText + system Gboard TV IME (no custom keyboard), typed text grey, cursor shown.
- Disabled Next is grey with the → glyph, matching ref 07.

## Punch list

### P0 — blockers

1. **HTTP playlists cannot load at all: cleartext traffic is blocked.**
   - Ref: `11/12` — TiviMate downloads `http://10.0.2.2:8090/playlist.m3u` and shows the
     processed summary. Telly: `telly-11-m3u-processing.png` — instant red error
     "Could not load the playlist. Check the URL and try again."
   - The fixture is reachable from the emulator (HTTP 200 via `nc`), the failure is app-side:
     `AndroidManifest.xml` has no `android:usesCleartextTraffic="true"` and no
     `networkSecurityConfig`, so with targetSdk 36 the OS rejects every `http://` URL before
     OkHttp opens a socket (`M3uFetcher` surfaces it as IOException). Virtually all real IPTV
     playlist URLs are plain HTTP. **The wizard cannot be completed end-to-end on this commit.**
   - Fix: add `android:usesCleartextTraffic="true"` (or a network security config permitting
     cleartext) to the application element.

### P1 — clearly off at arm's length

2. **Guidance pane 64 px too narrow; entire middle column shifted 64 px left (all wizard steps).**
   - Ref 03/07: left pane background ends at x=940; option labels start x=1008; focused pill
     `[980,378][1574,465]`. Telly: pane ends x=876; labels x=944; pill `[916,378][1511,465]`.
   - Root cause: `WizardScreenDims.guidanceWidth = 438.dp` was taken from the uidump's
     RelativeLayout (876 px), but the *painted* pane in the reference PNG is 940 px wide.
   - Fix: `guidanceWidth = 470.dp` and keep the option column anchored at x=1008 (pill inset
     so its left edge lands at 980).

3. **Option/button/action label text is ~15% oversized across the wizard.**
   - Measured glyph runs: "Xtream Codes" ref 152x20 px vs telly 208x23; "Cancel" ref 72x21 vs
     telly 98x24; welcome "Add playlist" label ref 142x26 vs telly 173x30.
   - Telly uses `16.sp` (`WizardScreenActionRow`, `WelcomeScreenPill`, `WizardScreenUrlField`,
     `WizardScreenProcessing`); the reference renders at ≈28 px ≈ **14 sp**.
   - Fix: drop those to 14.sp (URL value/description text too).

4. **Focus does not jump to Next after committing the URL with ENTER.**
   - Ref 10: ENTER dismisses the keyboard and focus lands on **Next** (white pill on Next).
   - Telly `telly-10-m3u-after-url-commit.png`: keyboard dismisses but focus stays on the
     Enter URL row; the user must press RIGHT manually.
   - Fix: request focus on the Next action when the inline edit commits with a non-empty URL.

5. **No transition animation between wizard steps.**
   - TiviMate uses leanback GuidedStep transitions (content slide/fade) between welcome →
     chooser → M3U form. A screenrecord of telly shows each step swap completes in a single
     frame (instant cut), plus item 14's focus-pill pop-in. At arm's length the app feels
     like a slideshow next to TiviMate.
   - Fix: animate step changes (slide-in of guidance + actions panes, ~300 ms, decelerate),
     matching GuidedStep motion.

6. **Processing state diverges (code-verified; unreachable at runtime due to item 1).**
   - Ref catalogue for 11: "Processing playlist" with an **indeterminate circular spinner
     replacing the actions column**. Telly (`WizardScreenProcessing.kt`) renders muted
     "Processing playlist" text + a 262dp-wide linear sweep bar.
   - Fix: circular indeterminate spinner in the actions column; re-verify once item 1 lands.

7. **KNOWN divergence (per direction, noted and moved on): completion step.**
   - Ref 12/13: "Playlist is processed" summary (Movies/Channels count, editable Playlist
     name auto-filled from host, TV/VOD radio) followed by the EPG source step, then the TV
     guide. Telly jumps to the "Channels loaded: N" stub (`ChannelsLoadedScreen`). Could not
     be exercised this run because of item 1.

### P2 — nitpicks

8. **Welcome buttons too wide and 7 px high.**
   - Ref: Add playlist pill 209x72 at `[757,604][966,676]`, Settings 166x72 at
     `[998,604][1164,676]`. Telly: 259x72 at `[712,597][971,669]`, Settings 206x71.
     Gap (32 px) and height match; the extra width is the oversized 16 sp label (item 3)
     plus `20.dp` horizontal padding (ref ≈17 dp), and the row sits 7 px high.

9. **Welcome subtitle sits 6 px high with wider letter tracking.**
   - Identical string spans 1177 px in ref (glyph top y=487) vs 1248 px in telly (y=481).
     Glyph height identical (34 px) — it is tracking, not size. Title shows the same ~2.5%
     per-char widening. Fix: tighten `letterSpacing` (Compose default for these sizes is
     wider than TiviMate's Roboto rendering; try `0.sp` / `(-0.01).em`).

10. **Guidance step title sits 13 px high with the same wide tracking.**
    - "Playlist type": ref glyphs y 374–442, 367 px wide; telly y 361–429, 389 px wide.
      Height matches (36 sp is correct); nudge the title block down ~6 dp and fix tracking.

11. **IME action key: checkmark (Done) instead of the reference's Next (→|) key; keyboard
    panel floats centered instead of right-anchored.**
    - Ref 08/09: Gboard enter key shows the →| Next glyph and the panel sits at x≈1043–1877
      under the form column. Telly: checkmark (Done) and the panel floats at x≈540–1380.
    - Fix: set `imeAction = ImeAction.Next` on the URL field; panel position should follow
      once the editor bounds/cursor-anchor info match (re-check after item 2).

12. **Edit-mode row expansion metrics differ.**
    - Ref 08: opening the editor pushes the rows below down 79 px (Paste 506→585) and the
      underline draws at y=501 with an 8 px left inset from the label (x=1016 vs 1008).
      Telly: pushes 56 px (504→560), underline at y≈520, no inset (starts flush at x=944).

13. **Focused *disabled* Next gets an invented outline treatment.**
    - `telly-07b-disabled-next-focused.png`: telly draws a grey rounded **border** around the
      focused disabled Next. No captured reference shows this state, but an outline focus ring
      appears nowhere in TiviMate's design language (focus is always the filled white pill;
      leanback typically skips disabled actions when focusing). Suggest: make disabled actions
      unfocusable (focus lands on Back), or keep the standard pill treatment dimmed.

14. **Focus pill renders one frame after the step content.**
    - `telly-motion-first-frame-after-step-change.png`: the chooser paints fully un-focused
      for a frame before the M3U pill appears (visible flicker at 60 Hz). Would be masked by
      item 5's transition; otherwise pre-resolve initial focus before first draw.

15. **Committed two-line row pill is 5 px shorter than reference two-line rows**
    (119 px vs 124 px, cf. ref 12's Playlist-name row). Check title/description vertical
    padding when a description line is present.

16. **Copy: "telly doesn't provide any sources of TV channels" vs "TiviMate doesn't…".**
    - Intentional brand substitution; subtitle, buttons, and all wizard copy are otherwise
      word-for-word identical. No action unless pixel-parity of the headline width matters.

Not comparable this run: error-state presentation (reference set contains no failed-load
capture), "Paste from clipboard" / "Select local playlist" behavior, and steps 11–13
(blocked by item 1).

## Severity summary

- **P0: 1** (cleartext block — wizard cannot complete; E2E fails)
- **P1: 6** (items 2–7; worst visual offenders: 64 px column shift, 15% oversized labels,
  missing transitions, no focus-jump on commit)
- **P2: 9** (items 8–16)
