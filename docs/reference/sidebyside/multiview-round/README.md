# Multiview captures (multiview-round, 2026-09-15, TiviMate 5.2.0 free, tv34)

Captures backing `multiview-spec.md`. See that file for the interaction
model. Read-only driving.

- `01-quickbar.png` / `-uidump.xml` — bottom quick-bar; Multiview is the 4th
  slot (monitor icon).
- `02-multiview-slot-focused.png` — Multiview slot focused.
- `03-multiview-launched.png` / `03-multiview-uidump.xml` /
  `03b-multiview-1pane-uidump.xml` — the launched single-pane view + hint.
- `03-multiview-flow-recording.mp4` — 25 s clean recording of the whole
  flow: quick-bar → Multiview → 1-pane → pane menu → BACK → exit to
  fullscreen. (Earlier `03/04-*-recording.mp4` were pulled before
  screenrecord finalized and were removed.)
- `04-multiview-menu.png` / `-uidump.xml` — pane menu (Add screen / Search
  and add / Change channel).
- `05-two-panes.png` / `-uidump.xml` — "Add screen" channel picker.
- `06-two-panes-mosaic.png` / `-uidump.xml` — picking a channel to add →
  Unlock Premium (2nd pane is premium; NOT a mosaic).
- `07-after-paywall-cancel.png` — BACK from paywall → 1-pane.
- `08-change-channel.png` / `09-change-channel-result.png` — Change channel
  picker → selection → Unlock Premium.
- `10-search-and-add.png` — Search and add opens the same picker.
- `11-exit-multiview.png` — BACK from the 1-pane view exits to fullscreen.

## Finding

Free-tier Multiview LAUNCHES but is a single-pane teaser: the current
channel in one centered 16:9 pane, and every mutate action (Add screen /
Change channel / Search and add) funnels through a channel picker whose
selection opens Unlock Premium. The multi-pane mosaic is premium and not
capturable in the free build — telly (not premium-gated) must design the
1/2/3/4-pane grid itself; the reference only fixes entry, single-pane
framing, the pane menu, the picker, and the BACK-to-fullscreen exit.
