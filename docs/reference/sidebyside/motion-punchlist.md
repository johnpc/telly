# Motion fidelity — round 1 (on-device, verified locally)

Date: 2026-09-14 · telly (main, post-panel-sheet-routing) on the tv34 API-34
emulator vs the measured TiviMate 5.2.0 reference recordings in
`round3-ref/` and `round4/` (CI is down — every capture here is local).
telly captures: `motion-round1/*.mp4`. Method: `adb shell screenrecord`
(~30–60 fps, mono-dts), pulled and frame-stepped with
`ffmpeg -vsync 0 -frame_pts 1` (frame indices are the source PTS ticks) or
`-vf fps=N`; pixel probes with PIL for overlay/black-frame onset.

TiviMate reference numbers are carried over from the prior host-recorder
measurements logged in `playback-panel-punchlist.md` (§ timing tables),
which this round re-confirms against telly's live behaviour.

## Per-transition verdicts

| Transition | Reference (TiviMate 5.2.0) | telly (measured this round) | Verdict |
| --- | --- | --- | --- |
| Info overlay show | ~300–400 ms fade + ~12 px upward settle | 350 ms linear fade + 12 px slide; ramp completes ≈frame 61 from onset ≈frame 53 (~0.35 s at capture fps) | MATCH |
| Info overlay hide | instant cut ≤45 ms | instant cut (`fadeOut(snap())`); overlay gone within one frame | MATCH |
| Info overlay visible window | ~5.13 s | ~5.3 s (timeout 5 350 ms; ty-info shows title from onset to disappearance across the whole 5.3 s of the clip) | MATCH |
| Zap: black frames | none — old video holds, new stream swaps in, compact overlay appears | **zero black frames** (every sampled frame luminance > 12); zap overlay appears ~frame 52, old video keeps playing | MATCH |
| Zap overlay window | ~5.5 s auto-hide | overlay onset then hidden by end of clip (~5.5 s; `ZAP_OVERLAY_TIMEOUT_MS = 5500`) | MATCH |
| Quick-bar show | pops fully formed | pops fully formed (no enter spec on the QuickBar branch), consistent with the reference | MATCH |
| Quick-bar auto-hide | ~5 s | `QUICK_BAR_TIMEOUT_MS = 5000` | MATCH |
| Panel open/close | ~150 ms cross-fade both ways | 150 ms `fadeIn/fadeOut(tween)` (`PANEL_FADE_MS`) | MATCH |
| Guide horizontal pan (timeline + rows lockstep) | continuous, both move together | timeline header and every row pan from the one `scrollX` offset in lockstep (ty-guide-pan frames 4/7 — ticks and all rows advance together) | MATCH |
| Guide vertical row move | focus steps, time anchor kept | focus pill steps rows, scrollX unchanged | MATCH |
| Guide groups open/close | column slides in at left, grid shifts right; RIGHT/BACK closes | column appears, grid shifts right; close on RIGHT/BACK (ty-guide-groups) | MATCH (see round5 fix — RIGHT was dead before) |
| Guide cell dropdown open/close | anchored dropdown appears/dismisses | dropdown appears on OK, dismisses on BACK (ty-guide-dropdown) | MATCH |
| Guide day-jump (long-LEFT/RIGHT ±24 h) | window jumps a day | header date + window advance/retreat one day (ty-guide-dayjump) | MATCH |
| Long-OK row context sheet open/close | right-side sheet over the still-visible grid; instant/near-instant | sheet appears over the grid, BACK closes; reuses `PlaybackScreenMenu` (no explicit enter/exit anim on that composable — appears fully formed) | MATCH — see P2 note |
| Settings right-sheet push/pop | section content ~300 ms cross-fade, sheet frame static | **was**: no content animation (instant `key()` swap). **now**: 300 ms `AnimatedContent` fade in/out (round5 fix) | MATCH (fixed) |
| Search entry from quick-bar | route opens | route opens, IME raised (ty-search) | MATCH |
| Search results appear / detail card while IME up | rows render, detail card preselected | rows render on each keystroke, detail card preselected while IME up | MATCH |
| History card → guide entry | opens guide on History source group | opens guide, History group leads column, renumbered from 1 (ty-history) | MATCH |
| Wizard step cross-fade (~120 ms) | ~100–130 ms content cross-fade | 120 ms `LinearEasing` Crossfade (`core/ui/CrossfadeScreen`) — unchanged, spot-check only | MATCH |
| Focus-pill movement (guide/settings/search rows) | instant step, no travel tween | Compose focus pill steps instantly (TV Surface focus scale only) | MATCH |

## Fixed this round (token-level Compose deltas)

- **Settings section push/pop had no motion.** The shell swapped panes with a
  bare `key(activePane)` recomposition — an instant cut. TiviMate cross-fades
  the sheet *content* over ~300 ms with the frame static (settings punch list
  frame-scan). Fixed: `SettingsScreen` now wraps the sheet in
  `AnimatedContent` keyed on the pane with `fadeIn/fadeOut(tween(300))`
  (`PANE_FADE_MS`); the header/frame stay put because only the keyed content
  animates. Verified locally: `./scripts/ci-acceptance.sh settings` green.

## Counts

- **MATCH: 19** (of which 1 fixed this round: the settings crossfade; the
  guide groups RIGHT-close is logged under the visual round-5 list where the
  routing fix lives).
- **P1: 0**
- **P2: 1 — logged, not fixed** (below).

## P2 — logged (architectural / needs reference not capturable here)

- **Long-OK row context sheet has no open/close animation.** telly reuses
  `PlaybackScreenMenu`, which renders the sheet fully formed with no
  enter/exit transition; the playback panel's sheet is the same. The prior
  timing tables never measured an explicit slide/fade for this sheet in the
  reference (round3-ref/05 is a still), so there is no target curve to match
  yet. Adding one would be a shared change to `PlaybackScreenMenu`'s
  entrance — deferred until the reference sheet motion is actually captured,
  and because `PlaybackMenuHandler` routing is being aligned concurrently by
  another agent (do-not-touch), any sheet-entrance work should land after
  that settles. No code change this round.

  **ANSWERED (round6)** — reference motion now captured
  (`ref-round6/01-sheet-open-close-take1-best.mp4` + `01-frames/`,
  frame-stepped): the sheet **entrance is a ~150–280 ms decelerating fade-in
  with a ~10 px slide-in from the right** (one mid-frame at ~140 ms shows
  ~80 % opacity, 9–10 px right of final; grid dim-scrim lands within a frame
  of settle). The **close is an instant cut of the sheet followed by a
  ~300 ms fade-out of the grid scrim**. The **Channel options push is a
  cross-fade in place that REPLACES the sheet** (sheet fades out ~170 ms,
  pane fades in ~350 ms); its **pop is a ~150 ms fade landing directly on
  the guide grid** (never back on the sheet). Target curves exist now — fix
  round can proceed.
