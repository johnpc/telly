# History re-investigation with accumulated history (history-round2, 2026-09-15)

TiviMate 5.2.0 on tv34, driven READ-ONLY. This round supersedes the
capture-48 reading (§3 of the catalogue) AND the interim Desktop-lane
reading that the History card opens an "EPG schedule browser". Neither is
correct.

## 1. Info overlay cards row (fullscreen → OK/DOWN), history present

Evidence: `02-info-overlay.png` + `02-info-overlay-uidump.xml`.

Bottom shortcut row, left → right (GridView [0,792][1920,1012], 220 px tall):
1. **TV guide** — grid icon, wider text tile. Container [64,796][344,1004]
   (280×208, the focused/first tile is slightly taller).
2. **History** — clock-with-counterclockwise-arrow icon. Tile ~248×184.
3. **Recent-channel cards** — ONE per recently-watched channel, newest
   first. Each is the channel **logo** (square, ~100 px, e.g. purple "MB"
   Music Box, blue "N1" News One) with the channel's **current programme
   title** beneath it in accent blue (e.g. "Classic Rock Bloc…", "Global
   Update: Epi…", "Business Hour: Ep…"). Three cards this session. Tiles
   ~248×184 at x = 608, 872, 1136. They show the PROGRAMME title, not the
   channel name/number. Focusing a recent card adds a bottom detail line:
   `05:00 — 06:00 AM  Classic Rock Block: Episode 21. S1 E21` (air window +
   programme title).
4. **Clear** — trash icon + "Clear" label. Rightmost, tile ~248×184 at
   x = 1400.

So with real history the row is: TV guide · History · [N recent-channel
cards] · Clear. (capture-48 saw none of the recent cards / Clear because
that install had no accumulated history.)

## 2. OK on a recent-channel card → Unlock Premium (NOT a zap)

Evidence: `13-recent-card1-focused.png`, `14-recent-card1-ok-result.png`
(Music-group card), `15-recent-card2-ok-result.png` (News-group card).

OK on a recent-channel card opens the shared **Unlock Premium** screen in
the free build — verified on both a Music Box card and a News One card, so
it is universal, not group-specific. BACK from the paywall returns to
fullscreen playback with NO channel change. Recent cards are
display/premium-gated in free tier; they do not zap directly.

## 3. OK on the History card → a distinct "History" screen (empty here)

Evidence: `12-history-surface-clean.png` +
`12-history-surface-clean-uidump.xml`. (`08`/`11` are earlier attempts that
actually operated on the guide overlay — see Caveats.)

OK on the **History** card opens a DISTINCT full-screen leanback surface,
NOT the guide overlay and NOT an EPG schedule browser:
- Title **"History"** top-RIGHT, `TextView [1568,40][1744,116]`.
- A **trash / clear-all icon** top-right, `ImageView [1792,40][1872,120]`.
- Body centered **"No history"**, `TextView [887,519][1033,562]`.
- Dimmed live video behind; no timeline header, no channel rows, no groups
  column, no cells.
- **BACK → fullscreen player** (verified, `after-hist-back.png`).

The standalone History screen was EMPTY ("No history") this session even
though the info-row recent-channel cards were populated — the two draw from
different sources. The screen is the classic leanback VerticalGrid empty
state (title + clear-all icon + centered empty text); when populated it
would show a grid of channel cards. It could not be populated further here:
CHANNEL_UP on this emulator opens the info overlay rather than zapping
(catalogue §3 note), so no new fullscreen watch sessions could be added.

## 4. Clear card

Evidence: `clear-focus.png` (focused). The Clear card (trash icon +
"Clear", rightmost in the info row) clears the recent-channel history row.
It was **NOT activated** — the round is read-only and must not clear the
emulator's accumulated history — so its confirm-dialog behavior (if any)
is not captured.

## Caveats / method notes

- BACK from the fullscreen info overlay lands on the **guide/channel-list
  panel**, not bare playback. Early attempts (`08`, `11`) therefore
  operated on the guide, not the info overlay. The reliable path is
  long-BACK to bare fullscreen, then OK for the info overlay.
- The info overlay auto-hides ~5 s and resets focus to TV guide; capture
  History-card OK in one fast burst from bare fullscreen.

## Ground truth (supersedes capture-48 §3 and the "EPG schedule browser" reading)

- History card → a plain **"History" list screen** (title + clear-all
  trash icon, empty "No history" here), BACK → player. NOT the guide, NOT
  an EPG grid.
- Recent-channel cards live in the info-overlay row (logo + current
  programme title); OK → Unlock Premium in free tier.
- A **Clear** card ends the row.
