# Round 8 — on-device verification punchlist (2026-09-15, tv34)

Evidence: `multiview-4pane-demo.mp4` (this directory) + screencap probes during the
round (not committed). Reference corpora: `../multiview-round/`, `../history-round2/`.

## Multiview — verified on-device

- **Teaser geometry EXACT:** telly's single pane measures interior (480,270)→(1440,810)
  (the extra 2 px ring is the 2 dp focus border) = the captured [480,270][1440,810].
  Hint text verbatim, pane playing.
- **Pane menu:** rows Add screen / Search and add / Change channel in captured order,
  right of the pane, vertically centered; row pitch and x-position match the reference
  capture 04 within a few px. Remove screen appears only at >1 pane (verified at 4).
- **2/3/4 panes all playing simultaneously:** verified via per-pane burned-in stream
  clocks advancing independently (4 distinct timestamps). 3-pane layout = large left +
  two stacked right; 4-pane = 2×2.
- **No codec exhaustion** on tv34 at 4 panes: no MediaCodec reclaim errors /
  ResourceManager kills in logcat. (Panes freezing at 00:00:59.960 is the 60 s fixture
  TS reaching end-of-media — a fixture artifact, not a codec failure.)
- **Per-pane error presentation:** with the fixture server killed, zapping the focused
  pane rendered the channel name + red "Playback error — ERROR_CODE_IO_NETWORK_
  CONNECTION_FAILED" INSIDE that pane only; the other three panes kept playing.
- **Audio follows focus — verified via `dumpsys media.audio_flinger`:** telly holds one
  AudioTrack per pane; exactly one track sits at L/R 0 dB while the other three read
  -inf dB (muted), and moving D-pad focus one pane flips which single track is unmuted
  (track 920 → 917 observed on one DOWN press).
- **D-pad focus border movement:** 2 dp white border moves with focus (pixel-probed on
  the 2×2 grid); zaps landed on the intended panes after LEFT/UP/RIGHT/DOWN moves.
- **CH+/− zaps the focused pane**, wrapping (CH+ on last channel Music Box 24 wrapped
  to News One).
- **BACK chain:** picker → panes → fullscreen playback of the FOCUSED pane's channel
  (verified: bottom-right pane focused → fullscreen "2 News One HD" via the normal
  cold-start restore path, overlay/badges intact).
- **Picker:** channel list left with play arrow on the pane's channel, focused channel's
  schedule middle (current programme in accent blue), airing-programme detail card
  top-right — structural match; 39 dp single-line rows instead of the reference's
  62.5 dp two-line rows is the DOCUMENTED share-primitives deviation.

### Multiview deltas logged (not fixed)

- **P3 — corner radius:** the reference teaser pane and pane menu have slightly rounded
  corners; telly's are square. Cosmetic; the decisions-log geometry facts (fractions,
  hint, menu rows) all hold.
- **P3 — "Add screen" still listed at 4 panes** (MAX_PANES); the reference free build
  paywalls every add so its at-cap menu is not capturable. Activating it at cap is a
  no-op by design.

## History — verified on-device

- **Overlay shortcut row (uidump 02):** TV guide · History · one recent card per
  recently watched channel (newest first, tuned channel EXCLUDED — exclusion verified
  across two different tuned channels) · Clear rightmost, only while recent cards
  exist. Recent card = channel logo + that channel's CURRENT programme title in accent
  blue (never name/number).
- **Focused recent card (uidumps 04/05):** bottom chevron swaps for the
  `air-time + title` line — format matches the reference dump's
  "04:45 — 05:30 AM   Business Hour…" pattern exactly.
- **OK on a recent card TUNES it** with the zap overlay + keep-frame (the documented
  deviation from the reference's Unlock Premium).
- **Clear card:** empties the row immediately (no confirm); cards + Clear unmount and
  focus falls back to the TV guide card.
- **History screen (uidump 12):** title "History" top-right within ~13 px of the
  reference bounds, clear-all trash right of it; flat app background; first row
  focused; 39 dp rows = logo + name + programme airing at the watch time + watch-time
  clock, newest first. Clear-all acts immediately → centered "No history" (within
  ~20 px of the reference empty-state bounds).
- **BACK from History screen → fullscreen player, no black surface** (stream restarts
  through the cold-start path and renders immediately).
- **OK on a History row tunes it** (persist + pop + playback restore; zap overlay shown).

### History deltas logged (not fixed)

- **P3 — shortcut row sits ~13 dp lower than the reference row** (telly "TV guide"
  label center y=977 px vs reference 950 px; x-centers identical at 204 px). Card
  pitch ~3 dp wider. Chrome-only comparison — the reference session's history was
  empty, so populated-row pixel parity is not capturable.
- **P3 — recent/shortcut cards measure ~139×102 dp vs the catalogue's 150×110 dp**
  reading of capture 34 (pre-existing card component, unchanged this round).

## Fixture note

The fixture streams are 60 s TS files; any pane/fullscreen surface freezes on its last
frame at 00:00:59.960 when the file ends. Real IPTV streams are endless, so this is an
emulator-fixture artifact only. Re-tune (zap) restarts the stream.
