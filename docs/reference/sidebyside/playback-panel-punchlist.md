# Playback screen + channel panel — round 3 side-by-side punch list

Captured 2026-09-13 on emulator-5554 (1920×1080, 2 px = 1 dp), TiviMate 5.2.0 free vs
telly debug build @ 7f70edb, both on the fixture playlist (News One = ch1), fixture EPG.

Evidence: `round3-ref/` = TiviMate (fresh, state-matched), `round3/` = telly.
Timing was measured with the emulator-console host recorder (`adb emu screenrecord`,
~21 fps real-time frames); frame timestamps quoted below are from those recordings
(`tm-ov4.webm`, `tm-zap.webm`, `tm-cold2.webm`, `ty-ov.webm`, `ty-zap.webm`, `ty-cold.webm`).

**Reference-behavior corrections discovered this round** (live device differs from the
capture catalogue — the fix agent should trust these, they were re-verified twice):

- **UP at bare fullscreen opens the INFO overlay** (same as OK/DOWN), *not* the guide
  overlay at the previous channel (catalogue §3 keymap, capture 36, is from a different
  pre-state). Repeated UP cycles focus into the transport row (see R-2 below).
  Evidence: `round3-ref/03-panel-up.png`, `03b-panel-up-up.png`, `03c-panel-up-x3.png`.
- **long-OK / MENU at bare fullscreen opens a bottom icon quick-bar**, not the right-side
  sheet: `Search · Channels list · Recordings · Multiview · Picture-in-picture ·
  1280 × 720 · Mono · 0 ms · Off(CC)`, focus = white circle on Search, auto-hides ~5 s.
  Corpus capture 38's right sheet is the long-OK menu of the *guide/panel* context (its
  background shows the guide with preview window). Evidence:
  `round3-ref/07-player-ctx-menu.png/.xml`, `08-player-menu-key.png` vs corpus `38-*`.
- **The right-side sheet menu** (Search/Settings/program/channel/All channels) is what
  long-OK opens **on a row inside the guide overlay/panel**. Evidence:
  `round3-ref/05-panel-row-longok.png/.xml`.
- **Focused shortcut card (TV guide/History) is NOT white.** Focused = `#252A2D`
  (37,42,45) with white icon/text, unfocused = `#181E20` (24,30,32); focus is shown by
  the lighter fill + ~13 % size growth (280×208 vs 248×184 px). The catalogue's
  "focused card #FFFFFF" is wrong. Evidence: `round3-ref/02-info-overlay.png`, steady
  frames of `tm-ov4.webm`.
- **Zap shows a compact info overlay variant** (logo + title + time/progress/remaining +
  number+name + **description line** + next programme; no full-width progress bar, no
  shortcut cards, no chevron) while the OLD video keeps playing until the new stream is
  ready. Evidence: `round3-ref/10-zap-2/3-*.png`.
- **Cold start**: system task-snapshot as starting window → guide skeleton (header +
  black preview box, no rows) → rows populate ~2 s later → focus pill ~2.5 s. Nothing
  auto-tunes. No splash, no welcome interstitial. Evidence: `round3-ref/09-coldstart-*`.

## Timing measurements (host-recorder, ±50 ms)

| Event | TiviMate | telly |
|---|---|---|
| Info overlay entrance | ~300–400 ms fade + ~12 px upward settle (4.16→4.59 s in tm-ov4) | instant pop, ≤80 ms (`ty-ov.webm` 5.06→5.14 s) |
| Info overlay visible (first frame → gone) | **5.13 s** (4.164→9.298, hide starts ≈5.1 s after keypress) | **4.51 s** (5.144→9.652) |
| Info overlay exit | instant cut ≤45 ms | instant cut ≤50 ms (matches) |
| Panel/guide-overlay open | ~150 ms fade, content pops fully formed (tm-zap 5.78→5.95 s) | instant pop ≤100 ms (ty-zap 3.00→3.10 s) |
| Panel close (on zap OK) | ~140 ms fade back to video (9.289→9.425) | instant cut ≤45 ms (8.890→8.934) |
| Zap: video gap | **none** — old video keeps playing; new stream swaps in ~1.5–2 s; compact zap overlay appears with the panel-close fade and auto-hides ~5.5 s later | **1.31 s full black** (8.934→10.243), no overlay at all |
| Cold start | snapshot → guide skeleton → rows ≈2 s later | ~0.8 s app bg → **welcome flash ~0.65 s** → **pure black ~1.65 s** → video |

## P0 — functional

1. **Programme title/episode formatting is broken everywhere.**
   telly renders `Global Update. 0.9.` where TiviMate renders
   `Global Update: Episode 10. S1 E10`. Root causes: (a) `PlaybackInfo.displayTitle`
   (`features/playback/PlaybackInfo.kt:50`) appends the RAW `<episode-num>` string
   (xmltv_ns `0.9.` = zero-based S1 E10) instead of parsing it to `S1 E10`;
   (b) the XMLTV `<sub-title>` element is never read
   (`features/epg/XmltvElementReader.kt:9` — `programTextTags` lacks `sub-title`), so the
   `Title: Subtitle` form ("Global Update: **Episode 10**") can't be built.
   Affects: info overlay title + next-programme line, panel rows, panel detail card,
   context-menu blue section header.
   Evidence: `round3/02-info-overlay.png` vs `round3-ref/02-info-overlay.png` (+ .xml
   text `Global Update: Episode 9. S1 E9`), `round3/04-panel-ch1-focused.png` vs
   `round3-ref/04-guide-overlay.png`.

2. **Zap transition: 1.3 s of black and zero feedback.**
   TiviMate: panel fades out ~140 ms, the OLD channel's video keeps playing, a compact
   zap overlay (see reference-behavior note above — includes the programme description
   line) appears immediately with the NEW channel's info, the new stream replaces the
   old ~1.5–2 s later, overlay auto-hides ~5.5 s after the zap.
   telly: panel hard-cuts, screen is pure black 8.934→10.243 s (1.31 s), then video pops;
   no overlay, no channel identification at any point.
   Fix: keep the previous stream frame (don't clear the surface on load) in
   `features/player/Media3PlayerEngine.kt` / `PlayerScreenSurface.kt`, and auto-show the
   info overlay (zap variant with description) on tune in
   `features/playback/TuneController.kt` / `PlaybackViewModel.kt`.
   Evidence: `round3-ref/10-zap-1/2/3-*.png`, `tm-zap.webm` vs `round3/10-zap-1/2/3-*.png`,
   `ty-zap.webm`.

3. **Cold start flashes the welcome screen (known issue) + long black gap.**
   Confirmed: welcome screen fully rendered for ~0.65 s (`ty-cold.webm` 12.20→12.85 s,
   `round3/09-coldstart-2-welcome-flash.png`), then ~1.65 s pure black, then video.
   Reference behavior to copy: TiviMate never shows an interstitial — its first app frame
   is the destination screen's skeleton (`round3-ref/09-coldstart-2-guide-skeleton.png`);
   for telly (which resumes playback) the equivalent is a black/quiet player frame, i.e.
   suppress the welcome route until the Room read resolves
   (`features/onboarding/StartRoute.kt` + `core/navigation`).

## P1 — clearly off at arm's length

4. **UP key opens the wrong surface.** Live TiviMate: UP at bare playback = info overlay
   (identical to OK). telly: UP = channel panel at previous channel (built from the
   catalogue's incorrect keymap). Decide with the director — but as observed, telly
   deviates from the real app. Related: TiviMate's second UP moves focus into a
   transport-control row (00:16/45:00, ⏮ ⏪ ⏸ ⏩ ⏭, LIVE badge, record dot —
   `round3-ref/03b-panel-up-up.png`) which telly lacks entirely (no transport row at all).
   Fix location: `features/playback/PlaybackKeyPolicy.kt`.

5. **Player context menu is the wrong menu.** Live TiviMate long-OK/MENU at fullscreen =
   bottom icon quick-bar (see corrections above; `round3-ref/07-player-ctx-menu.png/.xml`).
   telly shows the right-side sheet (`round3/07-player-ctx-menu.png`). telly's sheet is a
   faithful clone of the *panel-row* menu (that part is good — see Matches), but at bare
   fullscreen TiviMate shows the quick-bar instead.
   Fix: `features/playback/PlaybackMenuHandler.kt`, `PlayerMenu.kt`, `PlaybackScreenMenu.kt`.

6. **Info overlay auto-hide ~0.6 s too short and no entrance animation.**
   TiviMate: ~0.35 s fade-in + ~12 px upward settle, hide begins ≈5.1 s after keypress
   (visible 5.13 s), exit instant. telly: instant pop-in, visible 4.51 s, exit instant.
   Fix: `features/playback/OverlayState.kt` (timeout), `PlaybackScreenInfoOverlay.kt`
   (AnimatedVisibility fade+slide ~350 ms).

7. **"25 FPS" badge missing.** telly shows `HD MONO`; TiviMate shows `HD 25 FPS MONO`
   for the same stream. `PlaybackBadges.fps()` drops it because Media3 reports
   `frameRate <= 0` for the fixture TS — TiviMate derives it anyway (likely from the
   decoder/track). Fix: surface frame rate in
   `features/player/Media3PlayerEngine.kt` video-details mapping.
   Evidence: `round3/02-info-overlay.png` vs `round3-ref/02-info-overlay.xml`
   (`25 FPS` node [1099,666][1178,696]).

8. **Focused shortcut card is white — should be dark grey.** TiviMate focused
   TV guide card fill = (37,42,45) with WHITE icon/text; unfocused = (24,30,32).
   telly focused = (222,224,226) with BLACK icon/text (inverted). Card sizes match
   (~280×208 focused / 248×184 unfocused) — keep those.
   Fix: `features/playback/PlaybackScreenCards.kt`.
   Evidence: crops of both `02-info-overlay.png` (see also tm-ov4 steady frames).

9. **Mini dash-progress fill is blue — should be light grey.** TiviMate's 80×6 px dash
   next to the time range uses fill (183,185,188) on a dark track; telly fills with
   #2196F3. (The full-width bottom bar IS blue in both — only the small dash differs.)
   Fix: `features/playback/PlaybackScreenInfoLines.kt`.

10. **Badge pill styling.** TiviMate: translucent dark pill, text light grey (#999-ish),
    HD pill 43×30 px, gaps 16 px. telly: solid (58,61,64) pill, text pure white,
    HD pill ~56×32 px (more horizontal padding), gap ~21 px.
    Fix: `features/playback/PlaybackScreenInfoLines.kt` badge composable.

11. **Info overlay geometry offsets.**
    - Logo tile: TiviMate visible tile 158×158 at x 125–283, y 590–748 (centered inside
      a 248-wide slot starting x=80); telly 157×157 at x 80–237, y 609–766 → 45 px too
      far left, 19 px too low. Focus border: telly bright #2196F3; TiviMate's reads as a
      muted steel blue (~(46,91,129) sampled) — dimmer.
    - Text column starts x=360 in TiviMate ([360,591] title); telly ~x=272.
    - Title box top y=591 (TiviMate) vs ~618 (telly).
    - Full-width progress bar center y=771 (TiviMate) vs y=799 (telly), both x 80–1840.
    Fix: `features/playback/PlaybackScreenInfoOverlay.kt` paddings.
    Evidence: `round3-ref/02-info-overlay.xml` bounds vs pixel scans of
    `round3/02-info-overlay.png`.

12. **Top scrim too short.** TiviMate's top gradient is still dimming at y=150 and fades
    out ≈y=170–180 (two stacked scrim views [0,0–80] + [0,80–160]); telly's is gone by
    y≈75. Bottom scrim depth looks comparable.
    Fix: `features/playback/PlaybackScreenInfoOverlay.kt` scrim brush.

13. **Panel: focused-row detail card is pinned to the top — TiviMate expands it inline.**
    In the TiviMate guide overlay the focused channel row itself expands into the detail
    card (logo 140×140, title, times + dash + remaining, description, star, group name at
    right edge) at the row's position in the list; telly renders a fixed detail block
    above the list (clock + logo + title + times + description) that never moves.
    TiviMate's detail card also shows the **star (favorite) icon and group name at the
    right edge** — absent in telly's card.
    Fix: `features/panel/ChannelPanelScreen.kt` / `ChannelPanelScreenDetail.kt`.
    Evidence: `round3-ref/04-guide-overlay.png` vs `round3/04-panel-ch1-focused.png`.

14. **Panel channel context menu is a subset and drops the panel behind it.**
    TiviMate long-OK on a panel row: full right sheet — Search, Settings,
    [programme]: Open in external player/Record/Custom recording/Add to My list/Program
    description, [channel]: Add to Favorites/Block channel/Hide channel/Assign EPG/
    Channel options, [All channels]: Manage Favorites…Group options — with the guide
    overlay still visible behind. telly: only the channel section (5 items), and the
    panel is dismissed to bare video behind the sheet.
    Fix: `features/panel/PanelViewModel.kt` + `features/playback/PlaybackScreenMenu.kt`
    (reuse the full sheet; keep the panel composed behind it).
    Evidence: `round3-ref/05-panel-row-longok.png/.xml` vs `round3/05-panel-row-longok.png`.

15. **Menu sheet metrics.** TiviMate: sheet 512 px wide (x 1392–1904), 16 px margin from
    screen right/top, rounded corners; row pitch 80 px; the focused row is a rounded
    white pill **inset 16 px** from the sheet edges. telly: sheet flush to the top/right
    (y=15, right edge 1896), row pitch 72 px, focus pill square and flush to the sheet
    edges. Icons on Search/Settings match; no other rows have icons in either app ✓.
    Fix: `features/playback/PlaybackScreenMenu.kt`, `core/ui/TellyScreenMenuRow.kt`.

16. **No transport/timeshift row.** See item 4 — TiviMate's overlay can expand into
    transport controls (progress 00:16/45:00, ⏮⏪⏸⏩⏭, LIVE pill, record dot). telly has
    no equivalent; DOWN below the shortcut row is a no-op. (May be deliberately deferred
    with catch-up; flag for the director.)

## P2 — nitpicks

17. **Panel scrim tint.** Over identical video, telly panel background reads (22,29,36)
    vs TiviMate (34,46,60) — telly is darker and less blue; TiviMate lets more video
    through (its NEWS ONE watermark is clearly visible through the cell field).

18. **Panel/overlay motion.** telly panel opens/closes with an instant cut; TiviMate
    cross-fades ~150 ms both ways. telly info-overlay exit matches (instant) ✓.

19. **BACK at bare playback** goes to the channel panel in telly (documented stand-in);
    TiviMate returns to the TV guide (`round3-ref/06-back3-from-player.png`). Becomes P1
    once the guide slice exists.

20. **Chevron.** TiviMate: 56×56 px glyph centered at (996,1036), bright. telly's is
    smaller/thinner and ~30 px lower (bottom edge y≈1067), dimmer.

21. **Panel list does not wrap on DOWN at the last row** (telly stays on ch30;
    `round3/04-panel-ch1-focused.png` sequence). TiviMate wrap behavior in the guide
    overlay was not verified this round — verify before "fixing".

22. **telly panel rows show only the programme title** ("Weather Watch") in unfocused
    rows where TiviMate shows `Title: Subtitle` cells; partially subsumed by item 1 —
    once sub-titles parse, apply the same `Title: Subtitle` form to rows.

## Matches — do not churn

- Bare fullscreen playback: pixel-identical video, zero chrome both apps
  (`*/01-bare-playback.png`).
- Info overlay anatomy/order: group top-left + clock top-right (white, same format
  "Sun, Sep 13, 6:51 PM"), logo tile + title + time-range/dash/remaining/number+name
  (bold) + badges + next-programme line + full-width blue progress bar with round thumb +
  TV guide & History cards + centered chevron. All present, right order.
- Shortcut card geometry (280×208 focused / 248×184 unfocused, focus growth) and icons
  (grid, clock-with-arrow) — only the fill/icon colors are wrong (item 8).
- Full-width progress bar: blue #2196F3 fill, grey remainder, round thumb at now; spans
  x 80–1840 in both.
- Panel row pitch 78 px (39 dp) exactly matches the TiviMate guide-overlay rows; number /
  logo-tile / name column layout is close; playing channel rendered with blue name + blue
  ▶ marker in both; focused row = white pill with black text in both.
- Group renumbering restarts from 1 per group (verified round 1; not retested).
- Context-menu content (in the fullscreen sheet): section order and ALL 17 item labels
  verbatim-identical to TiviMate, including the below-the-fold All-channels section
  (`round3/07b-player-ctx-menu-bottom.png`); blue #2196F3 section headers ✓.
- BACK chain: menu → panel → player, matches TiviMate step-for-step (given telly's menu
  opens from the panel); overlay BACK-dismiss matches; long-BACK not needed at parity yet.
- Clock in panel header `#90CAF9` blue ✓ (telly detail block clock matches TiviMate's
  guide header styling).
- Info overlay auto-hide exit style (instant, no fade-out) matches.

## Session state notes

- TiviMate's RECORD_AUDIO permission ended up **granted** during cold-start testing (the
  OS mic dialog from its Search screen got accepted by a queued key event). Playlist,
  EPG and settings are untouched; the grant only silences the dialog if Search is opened
  with voice. No playlist/settings were modified in either app; telly's last channel is
  ch1 (News One).
- Both apps left force-stopped.
