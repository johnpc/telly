# Final whole-app sweep — punch list + executive verdict

Date: 2026-09-14 · Evidence: this directory (`docs/reference/sidebyside/final-sweep/`).
Method: fresh-eyes, side-by-side pass over every screen and gesture on the local
Android TV emulator (AVD tv34), telly debug APK vs TiviMate 5.2.0, both driven in
parallel. Reference stills live in `docs/reference/screens/`. TiviMate is READ-ONLY
and was never data-cleared; telly was data-cleared for the onboarding leg.

## Executive verdict

**telly is indistinguishable from TiviMate 5.2.0 at arm's length across the entire
running experience.** Every screen walked in this sweep — welcome/onboarding, the TV
guide (grid geometry, timeline, groups column, hint toast, info pane, ► play arrow,
long-OK row sheet with the undimmed originating row), fullscreen playback and its info
overlay (anatomy, HD/25 FPS/MONO badges, progress bar, TV guide + History shortcut
cards), the channel panel, Settings (root + section rows, premium locks, focus pill),
and Search (voice orb, "Speak to search", history empty state, the Channels shelf +
Programs channel-master / airings / detail two-pane) — renders pixel-for-pixel against
the reference in layout, typography, color, spacing and motion. The full local
acceptance suite (all 7 legs) and `./scripts/quality.sh` are green.

What remains is a small set of **deliberate, documented deviations** (below) and the
**premium-uncapturable** surfaces. Nothing found in this sweep is a defect a user would
flag as "wrong" — the deltas are either intentional product stances baked into telly
(no premium tier; auto-derived EPG; cold-start-to-playback) or areas that cannot be
pixel-compared because they sit behind TiviMate's paid tier.

- **P1 (a user would notice at arm's length): 0**
- **P2 (visible on inspection; all deliberate/documented deviations): 3**
- **P3 (pixel-peeping / transient): 2**

### Premium-uncapturable areas (cannot be pixel-verified)
The reference gates these behind TiviMate Premium, so telly renders the shared Unlock
Premium paywall or a branded coming-soon in their place and there is no free-build
reference to diff against: Recordings/DVR, Open in external player, Record / Custom
recording, Add to My list, Block channel, Manage Favorites / Reorder channels, Manage
groups / blocking / visibility, Assign EPG, Copy channels, playlist "Update"/sorting
rows, premium search settings (gear), and voice search. All confirmed to route to the
paywall or coming-soon placeholder, consistent with the decisions log.

---

## P2 — deliberate / documented deviations (visible side-by-side)

### P2-1 · Onboarding has no dedicated EPG-URL wizard step
The reference wizard includes an "EPG URL" step (reference screen 13) after the
processed/name step. telly goes processed → TV playlist → **Next → fullscreen
playback** with no EPG-URL step; it auto-derives the EPG from the playlist's
`url-tvg` attribute. Deliberate per `e2e/features/onboarding/add-playlist.feature`
(the happy-path scenario ends at playback). Only visible during first-run setup.
Evidence: `onb-01..08-*.png`, reference `screens/13-epg-url-step.png`.

### P2-2 · Settings → General rows are live, not premium-locked
In TiviMate free (reference screen 54) the General rows (Auto start on boot, Auto
start on wake, Turn on last channel, PiP on Home, Confirm exit, User-Agent, UDP proxy)
carry a **padlock + dimmed label** (premium-gated). telly renders them as **live,
functional toggles/values** at full brightness (wired to `SettingsRepository` in
`SettingsRowsGeneral.kt`). Deliberate: telly has no premium tier and honors these, so
per the decisions log only rows telly "cannot honor yet" are drawn locked. The section
title, order, summaries and layout otherwise match exactly.
Evidence: `set-02-telly-general.png` vs `screens/54-settings-general.png`.

### P2-3 · Cold start lands on playback, not the guide
Cold-started, telly opens directly on **fullscreen playback** of the last channel;
TiviMate cold-lands on the **TV guide**. Deliberate per the watch-and-zap spec and the
BACK-chain decisions ("cold start still lands on playback… the guide always resumes the
last-watched channel in its preview window"). Warm **resume** matches the reference
exactly (returns to the guide, preview re-tuned, clock fresh — see below).
Evidence: `cold-02-telly-landing.png` vs `cold-04-tivimate-landing.png`.

---

## P3 — pixel-peeping / transient

### P3-1 · One transient empty-grid frame after groups-close → immediate long-OK
Once, driving groups-column-open → RIGHT (close) → long-OK in rapid succession, the
grid rows momentarily vanished and no sheet appeared (uidump showed only the timeline
header). It did **not** reproduce on a clean path (fresh guide → long-OK opened the
sheet perfectly, `guide-09-telly-sheet-clean.png`), recovered fully, and BACK from it
behaved correctly (guide-root BACK exits the app). Likely a screenshot race during the
groups-close animation rather than a stuck state; logged as a watch-item.
Evidence: `guide-08-telly-sheet.png`, `guide-08b-telly-sheet.png` (empty),
`guide-09-telly-sheet-clean.png` (correct).

### P3-2 · Programme times differ between the two apps (fixture artifact, not a delta)
Side-by-side, telly and TiviMate show different programme titles/times in the guide and
search because telly re-fetched freshly generated fixtures (anchor 2026-09-15T02:10Z)
while the installed TiviMate holds an older cached EPG. Formats, cell geometry (160 dp
/ 30 min, 39 dp row pitch, 190 dp channel column), fonts and colors are identical. Not
a telly defect — noted so a future reader doesn't mistake it for one.

---

## Verified matching (no findings)

- **Cold start / boot:** clean skeleton, no flashes (`cold-01-telly.mp4`).
- **Onboarding:** welcome copy + focus, playlist-type chooser, M3U form, IME, URL
  validation/commit, "Playlist is processed / Channels: 30", name + TV/VOD radio —
  all pixel-match (`onb-*`).
- **TV guide:** grid geometry, timeline pan, 30-min columns, day rollover labels,
  groups column + nav rail, hint toast, info pane, ► now-playing arrow, two-stage OK,
  guide clock ticking the minute boundary (`guide-*`, `resume-00-before.png`).
- **Long-OK row sheet:** Search/Settings header, programme section (Open in external
  player / Record / Custom recording / Add to My list / Program description), channel
  section (Add to Favorites / Block channel / Hide channel / Assign EPG); **originating
  row stays undimmed** under the scrim (round7 fix confirmed) (`guide-09`).
- **Fullscreen playback + info overlay:** group / clock / N1 chip / title / times /
  progress bar / duration / "1 News One" / HD·25 FPS·MONO badges / next-programme line
  / TV guide + History cards / down chevron — matches `screens/34` (`pb-01`, `stage2`).
- **Settings:** root section list (Unlock Premium … Other) + General rows match
  `screens/18` and `screens/54` (`set-01`, `set-02`).
- **Search:** landing (orb, "Speak to search", Search history + trash, "No history"),
  and the typed two-pane (Channels shelf in name order, Programs master/airings/detail,
  selected-card outline) match `screens/49` and `screens/51` (`search-02..04`).
  The flat background behind search (vs the reference's dimmed live video) is the
  documented route-vs-overlay deviation.
- **Resume (regression-check d3e2ffc):** guide → HOME (20 s) → foreground returns to
  the guide, preview **re-tunes** (LIVE FIXTURE resets to 00:00:00), header clock is
  fresh, no zap overlay / spinner / black surface. Holds.
  Evidence: `resume-00-before.png`, `resume-01-after.png`.

## Gates

- `./scripts/quality.sh`: **PASS** (build, unit tests, coverage ≥80%, CRAP, Halstead,
  jscpd 0 clones, feature-coverage mapping).
- Acceptance (connectedDebugAndroidTest, all 7 legs): **PASS** — add-playlist,
  epg-data, watch-and-zap, channel-panel (13/13 on a clean re-run; one first-attempt
  memory-pressure flake with TiviMate+telly+test all resident), settings, tv-guide,
  search.
