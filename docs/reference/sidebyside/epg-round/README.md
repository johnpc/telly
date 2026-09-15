# EPG slice on-device round (2026-09-15, tv34)

Verification round for the explicit-EPG-sources slice (merge 0ea5137).

## Acceptance legs

- `@epg-data`: 6/6 green (after the fixes below), run three times.
- `@add-playlist`: 5/5 green, run twice.

First `@epg-data` run was red on "A custom EPG source is added in settings
and merged into the guide data": after the Add-source URL commit the
sources list never showed the URL. True layer = the shared `I type` step's
IME-dismiss loop — it pressed BACK while polling `imeVisible()`, and a
BACK racing the keyboard's own hide reaches the app and pops the
EpgSources pane under test. Fixed in the test layer: the loop now hides
the IME in-process (`windowInsetsController.hide(ime())`, TellyWorld.hideIme)
so no key event can leak to the app.

While reproducing manually a REAL app bug surfaced (D-pad only, invisible
to the semantics-driven tests): pushing a settings section pane loses
D-pad focus. Root cause: the pane switch is an `AnimatedContent`
cross-fade (300 ms) and Compose silently denies `requestFocus()` into the
entering content until the exiting content unmounts; the single-shot
`focusOnAppear` grab therefore no-oped and focus fell through to the
focusable layers under the sheet (guide sheet row / key anchor) — logcat
trace showed the grab landing only at ~frame 20 when retried. Fix:
`focusOnAppear` retries once per frame until the grab sticks (bounded
30 frames, `grabFocusUntilLanded`, JVM-tested). Verified on-device:
pushed General pane focuses Unlock Premium and D-pad moves normally
(fix2-*/h1/h2 screenshots archived in this round's captures).

## Migration v3 -> v4

Baseline debug APK built from 9df603e (worktree), wizard completed against
the :8090 fixture server, 3 zaps recorded, then the 0ea5137 debug build
installed OVER it (same signature):

- before: user_version 3, tables channels/programs/playlists/watch_history,
  30 channels / 889 programs / 4 watch_history rows
- after: user_version 4, epg_sources table created (empty), 30 channels /
  889 programs / 4 watch_history rows intact, no crash, playback resumed
  and the guide rendered channels + EPG (History-blue last-watched row).

MIGRATION_3_4 verdict: PASS.

## Wizard EPG step pixel check (capture 13)

- `ref-13-epg-url-step.png` — reference (TiviMate 5.2.0).
- `telly-13-epg-url-step.png` + `telly-13-epg-url-step-uidump.xml` — telly.
- `telly-13b-down-x3-focus-skip.png` — DOWN x3 from Enter URL: focus stops
  on Paste playlist URL; the greyed "Use default source" is focus-skipped.

Geometry deltas (uidump vs uidump, px): every action row and button within
±4 px of the reference (Enter URL row exact: [980,378][1576,503] vs
[980,378][1575,503]); guidance text block differs only by font line-metric
rounding (ref text nodes are leanback TextViews with different internal
padding). Guidance copy verbatim, url-tvg pre-filled into Enter URL,
Done/Back positions match. No actionable pixel deltas.
