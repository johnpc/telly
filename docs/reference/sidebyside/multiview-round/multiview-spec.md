# Multiview — interaction model (TiviMate 5.2.0 free, tv34, 2026-09-15)

telly does not have Multiview yet; this specs the clone from the reference.
Evidence: this directory (`multiview-round/`). All captures are the free
build against the fixture playlist.

## Headline: free-tier Multiview is a SINGLE-PANE teaser

Free TiviMate LAUNCHES Multiview but the actual mosaic (2+ panes) is
premium-locked. Every action that would add or change a stream funnels to a
channel picker whose selection opens the shared **Unlock Premium** screen.
So the free build shows exactly one pane — the currently-playing channel.

## Entry flow

1. Bare fullscreen playback → **MENU** (or long-OK) → bottom quick-bar
   (`01-quickbar.png`): Search · Channels list · Recordings · **Multiview**
   (monitor icon, 4th slot, label at x≈736) · Picture-in-picture · <res> ·
   Mono · 0 ms · Off. Focus starts on Search; RIGHT ×3 reaches Multiview.
2. OK on Multiview → Multiview opens (`03-multiview-launched.png`): a single
   centered 16:9 pane on a black background showing the current channel
   (fixture NEWS ONE card), with a two-line hint centered below:
   **"Press OK to show menu"** (`[789,897][1131,940]`) /
   **"Your IPTV provider may limit the number of concurrent connections"**
   (`[543,940][1378,978]`). Pane rect ≈ [480,270][1440,810] (960×540,
   half-size, centered) on 1920×1080.

## Pane menu (OK on a pane)

`04-multiview-menu.png` + uidump. OK shows a 400 px-wide dark popup at the
pane's right edge, 3 rows (80 px pitch), focus on the first:
- **Add screen** `[1480,420][1880,500]`
- **Search and add** `[1480,500][1880,580]`
- **Change channel** `[1480,580][1880,660]`

All three open a **channel picker** (`05-two-panes.png` = Add screen,
`08-change-channel.png` = Change channel, `10-search-and-add.png` = Search
and add — the same picker each time): full-height **channel list** on the
left (logo + number + name + current-programme sub-line, focused row
highlighted white; the pane's current channel carries a blue play-arrow),
the focused channel's **schedule** (time + programme rows) in the middle,
and a **programme detail card** (title + air window + progress + synopsis)
top-right. Dimmed live pane behind.

## The premium wall (free-tier limit)

- **Add screen → pick a channel → Unlock Premium** (`06-two-panes-mosaic.png`
  shows the paywall, not a 2-pane mosaic). Adding a 2nd pane is premium.
- **Change channel → pick a channel → Unlock Premium**
  (`09-change-channel-result.png`). Even swapping the single pane's channel
  is premium.
- **Search and add** opens the same picker; selection likewise gated.

So with the free build we can only observe the 1-pane layout. The 2/3/4-pane
mosaic geometry is NOT capturable here (premium). For the clone, telly is
NOT premium-gated — the multi-pane grid layout must therefore be designed
(not cloned pixel-for-pixel); the reference only fixes the ENTRY, the
single-pane framing, the pane menu, and the picker.

## Key map (inside Multiview, free)

| Key | Result | Evidence |
|---|---|---|
| OK (on pane) | pane menu (Add screen / Search and add / Change channel) | 04 |
| OK (menu row) | opens channel picker | 05/08/10 |
| OK (picker row) | Unlock Premium (add/change gated) | 06/09 |
| BACK (picker) | → back to the 1-pane view | mv-back1 |
| BACK (1-pane) | → **exits Multiview to fullscreen player** | 11 |
| D-pad | moves menu/picker focus | 05/08 |

## Exit

BACK from the picker returns to the single pane; BACK from the single pane
exits Multiview straight to fullscreen playback of that channel
(`11-exit-multiview.png`, full-screen NEWS ONE).

## Clone implications (spec, not built this round)

- Reachable from the playback quick-bar's Multiview slot (telly must add
  that slot + a `Route.Multiview`).
- Entry state = one pane (the current channel) centered, hint text — mirror
  the reference framing.
- Pane menu = Add screen / Search and add / Change channel over a channel
  picker reusing telly's existing channel-list + schedule + detail
  components.
- telly is not premium-gated, so it should actually ADD panes: define a
  mosaic layout for 1/2/3/4 panes (reference gives no premium mosaic to
  copy — design it), which pane owns audio, focus movement between panes,
  and CH+/- / OK / BACK semantics inside the grid. These are telly product
  decisions since the reference free build blocks them.
- Exit = BACK from the single/last pane → fullscreen player.
