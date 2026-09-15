# Round 7 — background/resume P2: stale guide clock + black preview

Date: 2026-09-14 · Emulator tv34 (API 34), fixture server on 10.0.2.2:8090.
All resumes below are WARM (process pid verified unchanged) — this is the
task-switch path from the punch list, not process death.

## Repro (before the fix)

1. telly on the guide at 8:25 PM (`00-telly-guide-before-background.png`),
   preview playing NEWS ONE.
2. HOME, then TiviMate in the foreground ~18 min (the original round-7 repro).
   Logcat: telly kept pulling the stream ~90 s, was paused by TiviMate's
   audio-focus grab at 20:27:09, and the cached-app freezer froze the process
   at 20:28:02 (`ActivityManager: freezing … com.johncorser.telly`).
3. Relaunch telly at 8:45 PM (`01-telly-resume-broken*.png/.mp4`): header
   clock still "Mon, Sep 14, 8:25 PM", now-line at 8:25, info pane still
   "50 min" remaining, preview window permanently black.

Root causes: `GuideController` sampled "now" ONCE at build (clock text,
now-line, info pane, activation all `val`s over it) and nothing anywhere
handled activity STOP/START for the player — the paused, stale live stream
plus the recreated surface came back black and nothing re-tuned.

## Reference behavior (live TiviMate 5.2.0, read-only)

- Guide clock TICKS per minute while the guide is open (8:26 → 8:27 without
  any input).
- On HOME it abandons audio focus and releases its codecs IMMEDIATELY
  (logcat `MediaFocusControl: abandonAudioFocus() … ar.tvplayer.tv` the same
  second) — background streaming stops at once.
- Warm resume after 11 min in background (`02-tm-resume-11min.mp4`,
  `02-tm-resume-guide-clock-839.png`) AND after 90 s
  (`03-tm-resume-90s.mp4`, `03-tm-resume-90s-guide.png`), both times from
  FULLSCREEN playback: TiviMate lands on the TV GUIDE, header clock and
  now-line re-anchored to the true "now", focused cell moved to the airing
  programme. No zap overlay, no spinner. (Its preview window stays untuned
  until OK — this free build never auto-tunes the preview, cf. first open.)

## Fix (device-verified after)

- `GuideNow` minute ticker + foreground re-seed drives header clock,
  now-line, info pane, activation; `originMs` stays anchored.
- `ScreenLifecycleStartStop` (core/ui) + `PlaybackLifecycle`
  (features/playback): ON_STOP stops the stream (`TuneController
  .suspendPlayback`), the ON_START after it re-seeds and recovers — guide
  re-tunes its preview (telly's guide auto-tunes by charter), fullscreen
  exits to the guide like the reference.
- After-fix captures: `04-telly-fixed-resume-guide.png` (clock + preview
  fresh after a long background), `04-telly-fixed-resume.mp4`,
  `05-telly-fixed-fullscreen-resume-lands-on-guide.png`.
