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

---

## Round 2 resolution (2026-09-13, fixed on main)

Evidence: `round2/telly-*.png` (same emulator, fixture URL, fresh install per run).
All measurements below were re-taken with ImageMagick bounding boxes / pixel
profiles against the reference PNGs; "exact" means identical bbox, "±Npx"
means within N px.

1. **FIXED (P0).** `res/xml/network_security_config.xml` (cleartext permitted for all
   domains) + `android:networkSecurityConfig` in the manifest. Full wizard completes
   end-to-end against `http://10.0.2.2:8090/playlist.m3u`; Room ends with 30 channels /
   5 groups and the playlist row (name `10.0.2.2`, epgUrl from `url-tvg`) — verified by
   pulling the DB. Evidence: `telly-11/12/14`.
2. **FIXED.** `WizardScreenDims.guidanceWidth = 470.dp`. Painted pane edge now at
   x=940 exactly (last `#232629` pixel at 939 in both); option labels x=1008, focused
   pill `[980,378]` 595–596 px wide (ref 595); divider within 1 px of x=1615.
3. **FIXED.** Action/label text is 14 sp — and, discovered while fixing: TiviMate renders
   guided-action labels in **Roboto Condensed** (leanback's guided-action style).
   With `sans-serif-condensed` + `letterSpacing 0`: "Xtream Codes" 153x22 **exact**,
   "M3U playlist" 132x27 **exact**, "Stalker Portal" 147x22 **exact**, "Cancel" 73x22,
   welcome "Add playlist" 143x27 **exact** (welcome buttons stay regular Roboto, which
   matches). Descriptions are 12 sp (uidump: 33 px line vs 38 px title line).
4. **FIXED.** ENTER commit hides the IME and moves focus to **Next**
   (`WizardScreenEditState`); `telly-10-m3u-after-url-commit.png` shows the white pill
   on Next, matching ref 10.
5. **FIXED — with a correction to this item's assumption.** TiviMate's GuidedStep
   transition was re-measured frame-by-frame on the emulator (screenrecord of
   settings-root → guided step and BACK, 60 fps extraction): it is **not a slide** —
   both steps render at their final positions and cross-fade in ~100–130 ms (old
   fully visible → settled in 6–8 frames, both directions). telly now uses a matching
   120 ms linear `Crossfade` between wizard steps *and* top-level routes
   (`core/ui/CrossfadeScreen.kt`). Mid-fade frame: `telly-motion-midcrossfade.png`.
6. **FIXED.** Circular indeterminate spinner (rotating 270° arc) replaces the actions
   column during processing; guidance shows the download icon + "Processing playlist".
   `telly-11-m3u-processing.png`. Still no reference capture of this state exists, so
   spinner size/position remain best-effort per the catalogue description.
7. **FIXED (was KNOWN divergence).** The full processed/name step now exists
   (`WizardScreenNameStep` + `WizardStep.PROCESSED`): guidance "Playlist is processed"
   + "Channels: 30" (or "Movies: N" when all streams are `.mp4`/`.mkv`, per catalogue
   parser behavior), editable **Playlist name** pre-filled with the URL host
   ("10.0.2.2"), **TV playlist** / **VOD playlist (no TV channels)** radios, Next/Back.
   Name persists through `PlaylistRepository.add(url, playlist, name)`; a nameless
   background re-add keeps the custom name. Measured vs ref 12: pill 125 px tall at
   `[980,378]` **exact**, title glyphs **exact**, value glyphs **exact** (incl. focused
   value color `#6F7071` / resting `#5D5F61`), radio rows within 1 px.
   Remaining divergence, per direction: after Next, TiviMate shows the EPG-source
   step (ref 13) and then the TV guide; telly has neither yet, so it lands on a
   branded placeholder stating channel + group counts
   (`telly-14-channels-loaded-landing.png`). The EPG URL is already consumed from
   `url-tvg` by the EPG slice, and the TV/VOD radio is UI state only (nothing to
   persist until a VOD slice exists). Both arrive in later slices.
8. **FIXED.** Welcome pill horizontal padding 16.5 dp + 14 sp label: Add playlist pill
   209–210 px wide at y=603 (ref 209 px at y=604); label glyphs 143x27 **exact**.
9. **FIXED.** `letterSpacing = 0.sp`: subtitle glyph run 1178x35 at y=487 — **exact**
   width and position (ref 1178x35 @ 487).
10. **FIXED.** Guidance title: +6 dp down (`padding top 28.dp`) and `(-0.01).em`
    tracking → "Playlist type" 369 px wide at y=373 (ref 368 px at y=374).
11. **PARTIALLY FIXED.** The IME action key is now the →| Next key (ref 08/09): the
    inline editor is a real `EditText` (like leanback's) with
    `imeOptions = IME_ACTION_NEXT`. The floating Gboard-TV panel, however, still
    docks screen-center instead of right-anchored: the panel position is chosen by
    the IME and did not follow even with a real EditText reporting identical editor
    bounds `[1008..1547]`. Not app-controllable as far as investigated — **deferred**
    (cosmetic; keyboard layout, keys and behavior match).
12. **FIXED.** Edit-mode metrics re-derived from ref 08/09 and matched: label glyphs
    **exact**, value text 12 sp condensed `#5D5F61` at (1017,464) (ref exact),
    underline at y=501–502 spanning x=1016→1616 (to the divider; ref 601 px vs telly
    600 px), rows below pushed to the same push (Paste glyphs land at identical y).
13. **FIXED.** Disabled actions are now unfocusable (`focusProperties.canFocus`),
    matching leanback: RIGHT from the options column lands on **Back**
    (`telly-07b-disabled-next-focus-skipped.png`); the invented outline treatment is
    gone.
14. **FIXED (masked by item 5, as predicted).** The incoming step's focus pill is
    already present during the cross-fade (`telly-motion-midcrossfade.png`); no
    visible pop-in at 60 Hz.
15. **FIXED.** Two-line row: 12 sp description, 4.5 dp title→description gap, 2.5 dp
    description bottom inset → committed/name pill is 125 px tall (378..502),
    **identical** to ref 12's pill profile.
16. **RESOLVED AS INTENDED.** "telly doesn't provide any sources of TV channels" is a
    deliberate brand substitution; all other copy is word-for-word. Headline block
    position matched via a measured 3.5 dp offset (same-substring "TV channels"
    glyphs align within 1 px).

Not fixed / out of scope this round: floating-IME panel x-position (item 11, IME-owned);
EPG-source wizard step + TV guide landing (item 7 note — later slices); error-state
presentation and "Select local playlist" remain unreferenced/unbuilt as before.
